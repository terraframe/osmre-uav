/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.geoplatform.uasdm.view;

import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.json.JSONArray;
import org.json.JSONObject;

public class QuerySiteResult implements QueryResult
{
  private JSONArray hierarchy;

  private String    id;

  private String    oid;

  private String    filename;

  private Boolean   isPrivate;

  public QuerySiteResult()
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

  public String getOid()
  {
    return oid;
  }

  public void setOid(String oid)
  {
    this.oid = oid;
  }

  public Boolean getIsPrivate()
  {
    return isPrivate;
  }

  public void setIsPrivate(Boolean isPrivate)
  {
    this.isPrivate = isPrivate;
  }
  
  public JSONArray getHierarchy()
  {
    return hierarchy;
  }

  @Override
  public Type getType()
  {
    return Type.SITE;
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

  public JSONObject toJSON()
  {
    String label = this.filename != null ? this.filename : this.hierarchy.getJSONObject(this.hierarchy.length() - 1).getString("label");

    JSONObject object = new JSONObject();
    object.put("id", this.id);
    object.put("filename", this.filename);
    object.put("hierarchy", this.hierarchy);
    object.put("label", label);
    object.put("type", this.getType().name());
    object.put("isPrivate", this.getIsPrivate());
    object.put("oid", this.getOid());

    return object;
  }

  public static QuerySiteResult build(SolrDocument document)
  {
    QuerySiteResult result = new QuerySiteResult();
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
