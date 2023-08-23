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
package gov.geoplatform.uasdm.service;

import java.util.List;
import java.util.stream.Collector;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.uasdm.graph.Platform;

public class PlatformService
{
  @Request(RequestType.SESSION)
  public JSONObject page(String sessionId, JSONObject criteria)
  {
    return Platform.getPage(criteria).toJSON();
  }

  @Request(RequestType.SESSION)
  public JSONArray getAll(String sessionId)
  {
    return Platform.getAll();
  }

  @Request(RequestType.SESSION)
  public JSONObject apply(String sessionId, JSONObject json)
  {
    Platform platform = Platform.apply(json);

    return platform.toJSON();
  }

  @Request(RequestType.SESSION)
  public void remove(String sessionId, String oid)
  {
    Platform platform = Platform.get(oid);

    if (platform != null)
    {
      platform.delete();
    }
  }

  @Request(RequestType.SESSION)
  public JSONObject get(String sessionId, String oid)
  {
    return Platform.get(oid).toJSON();
  }

  @Request(RequestType.SESSION)
  public JSONObject newInstance(String sessionId)
  {
    return new Platform().toJSON();
  }

  @Request(RequestType.SESSION)
  public JSONArray search(String sessionId, String text)
  {
    List<Platform> platforms = Platform.search(text);

    return platforms.stream().map(w -> {
      JSONObject object = new JSONObject();
      object.put(Platform.OID, w.getOid());
      object.put(Platform.NAME, w.getName());

      return object;
    }).collect(Collector.of(JSONArray::new, JSONArray::put, JSONArray::put));
  }
}
