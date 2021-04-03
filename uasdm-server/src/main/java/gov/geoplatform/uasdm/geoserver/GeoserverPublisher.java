package gov.geoplatform.uasdm.geoserver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.resource.CloseableFile;

import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.graph.UasComponent;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.odm.ODMZipPostProcessor;
import gov.geoplatform.uasdm.view.SiteObject;
import net.geoprism.gis.geoserver.GeoserverFacade;

public class GeoserverPublisher
{
  public static final Set<String> publishedObjects = new HashSet<String>();
  
  static
  {
    publishedObjects.add(ImageryComponent.ORTHO + "/odm_orthophoto.tif");
    publishedObjects.add(ODMZipPostProcessor.DEM_GDAL + "/dsm.tif");
  }
  
  public static List<SiteObject> getPublishableObjects(UasComponentIF component)
  {
    List<SiteObject> publishedObjects = new ArrayList<SiteObject>();
    
    publishedObjects.addAll(component.getSiteObjects(ImageryComponent.ORTHO, null, null).getObjects());
    
    publishedObjects.addAll(component.getSiteObjects(ODMZipPostProcessor.DEM_GDAL, null, null).getObjects());
    
    return publishedObjects;
  }
  
  public static void createImageServices(String workspace, UasComponentIF component, boolean refreshMosiac)
  {
    try
    {
      List<SiteObject> publishedObjects = getPublishableObjects(component);

      for (SiteObject object : publishedObjects)
      {
        publishGeoTiff(object.getKey(), (UasComponent) component, workspace);
      }

      // Refresh the public mosaic
      if (refreshMosiac)
      {
        new ImageMosaicService().refresh();
      }
    }
    catch (Exception e)
    {
      throw new ProgrammingErrorException(e);
    }
  }
  
  public static void removeImageServices(String workspace, UasComponentIF component, boolean refreshMosiac)
  {
    List<SiteObject> publishedObjects = getPublishableObjects(component);

    for (SiteObject object : publishedObjects)
    {
      unpublish(object.getKey(), (UasComponent) component, workspace);
    }
    
    // Refresh the public mosaic
    if (refreshMosiac)
    {
      new ImageMosaicService().refresh();
    }
  }
  
  public static void refreshImageServices(String workspace, UasComponentIF component)
  {
    createImageServices(workspace, component, false);
    removeImageServices(workspace, component, false);
    new ImageMosaicService().refresh();
  }
  
  public static void unpublish(String key, UasComponent component, String workspace)
  {
    if (shouldPublish(key))
    {
      String storeName = component.getStoreName(key);

      if (GeoserverFacade.layerExists(workspace, storeName))
      {
        removeCoverageStore(workspace, storeName);
      }
    }
  }
  
  public static void publishGeoTiff(String key, UasComponentIF collection, String workspace)
  {
    if (shouldPublish(key))
    {
      final String storeName = collection.getStoreName(key);

      if (GeoserverFacade.layerExists(workspace, storeName))
      {
        removeCoverageStore(workspace, storeName);
      }

      try (CloseableFile geotiff = Util.download(key, storeName))
      {
        GeoserverFacade.publishGeoTiff(workspace, storeName, geotiff);
      }
    }
  }
  
  public static boolean shouldPublish(String key)
  {
    for (String expected : publishedObjects)
    {
      if (key.endsWith(expected))
      {
        return true;
      }
    }
    
    return false;
  }
  
  public static void removeCoverageStore(String workspace, String storeName)
  {
//    GeoserverFacade.removeStyle(storeName);
//    GeoserverFacade.forceRemoveLayer(workspace, storeName);
    GeoserverFacade.removeCoverageStore(workspace, storeName);
  }
}
