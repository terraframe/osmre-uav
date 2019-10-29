package gov.geoplatform.uasdm.model;

import java.util.List;

import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.graph.Document;
import gov.geoplatform.uasdm.graph.Imagery;
import gov.geoplatform.uasdm.graph.Mission;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.graph.Project;
import gov.geoplatform.uasdm.graph.Site;
import gov.geoplatform.uasdm.graph.UasComponent;

public class ComponentFactory
{

  public static UasComponentIF getComponent(String oid)
  {
    return UasComponent.get(oid);
  }

  public static SiteIF getSite(String oid)
  {
    return Site.get(oid);
  }

  public static ProjectIF getProject(String oid)
  {
    return Project.get(oid);
  }

  public static MissionIF getMission(String oid)
  {
    return Mission.get(oid);
  }

  public static CollectionIF getCollection(String oid)
  {
    return Collection.get(oid);
  }

  public static ImageryIF getImagery(String oid)
  {
    return Imagery.get(oid);
  }

  public static ProductIF getProduct(String oid)
  {
    return Product.get(oid);
  }

  public static DocumentIF getDocument(String oid)
  {
    return Document.get(oid);
  }

  public static List<SiteIF> getSites(String bounds)
  {
    return Site.getSites(bounds);
  }

  public static java.util.Collection<CollectionIF> getMissingMetadata()
  {
    return Collection.getMissingMetadata();
  }
}
