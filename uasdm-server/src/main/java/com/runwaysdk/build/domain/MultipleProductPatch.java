package com.runwaysdk.build.domain;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.cache.globalcache.ehcache.CacheShutdown;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.command.GenerateMetadataCommand;
import gov.geoplatform.uasdm.command.ReIndexStacItemCommand;
import gov.geoplatform.uasdm.controller.PointcloudController;
import gov.geoplatform.uasdm.graph.Document;
import gov.geoplatform.uasdm.graph.ODMRun;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.graph.UasComponent;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.processing.ODMZipPostProcessor;
import gov.geoplatform.uasdm.processing.report.CollectionReportFacade;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.service.IndexService;
import gov.geoplatform.uasdm.view.SiteObjectsResultSet;

public class MultipleProductPatch implements Runnable
{
  private static final Logger logger = LoggerFactory.getLogger(MultipleProductPatch.class);

  public static void main(String[] args)
  {
    try
    {
      new MultipleProductPatch().run();
    }
    finally
    {
      if (args.length > 0 && Boolean.valueOf(args[0]))
      {
        IndexService.shutdown();
        CollectionReportFacade.finish();
        CacheShutdown.shutdown();
      }
    }

  }

  public static void start()
  {
    Thread t = new Thread(new MultipleProductPatch(), "MultipleProductPatch");
    t.setDaemon(true);
    t.start();
  }

  @Request
  public void run()
  {
    processProducts();

    logger.error("Transaction committed");
  }

  protected void processProducts()
  {
    List<Product> products = this.getProducts();

    logger.error("Patcher will migrate [" + products.size() + "] existing products to allow for multiple products.");

    for (Product sourceProduct : products)
    {
      processProduct(sourceProduct);
    }

    logger.error("Transaction finished");
  }

  @Transaction
  private void processProduct(Product sourceProduct)
  {
    // Create the new Product
    UasComponent component = sourceProduct.getComponent();
    
    if (! (component instanceof CollectionIF)) return;

    Product targetProduct = (Product) component.createProductIfNotExist(System.currentTimeMillis() + "");
    targetProduct.setPrimary(component.getPrimaryProduct().isEmpty());
    targetProduct.setBoundingBox(sourceProduct.getBoundingBoxInternal());
    targetProduct.apply();

    sourceProduct.getGeneratedFromDocuments().forEach(doc -> {
      doc.addGeneratedProduct(targetProduct);
    });

    copyGeneratedDocuments(sourceProduct, component, targetProduct);

    if (sourceProduct.isPublished() && !component.isPrivate())
    {
      logger.error("Publishing product [" + targetProduct.getS3location() + "]");

      // NOTE: Publishing will also re-create the stac item
      targetProduct.togglePublished();
    }
    else
    {
      // Index the new product
      new ReIndexStacItemCommand(targetProduct).doIt();
    }

    // Delete the old product
    sourceProduct.delete();

    // Regenerate the component XML ??
    // This will happen as part of the CollectionMetadataPatch
//    if (component instanceof CollectionIF) {
//      new GenerateMetadataCommand(component, null, ((CollectionIF)component).getMetadata().orElseThrow()).doIt();
//    }

    logger.error("Migration finished for product [" + component.getS3location() + "]");
  }

  private void copyGeneratedDocuments(Product sourceProduct, UasComponent component, Product targetProduct)
  {
    logger.error("Moving documents for product [" + component.getS3location() + "] to [" + targetProduct.getS3location() + "]");

    List<Document> sourceDocuments = sourceProduct.getDocuments().stream().map(d -> (Document) d).collect(Collectors.toList());

    // Copy over all files in the product folders
    sourceDocuments.stream().findFirst().ifPresent(document -> {

      String location = document.getS3location();

      String sourceRoot = null;

      if (!location.startsWith(component.getS3location() + ImageryComponent.PRODUCTS))
      {
        // Older version of the product data
        sourceRoot = component.getS3location();
      }
      else
      {
        // Newer version of product data
        String productName = location.replaceFirst(component.getS3location() + ImageryComponent.PRODUCTS + "/", "").split("/")[0];

        sourceRoot = component.getS3location() + ImageryComponent.PRODUCTS + "/" + productName + "/";
      }

      String[] folders = new String[] { ImageryComponent.DEM, ImageryComponent.ORTHO, ImageryComponent.PTCLOUD, Product.ODM_ALL_DIR };

      for (String folder : folders)
      {
        String sourceKey = sourceRoot + folder;
        String targetKey = component.getS3location(targetProduct, folder);
        
        if (targetKey.endsWith("/")) targetKey = targetKey.substring(0, targetKey.length() - 1);

        logger.error("Copying source folder [" + sourceKey + "] to target folder [" + targetKey + "]");

        RemoteFileFacade.copyFolder(sourceKey, AppProperties.getBucketName(), targetKey, AppProperties.getBucketName());
      }
    });

    // Create new document objects for all the source documents
    List<DocumentIF> targetDocuments = sourceDocuments.stream().map(sourceDocument -> {
      String sourceKey = sourceDocument.getS3location();
      String filename = FilenameUtils.getName(sourceKey);
      String uploadTarget = FilenameUtils.getName(FilenameUtils.getPathNoEndSeparator(sourceKey));

      if (uploadTarget.equals("gdal"))
      {
        uploadTarget = ODMZipPostProcessor.DEM_GDAL;
      }
      else if (uploadTarget.equals("entwine_pointcloud"))
      {
        uploadTarget = ODMZipPostProcessor.POTREE;
      }

      String targetKey = component.getS3location(targetProduct, uploadTarget) + filename;

      // Ensure that the source file has already been copied to the target
      // directory
      if (!RemoteFileFacade.objectExists(targetKey))
      {
        logger.error("Moving source key [" + sourceKey + "] target key [" + targetKey + "]");

        RemoteFileFacade.copyObject(sourceKey, AppProperties.getBucketName(), targetKey, AppProperties.getBucketName());
      }

      // Create the new document
      Document document = Document.createIfNotExist(component, targetKey, filename, sourceDocument.toMetadata());

      // Attach the ODM run to the new document if one created the source
      // document
      ODMRun run = ODMRun.getGeneratingRun(sourceDocument);

      if (run != null)
      {
        run.addODMRunOutputChild(document).apply();
      }

      return (DocumentIF) document;
    }).collect(Collectors.toList());

    targetProduct.addDocuments(targetDocuments);
  }

  private List<Product> getProducts()
  {
    final String product0 = MdVertexDAO.getMdVertexDAO(Product.CLASS).getDBClassName();

    StringBuilder builder = new StringBuilder();
    builder.append("SELECT FROM " + product0);
    builder.append(" WHERE productName IS NULL");

    final GraphQuery<Product> query = new GraphQuery<Product>(builder.toString());

    return query.getResults();
  }

}
