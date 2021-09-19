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

import gov.geoplatform.uasdm.graph.Bureau;
import gov.geoplatform.uasdm.graph.UAV;

public class UAVService
{
  @Request(RequestType.SESSION)
  public JSONObject page(String sessionId, JSONObject criteria)
  {
    return UAV.getPage(criteria).toJSON();
  }

  @Request(RequestType.SESSION)
  public JSONObject apply(String sessionId, JSONObject json)
  {
    UAV platform = UAV.apply(json);

    return platform.toJSON();
  }

  @Request(RequestType.SESSION)
  public void remove(String sessionId, String oid)
  {
    UAV platform = UAV.get(oid);

    if (platform != null)
    {
      platform.delete();
    }
  }

  @Request(RequestType.SESSION)
  public JSONObject get(String sessionId, String oid)
  {
    UAV uav = UAV.get(oid);
    JSONArray bureaus = Bureau.getOptions().stream().map(w -> w.toJSON()).collect(Collector.of(JSONArray::new, JSONArray::put, JSONArray::put));

    JSONObject obj = new JSONObject();
    obj.put("uav", uav.toJSON());
    obj.put("bureaus", bureaus);

    return obj;
  }

  @Request(RequestType.SESSION)
  public JSONObject newInstance(String sessionId)
  {
    UAV uav = new UAV();
    JSONArray bureaus = Bureau.getOptions().stream().map(w -> w.toJSON()).collect(Collector.of(JSONArray::new, JSONArray::put, JSONArray::put));

    JSONObject obj = new JSONObject();
    obj.put("uav", uav.toJSON());
    obj.put("bureaus", bureaus);

    return obj;
  }

  @Request(RequestType.SESSION)
  public JSONArray search(String sessionId, String text)
  {
    List<UAV> uavs = UAV.search(text);

    return uavs.stream().map(w -> {
      JSONObject object = new JSONObject();
      object.put(UAV.OID, w.getOid());
      object.put(UAV.SERIALNUMBER, w.getSerialNumber());
      object.put(UAV.FAANUMBER, w.getFaaNumber());

      return object;
    }).collect(Collector.of(JSONArray::new, JSONArray::put, JSONArray::put));
  }

  @Request(RequestType.SESSION)
  public JSONObject getMetadataOptions(String sessionId, String oid)
  {
    UAV uav = UAV.get(oid);

    return uav.getMetadataOptions();
  }

}
