/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.geoplatform.uasdm.bus;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

public class Platform extends PlatformBase
{
  private static final long   serialVersionUID = 1790259303;

  public static final String  OTHER            = "OTHER";

  private static final String NEW_INSTANCE     = "newInstance";

  public Platform()
  {
    super();
  }

  public boolean isOther()
  {
    return this.getName().equals(OTHER);
  }

  public JSONObject toJSON()
  {
    JSONObject object = new JSONObject();
    object.put(NEW_INSTANCE, this.isNew());
    object.put(Platform.NAME, this.getName());
    object.put(Platform.DISPLAYLABEL, this.getDisplayLabel());
    object.put(Platform.PLATFORMTYPE, this.getPlatformType());

    if (!this.isNew() || this.isAppliedToDB())
    {
      object.put(Platform.OID, this.getOid());
    }

    return object;
  }

  public static PlatformQuery page(Integer pageSize, Integer pageNumber)
  {
    PlatformQuery query = getQuery();
    query.restrictRows(pageSize, pageNumber);

    return query;
  }

  public static PlatformQuery getQuery()
  {
    PlatformQuery query = new PlatformQuery(new QueryFactory());
    query.ORDER_BY_ASC(query.getDisplayLabel());

    return query;
  }

  public static Platform fromJSON(JSONObject json)
  {
    Platform platform = json.getBoolean(NEW_INSTANCE) ? new Platform() : Platform.get(json.getString(Platform.OID));
    platform.setDisplayLabel(json.getString(Platform.DISPLAYLABEL));
    platform.setName(json.getString(Platform.NAME));
    platform.setPlatformType(json.getString(Platform.PLATFORMTYPE));

    return platform;
  }

  public static JSONObject toJSON(PlatformQuery query)
  {
    JSONArray resultSet = new JSONArray();

    try (OIterator<? extends Platform> it = query.getIterator())
    {
      for (Platform platform : it)
      {
        resultSet.put(platform.toJSON());
      }
    }

    JSONObject json = new JSONObject();
    json.put("count", query.getCount());
    json.put("pageNumber", query.getPageNumber());
    json.put("pageSize", query.getPageSize());
    json.put("resultSet", resultSet);

    return json;
  }

  public static JSONArray getAll()
  {
    JSONArray array = new JSONArray();

    PlatformQuery query = Platform.getQuery();

    try (OIterator<? extends Platform> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        array.put(iterator.next().toJSON());
      }
    }

    return array;
  }

}
