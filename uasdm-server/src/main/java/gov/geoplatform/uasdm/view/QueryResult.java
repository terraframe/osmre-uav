package gov.geoplatform.uasdm.view;

import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.json.JSONArray;
import org.json.JSONObject;

public class QueryResult
{
  private JSONArray hierarchy;

  private String    id;

  private String    filename;

  public QueryResult()
  {
    this.hierarchy = new JSONArray();
  }

  public String getFilename()
  {
    return filename;
  }

  public void setFilename(String filename)
  {
    this.filename = filename;
  }

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public void addItem(String id, String name)
  {
    if (id != null && name != null)
    {
      JSONObject object = new JSONObject();
      object.put("id", id);
      object.put("label", name);

      hierarchy.put(object);
    }
  }

  private JSONObject toJSON()
  {
    String label = this.filename != null ? this.filename : this.hierarchy.getJSONObject(this.hierarchy.length() - 1).getString("label");

    JSONObject object = new JSONObject();
    object.put("id", this.id);
    object.put("filename", this.filename);
    object.put("hierarchy", this.hierarchy);
    object.put("label", label);

    return object;
  }

  public static QueryResult build(SolrDocument document)
  {
    QueryResult result = new QueryResult();
    result.setId((String) document.getFieldValue("id"));
    result.setFilename((String) document.getFieldValue("filename"));
    result.addItem((String) document.getFieldValue("siteId"), (String) document.getFieldValue("siteName"));
    result.addItem((String) document.getFieldValue("projectId"), (String) document.getFieldValue("projectName"));
    result.addItem((String) document.getFieldValue("missionId"), (String) document.getFieldValue("missionName"));
    result.addItem((String) document.getFieldValue("collectionId"), (String) document.getFieldValue("collectionName"));

    return result;
  }

  public static JSONArray serialize(List<QueryResult> list)
  {
    JSONArray array = new JSONArray();

    for (QueryResult result : list)
    {
      array.put(result.toJSON());
    }

    return array;
  }
}
