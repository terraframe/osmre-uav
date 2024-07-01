package com.runwaysdk.build.domain;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
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
import gov.geoplatform.uasdm.graph.Document;
import gov.geoplatform.uasdm.graph.ODMRun;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.graph.UasComponent;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.processing.report.CollectionReportFacade;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.service.IndexService;

public class MultipleProductPatch implements Runnable
{
  private static final Logger logger = LoggerFactory.getLogger(MultipleProductPatch.class);

  public static void main(String[] args)
  {
    new MultipleProductPatch().run();
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
    try
    {
      try
      {
        try
        {
          transaction();

          logger.error("Transaction committed");
        }
        finally
        {
          CollectionReportFacade.finish();
        }
      }
      finally
      {
        IndexService.shutdown();
      }
    }
    finally
    {
      CacheShutdown.shutdown();
    }
  }

  @Transaction
  protected void transaction()
  {
    List<Product> products = this.getProducts();

    logger.error("Patcher will migrate [" + products.size() + "] existing products to allow for multiple products.");

    for (Product sourceProduct : products)
    {
      // Create the new Product
      UasComponent component = sourceProduct.getComponent();

      Product targetProduct = (Product) component.createProductIfNotExist(System.currentTimeMillis() + "");
      targetProduct.setPrimary(true);
      targetProduct.setBoundingBox(sourceProduct.getBoundingBoxInternal());
      targetProduct.apply();

      sourceProduct.getGeneratedFromDocuments().forEach(doc -> {
        doc.addGeneratedProduct(targetProduct);
      });

      copyGeneratedDocuments(sourceProduct, component, targetProduct);

      if (sourceProduct.isPublished())
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
      new GenerateMetadataCommand((CollectionIF) component).doIt();

      logger.error("Migration finished for product [" + component.getS3location() + "]");
    }

    logger.error("Transaction finished");
  }

  private void copyGeneratedDocuments(Product sourceProduct, UasComponent component, Product targetProduct)
  {
    // Copy documents from the old product to the new product
    logger.error("Moving documents for product [" + component.getS3location() + "] to [" + targetProduct.getS3location() + "]");

    List<Document> sourceDocuments = sourceProduct.getDocuments().stream().map(d -> (Document) d).collect(Collectors.toList());

    List<DocumentIF> targetDocuments = sourceDocuments.stream().map(sourceDocument -> {
      String sourceKey = sourceDocument.getS3location();
      String filename = FilenameUtils.getName(sourceKey);
      String uploadTarget = FilenameUtils.getName(FilenameUtils.getPathNoEndSeparator(sourceKey));

      String targetKey = component.getS3location(targetProduct, uploadTarget) + "/" + filename;

      logger.error("Moving source key [" + sourceKey + "] target key [" + targetKey + "]");

      RemoteFileFacade.copyObject(sourceKey, AppProperties.getBucketName(), targetKey, AppProperties.getBucketName());

      Document document = Document.createIfNotExist(component, targetKey, filename, sourceDocument.toMetadata());

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
