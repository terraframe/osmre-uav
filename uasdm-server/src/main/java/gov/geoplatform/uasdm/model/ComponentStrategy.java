package gov.geoplatform.uasdm.model;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public interface ComponentStrategy
{
  public UasComponentIF getComponent(String oid);

  public SiteIF getSite(String oid);

  public ProjectIF getProject(String oid);

  public MissionIF getMission(String oid);

  public CollectionIF getCollection(String oid);

  public ImageryIF getImagery(String oid);

  public ProductIF getProduct(String oid);

  public DocumentIF getDocument(String oid);

  public List<ProductIF> getProducts();

  public List<SiteIF> getSites(String bounds);

  public Page<MetadataMessage> getMissingMetadata(Integer pageNumber, Integer pageSize);

  public long getMissingMetadataCount();

  public UasComponentIF newRoot();

  public JSONObject features() throws IOException;

  public JSONArray bbox();
}
