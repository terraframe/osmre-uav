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

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.uasdm.graph.PlatformManufacturer;

public class PlatformManufacturerService
{
  @Request(RequestType.SESSION)
  public JSONObject page(String sessionId, JSONObject criteria)
  {
    return PlatformManufacturer.getPage(criteria).toJSON();
  }

  @Request(RequestType.SESSION)
  public JSONArray getAll(String sessionId)
  {
    return PlatformManufacturer.getAll();
  }

  @Request(RequestType.SESSION)
  public JSONObject apply(String sessionId, JSONObject json)
  {
    PlatformManufacturer manufacturer = PlatformManufacturer.fromJSON(json);
    manufacturer.apply();

    return manufacturer.toJSON();
  }

  @Request(RequestType.SESSION)
  public void remove(String sessionId, String oid)
  {
    PlatformManufacturer manufacturer = PlatformManufacturer.get(oid);

    if (manufacturer != null)
    {
      manufacturer.delete();
    }
  }

  @Request(RequestType.SESSION)
  public JSONObject get(String sessionId, String oid)
  {
    return PlatformManufacturer.get(oid).toJSON();
  }

  @Request(RequestType.SESSION)
  public JSONObject newInstance(String sessionId)
  {
    return new PlatformManufacturer().toJSON();
  }
}
