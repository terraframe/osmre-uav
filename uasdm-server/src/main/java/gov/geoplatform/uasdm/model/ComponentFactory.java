package gov.geoplatform.uasdm.model;

import java.io.IOException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class ComponentFactory
{
  private static ComponentStrategy INSTANCE = new GraphStrategy();

  public static UasComponentIF getComponent(String oid)
  {
    return INSTANCE.getComponent(oid);
  }

  public static SiteIF getSite(String oid)
  {
    return INSTANCE.getSite(oid);
  }

  public static ProjectIF getProject(String oid)
  {
    return INSTANCE.getProject(oid);
  }

  public static MissionIF getMission(String oid)
  {
    return INSTANCE.getMission(oid);
  }

  public static CollectionIF getCollection(String oid)
  {
    return INSTANCE.getCollection(oid);
  }

  public static ImageryIF getImagery(String oid)
  {
    return INSTANCE.getImagery(oid);
  }

  public static ProductIF getProduct(String oid)
  {
    return INSTANCE.getProduct(oid);
  }

  public static DocumentIF getDocument(String oid)
  {
    return INSTANCE.getDocument(oid);
  }

  public static List<SiteIF> getSites(String bounds)
  {
    return INSTANCE.getSites(bounds);
  }

  public static java.util.Collection<CollectionIF> getMissingMetadata()
  {
    return INSTANCE.getMissingMetadata();
  }

  public static UasComponentIF newRoot()
  {
    return INSTANCE.newRoot();
  }

  public static JSONObject features() throws IOException
  {
    return INSTANCE.features();
  }

  public static JSONArray bbox()
  {
    return INSTANCE.bbox();
  }
}
