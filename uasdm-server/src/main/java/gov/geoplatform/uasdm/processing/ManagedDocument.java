package gov.geoplatform.uasdm.processing;

import java.io.File;

import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;

import gov.geoplatform.uasdm.graph.Document;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.EdgeType;
import gov.geoplatform.uasdm.service.SolrService;

public class ManagedDocument extends S3FileUpload
{
  private boolean searchable;
  
  public ManagedDocument(String s3Path, Product product, CollectionIF collection, StatusMonitorIF monitor)
  {
    this(s3Path, product, collection, monitor, true);
  }

  public ManagedDocument(String s3Path, Product product, CollectionIF collection, StatusMonitorIF monitor, boolean searchable)
  {
    super(s3Path, product, collection, monitor);

    this.searchable = searchable;
  }
  
  protected ManagedDocumentTool getTool()
  {
    return ManagedDocumentTool.ODM;
  }

  @Override
  public boolean process(File file)
  {
    boolean success = super.process(file);
    
    if (!file.isDirectory())
    {
      String key = this.getS3Key();
      
      String documentName = key.substring(key.lastIndexOf("/") + 1);
      
      DocumentIF document = this.collection.createDocumentIfNotExist(key, documentName, null, this.getTool().name());
      
      final MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.PRODUCT_HAS_DOCUMENT);

      this.product.addChild((Document) document, mdEdge).apply();

      if (searchable)
      {
        SolrService.updateOrCreateDocument(this.collection.getAncestors(), this.collection, key, documentName);
      }
    }
    
    return success;
  }
}