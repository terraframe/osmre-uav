package gov.geoplatform.uasdm.model;

import java.io.IOException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.graph.Document;
import gov.geoplatform.uasdm.graph.Imagery;
import gov.geoplatform.uasdm.graph.Mission;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.graph.Project;
import gov.geoplatform.uasdm.graph.Site;
import gov.geoplatform.uasdm.graph.UasComponent;

public class GraphStrategy implements ComponentStrategy
{

  public UasComponentIF getComponent(String oid)
  {
    return UasComponent.get(oid);
  }

  public SiteIF getSite(String oid)
  {
    return Site.get(oid);
  }

  public ProjectIF getProject(String oid)
  {
    return Project.get(oid);
  }

  public MissionIF getMission(String oid)
  {
    return Mission.get(oid);
  }

  public CollectionIF getCollection(String oid)
  {
    return Collection.get(oid);
  }

  public ImageryIF getImagery(String oid)
  {
    return Imagery.get(oid);
  }

  public ProductIF getProduct(String oid)
  {
    return Product.get(oid);
  }

  public DocumentIF getDocument(String oid)
  {
    return Document.get(oid);
  }

  public List<SiteIF> getSites(String bounds)
  {
    return Site.getSites(bounds);
  }

  public java.util.Collection<CollectionIF> getMissingMetadata()
  {
    return Collection.getMissingMetadata();
  }

  @Override
  public UasComponentIF newRoot()
  {
    return new Site();
  }

  @Override
  public JSONObject features() throws IOException
  {
    return Site.features();
  }

  @Override
  public JSONArray bbox()
  {
    return UasComponent.bbox();
  }
}
