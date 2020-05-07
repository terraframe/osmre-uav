package gov.geoplatform.uasdm.model;

import java.io.IOException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class ComponentFacade
{
  private static ComponentStrategy STRATEGY = new GraphStrategy();

  public static UasComponentIF getComponent(String oid)
  {
    return STRATEGY.getComponent(oid);
  }

  public static SiteIF getSite(String oid)
  {
    return STRATEGY.getSite(oid);
  }

  public static ProjectIF getProject(String oid)
  {
    return STRATEGY.getProject(oid);
  }

  public static MissionIF getMission(String oid)
  {
    return STRATEGY.getMission(oid);
  }

  public static CollectionIF getCollection(String oid)
  {
    return STRATEGY.getCollection(oid);
  }

  public static ImageryIF getImagery(String oid)
  {
    return STRATEGY.getImagery(oid);
  }

  public static ProductIF getProduct(String oid)
  {
    return STRATEGY.getProduct(oid);
  }

  public static List<ProductIF> getProducts()
  {
    return STRATEGY.getProducts();
  }

  public static DocumentIF getDocument(String oid)
  {
    return STRATEGY.getDocument(oid);
  }

  public static List<SiteIF> getSites(String bounds)
  {
    return STRATEGY.getSites(bounds);
  }

  public static long getMissingMetadataCount()
  {
    return STRATEGY.getMissingMetadataCount();
  }

  public static Page<MetadataMessage> getMissingMetadata(Integer pageNumber, Integer pageSize)
  {
    return STRATEGY.getMissingMetadata(pageNumber, pageSize);
  }

  public static UasComponentIF newRoot()
  {
    return STRATEGY.newRoot();
  }

  public static JSONObject features() throws IOException
  {
    return STRATEGY.features();
  }

  public static JSONArray bbox()
  {
    return STRATEGY.bbox();
  }

}
