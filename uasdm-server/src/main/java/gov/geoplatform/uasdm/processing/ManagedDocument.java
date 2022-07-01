package gov.geoplatform.uasdm.processing;

import java.io.File;

import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.graph.Document;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.EdgeType;
import gov.geoplatform.uasdm.service.SolrService;

public class ManagedDocument extends S3FileUpload
{
  private boolean searchable;
  
  private Product product;

  public ManagedDocument(String filename, AbstractWorkflowTask progressTask, Product product, CollectionIF collection, String s3FolderName)
  {
    this(filename, progressTask, product, collection, s3FolderName, true);
  }

  public ManagedDocument(String filename, AbstractWorkflowTask progressTask, Product product, CollectionIF collection, String s3FolderName, boolean searchable)
  {
    super(filename, progressTask, collection, s3FolderName, false);

    this.searchable = searchable;
    this.product = product;
  }

  @Override
  public void handleUnprocessed()
  {
    if (this.progressTask != null)
    {
      this.progressTask.createAction("ODM did not produce an expected file [" + this.getS3FolderName() + "/" + this.filename + "].", "error");
    }
  }

  @Override
  public void processFile(File file, String key)
  {
    super.processFile(file, key);
    
    if (!file.isDirectory())
    {
      DocumentIF document = this.collection.createDocumentIfNotExist(key, file.getName(), null, "ODM");
      
      final MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.PRODUCT_HAS_DOCUMENT);

      this.product.addChild((Document) document, mdEdge).apply();

      if (searchable)
      {
        SolrService.updateOrCreateDocument(this.collection.getAncestors(), this.collection, key, file.getName());
      }
    }
  }
}