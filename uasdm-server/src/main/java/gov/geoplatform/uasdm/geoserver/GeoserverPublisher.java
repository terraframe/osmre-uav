package gov.geoplatform.uasdm.geoserver;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.resource.CloseableFile;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.geoserver.GeoserverLayer.LayerClassification;
import gov.geoplatform.uasdm.graph.Document;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.graph.UasComponent;
import gov.geoplatform.uasdm.model.CollectionSubfolder;
import gov.geoplatform.uasdm.model.EdgeType;
import gov.geoplatform.uasdm.odm.ODMZipPostProcessor;
import gov.geoplatform.uasdm.view.SiteObject;
import net.geoprism.gis.geoserver.GeoserverFacade;
import net.geoprism.gis.geoserver.GeoserverProperties;

public class GeoserverPublisher
{
  private static final Logger logger = LoggerFactory.getLogger(GeoserverPublisher.class);
  
  private boolean geoserverInitialized;
  
  public GeoserverPublisher()
  {
    geoserverInitialized = GeoserverInitializer.isInitialized();
  }
  
  public void initializeGeoserver()
  {
    boolean rebuild = false;
    
    if (!GeoserverFacade.workspaceExists(AppProperties.getPublicWorkspace()))
    {
      GeoserverFacade.publishWorkspace(AppProperties.getPublicWorkspace());

      rebuild = true;
    }

    if (!GeoserverFacade.workspaceExists(GeoserverProperties.getWorkspace()))
    {
      GeoserverFacade.publishWorkspace();

      rebuild = true;
    }
    
    if (!GeoserverFacade.workspaceExists(AppProperties.getPublicHillshadeWorkspace()))
    {
      GeoserverFacade.publishWorkspace(AppProperties.getPublicHillshadeWorkspace());

      rebuild = true;
    }
    
    geoserverInitialized = true;
    
    if (rebuild)
    {
      GeoserverLayer.dirtyAllLayers();
    }
    else
    {
      for (GeoserverLayer layer : GeoserverLayer.getAllLayers())
      {
        if (!this.isPublished(layer))
        {
          layer.setDirty(true);
          layer.apply();
        }
      }
    }
    
    List<? extends GeoserverLayer> dirtyLayers = GeoserverLayer.getDirtyLayers();

    if (dirtyLayers.size() > 0)
    {
      logger.info("Republishing [" + dirtyLayers.size() + "] layers.");

      for (GeoserverLayer layer : dirtyLayers)
      {
        unpublishLayer(layer, false);
        publishLayer(layer);
        
        layer.setDirty(false);
        layer.apply();
      }
      
      ImageMosaicPublisher.refreshAll();
    }
    else
    {
      ImageMosaicPublisher.initializeAll();
    }
  }
  
  public void createImageServices(Document document, Product product, boolean refreshMosiac)
  {
    final UasComponent component = product.getComponent();
    
    try
    {
      List<GeoserverLayer> publishableLayers = getPublishableLayers(document, product, component);

      for (GeoserverLayer layer : publishableLayers)
      {
        if (geoserverInitialized)
        {
          unpublishLayer(layer, false);
          publishLayer(layer);
          
          layer.setDirty(false);
        }
        else
        {
          layer.setDirty(true);
          layer.apply();
        }
      }

      // Refresh the public mosaic
      if (geoserverInitialized && refreshMosiac)
      {
        ImageMosaicPublisher.refreshAll();
      }
    }
    catch (Exception e)
    {
      throw new ProgrammingErrorException(e);
    }
  }
  
  public void removeImageServices(Document document, Product product, boolean refreshMosiac)
  {
    if (!geoserverInitialized)
    {
      return;
    }
    
    final UasComponent component = product.getComponent();
    
    List<GeoserverLayer> publishableLayers = getPublishableLayers(document, product, component);

    for (GeoserverLayer layer : publishableLayers)
    {
      unpublishLayer(layer, false);
      
      layer.delete(false);
    }
    
    // Refresh the public mosaic
    if (refreshMosiac)
    {
      ImageMosaicPublisher.refreshAll();
    }
  }
  
  public void togglePublic(List<GeoserverLayer> layers)
  {
    for (GeoserverLayer layer : layers)
    {
      if (geoserverInitialized && this.isPublished(layer))
      {
        unpublishLayer(layer, false);
        
        layer.setIsPublic(!layer.getIsPublic());
        
        publishLayer(layer);
        
        layer.setDirty(false);
        layer.apply();
      }
      else if (!geoserverInitialized)
      {
        layer.setIsPublic(!layer.getIsPublic());
        layer.setDirty(true);
        layer.apply();
      }
    }
  }
  
  public void refreshImageServices(Document document, Product product, UasComponent component)
  {
    createImageServices(document, product, false);
    removeImageServices(document, product, false);
    
    if (geoserverInitialized)
    {
      ImageMosaicPublisher.refreshAll();
    }
  }
  
  public void unpublishLayer(GeoserverLayer layer, boolean atEndOfTransaction)
  {
    if (geoserverInitialized)
    {
      if (isPublished(layer))
      {
        if (atEndOfTransaction)
        {
          new GeoserverRemoveCoverageCommand(layer.getWorkspace(), layer.getStoreName()).doIt();
        }
        else
        {
          GeoserverFacade.removeCoverageStore(layer.getWorkspace(), layer.getStoreName());
        }
      }
    }
  }
  
  public boolean isPublished(GeoserverLayer layer)
  {
    if (geoserverInitialized)
    {
      return GeoserverFacade.layerExists(layer.getWorkspace(), layer.getStoreName());
    }
    else
    {
      return false;
    }
  }
  
  public void publishLayer(GeoserverLayer layer)
  {
    if (geoserverInitialized)
    {
      try (CloseableFile geotiff = Util.download(layer.getLayerKey(), layer.getStoreName()))
      {
        GeoserverFacade.publishGeoTiff(layer.getWorkspace(), layer.getStoreName(), geotiff);
      }
    }
    else
    {
      layer.setDirty(true);
    }
  }
  
  protected List<GeoserverLayer> getPublishableLayers(Document document, Product product, UasComponent collection)
  {
    List<SiteObject> publishedObjects = new ArrayList<SiteObject>();
    
    publishedObjects.addAll(filterSiteObjects(collection.getSiteObjects(CollectionSubfolder.ORTHO.getFolderName(), null, null).getObjects()));
    
    publishedObjects.addAll(filterSiteObjects(collection.getSiteObjects(CollectionSubfolder.ODM_ALL_GDAL.getFolderName(), null, null).getObjects()));
    
    return createPublishableLayersFromSiteObjects(document, product, collection, publishedObjects);
  }
  
  protected List<GeoserverLayer> createPublishableLayersFromSiteObjects(Document document, Product product, UasComponent collection, List<SiteObject> siteObjects)
  {
    List<GeoserverLayer> layers = new ArrayList<GeoserverLayer>();
    
    for (SiteObject siteObject : siteObjects)
    {
      GeoserverLayer layer = GeoserverLayer.getByKey(siteObject.getKey());
      
      if (layer == null)
      {
        layer = new GeoserverLayer(collection, siteObject.getKey(), product.getPublished());
        layer.apply();
        
        document.addChild(layer, EdgeType.DOCUMENT_HAS_LAYER).apply();
      }
      
      layers.add(layer);
    }
    
    return layers;
  }
  
  protected List<SiteObject> filterSiteObjects(List<SiteObject> objects)
  {
    List<SiteObject> filtered = new ArrayList<SiteObject>();
    
    for (SiteObject object : objects)
    {
      for (LayerClassification classy : GeoserverLayer.LayerClassification.values())
      {
        if (object.getKey().endsWith(classy.getKeyPath()))
        {
          filtered.add(object);
        }
      }
    }
    
    return filtered;
  }
}
