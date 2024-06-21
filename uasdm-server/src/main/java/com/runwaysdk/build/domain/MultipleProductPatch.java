package com.runwaysdk.build.domain;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.constants.graph.MdVertexInfo;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.command.GenerateMetadataCommand;
import gov.geoplatform.uasdm.command.ReIndexStacItemCommand;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.graph.Document;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.graph.UasComponent;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.EdgeType;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.view.SiteObject;
import net.geoprism.configuration.GeoprismProperties;

public class MultipleProductPatch
{
  private static final Logger logger = LoggerFactory.getLogger(MissingRawDocumentFixer.class);

  public static void main(String[] args)
  {
    new MissingRawDocumentFixer().run();
  }

  public static void start()
  {
    Thread t = new Thread(new MissingRawDocumentFixer(), "MultipleProductPatch");
    t.setDaemon(true);
    t.start();
  }

  @Request
  public void run()
  {
    transaction();
  }

  @Transaction
  protected void transaction()
  {
    List<Product> products = this.getProducts();

    logger.error("Patcher will fix [" + products.size() + "] corrupted collections, which have a product but no raw images.");

    for (Product sourceProduct : products)
    {
      logger.error("Moving documents for product [" + sourceProduct.getOid() + "]");

      // Create the new Product
      UasComponent component = sourceProduct.getComponent();
      ProductIF targetProduct = component.createProductIfNotExist(System.currentTimeMillis() + "");

      // Copy documents from the old product to the new product
      List<Document> sourceDocuments = sourceProduct.getDocuments().stream().map(d -> (Document) d).collect(Collectors.toList());

      List<DocumentIF> targetDocuments = sourceDocuments.stream().map(sourceDocument -> {
        String sourceKey = sourceDocument.getS3location();
        String filename = FilenameUtils.getName(sourceKey);
        String uploadTarget = FilenameUtils.getName(FilenameUtils.getPathNoEndSeparator(sourceKey));

        String targetKey = component.getS3location(targetProduct, uploadTarget) + "/" + filename;

        RemoteFileFacade.copyObject(sourceKey, AppProperties.getBucketName(), targetKey, AppProperties.getBucketName());

        return (DocumentIF) Document.createIfNotExist(component, targetKey, filename, sourceDocument.toMetadata());
      }).collect(Collectors.toList());

      targetProduct.addDocuments(targetDocuments);

      if (sourceProduct.isPublished())
      {
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
    }
  }

  private List<Product> getProducts()
  {
    final String product0 = MdVertexDAO.getMdVertexDAO(Product.CLASS).getDBClassName();

    StringBuilder builder = new StringBuilder();
    builder.append("SELECT FROM " + product0 + " WHERE productName IS NULL");

    final GraphQuery<Product> query = new GraphQuery<Product>(builder.toString());

    return query.getResults();
  }

}
