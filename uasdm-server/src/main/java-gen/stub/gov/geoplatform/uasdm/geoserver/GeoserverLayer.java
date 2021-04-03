package gov.geoplatform.uasdm.geoserver;

import java.util.List;

import com.runwaysdk.query.QueryFactory;

public class GeoserverLayer extends GeoserverLayerBase
{
  private static final long serialVersionUID = 512525063;
  
  public GeoserverLayer()
  {
    super();
  }
  
  public static List<? extends GeoserverLayer> getDirtyLayers()
  {
    GeoserverLayerQuery query = new GeoserverLayerQuery(new QueryFactory());
    query.WHERE(query.getDirty().EQ(true));
    
    return query.getIterator().getAll();
  }
  
  public static void clearDirtyLayers()
  {
    for (GeoserverLayer layer : getDirtyLayers())
    {
      layer.delete();
    }
  }
  
}
