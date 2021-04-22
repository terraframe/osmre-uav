package gov.geoplatform.uasdm.geoserver;

import java.util.List;

import org.apache.commons.io.FilenameUtils;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.graph.Document;
import gov.geoplatform.uasdm.graph.UasComponent;
import gov.geoplatform.uasdm.model.CollectionSubfolder;
import gov.geoplatform.uasdm.model.EdgeType;
import gov.geoplatform.uasdm.odm.ODMZipPostProcessor;
import net.geoprism.gis.geoserver.GeoserverProperties;

public class GeoserverLayer extends GeoserverLayerBase
{
  private GeoserverPublisher publisher = new GeoserverPublisher();
  
  public static enum LayerClassification
  {
    ORTHO(CollectionSubfolder.ORTHO.getFolderName() + "/odm_orthophoto.tif"),
    DEM_DSM(CollectionSubfolder.ODM_ALL_GDAL.getFolderName() + "/dsm.tif"),
    DEM_DTM(CollectionSubfolder.ODM_ALL_GDAL.getFolderName() + "/dtm.tif");
    
    private String keyPath;
    
    private LayerClassification(String keyPath)
    {
      this.keyPath = keyPath;
    }

    public String getKeyPath()
    {
      return keyPath;
    }
  }
  
  public GeoserverLayer()
  {
    super();
  }
  
  public GeoserverLayer(UasComponent collection, String layerKey, boolean isPublic)
  {
    super();
    
    this.setLayerKey(layerKey);
    this.setIsPublic(isPublic);
    
    this.setStoreName(this.calculateStoreName(collection, layerKey));
    
    this.setWorkspace(this.calculateWorkspace(isPublic, layerKey));
    
    this.setDirty(true);
  }
  
  protected String calculateStoreName(UasComponent collection, String layerKey)
  {
    String baseName = FilenameUtils.getBaseName(layerKey);

    return collection.getOid() + "-" + baseName;
  }
  
  protected String calculateWorkspace(boolean isPublic, String key)
  {
    if (!isPublic)
    {
      return GeoserverProperties.getWorkspace();
    }
    
    if (key.contains("/" + CollectionSubfolder.ORTHO.getFolderName() + "/"))
    {
      return AppProperties.getPublicWorkspace();
    }
    else if (key.contains("/" + CollectionSubfolder.ODM_ALL_GDAL.getFolderName() + "/"))
    {
      return AppProperties.getPublicHillshadeWorkspace();
    }
    else
    {
      throw new UnsupportedOperationException("Invalid key [" + key + "].");
    }
  }
  
  public LayerClassification getClassification()
  {
    if (this.getLayerKey().contains(CollectionSubfolder.ORTHO.getFolderName() + "/"))
    {
      return LayerClassification.ORTHO;
    }
    else if (this.getLayerKey().contains(CollectionSubfolder.ODM_ALL_GDAL.getFolderName() + "/"))
    {
      return LayerClassification.DEM_DSM;
    }
    else
    {
      throw new UnsupportedOperationException("Unsupported layer key [" + this.getLayerKey() + "].");
    }
  }
  
  public UasComponent getComponent()
  {
    return this.getDocument().getComponent();
  }
  
  public Document getDocument()
  {
    final MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.DOCUMENT_HAS_LAYER);
    final List<Document> parents = this.getParents(mdEdge, Document.class);

    return parents.get(0);
  }
  
  /**
   * Asks Geoserver if this layer exists.
   */
  public boolean isPublished()
  {
    return this.publisher.isPublished(this);
  }
  
  @Override
  public void setIsPublic(Boolean value)
  {
    super.setIsPublic(value);
    
    this.setWorkspace(this.calculateWorkspace(value, this.getLayerKey()));
    
    this.setDirty(true);
  }
  
  @Override
  protected String buildKey()
  {
    return this.getLayerKey();
  }
  
  /**
   * Publishes the layer to Geoserver.
   */
  public void publish()
  {
    this.publisher.publishLayer(this);
    this.setDirty(false);
  }
  
  @Override
  public void delete()
  {
    this.unpublish(false);
    
    super.delete();
  }
  
  public void delete(boolean unpublishLayer)
  {
    if (unpublishLayer)
    {
      this.unpublish(false);
    }
    
    super.delete();
  }
  
  /**
   * Removes the layer from Geoserver, if it has been published previously.
   */
  public void unpublish(boolean atEndOfTransaction)
  {
    this.publisher.unpublishLayer(this, atEndOfTransaction);
    this.setDirty(true);
  }
  
  public static GeoserverLayer getByKey(String layerKey)
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(GeoserverLayer.CLASS);

    String statement = "SELECT FROM " + mdVertex.getDBClassName() + " WHERE layerKey = :layerKey";

    final GraphQuery<GeoserverLayer> query = new GraphQuery<GeoserverLayer>(statement);
    query.setParameter("layerKey", layerKey);

    return query.getSingleResult();
  }
  
  public static List<? extends GeoserverLayer> getDirtyLayers()
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(GeoserverLayer.CLASS);

    String statement = "SELECT FROM " + mdVertex.getDBClassName() + " WHERE dirty = true";

    final GraphQuery<GeoserverLayer> query = new GraphQuery<GeoserverLayer>(statement);

    return query.getResults();
  }
  
  public static List<? extends GeoserverLayer> getAllLayers()
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(GeoserverLayer.CLASS);

    String statement = "SELECT FROM " + mdVertex.getDBClassName();

    final GraphQuery<GeoserverLayer> query = new GraphQuery<GeoserverLayer>(statement);

    return query.getResults();
  }
  
  public static void dirtyAllLayers()
  {
    for (GeoserverLayer layer : getAllLayers())
    {
      layer.setDirty(true);
      layer.apply();
    }
  }
  
}
