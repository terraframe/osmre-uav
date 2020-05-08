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
package gov.geoplatform.uasdm.service;

import org.json.JSONObject;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.uasdm.bus.Sensor;
import gov.geoplatform.uasdm.bus.SensorQuery;

public class SensorService
{
  @Request(RequestType.SESSION)
  public JSONObject page(String sessionId, Integer pageNumber)
  {
    SensorQuery query = Sensor.page(20, pageNumber);
    query.WHERE(query.getName().NE(Sensor.OTHER));

    return Sensor.toJSON(query);
  }

  @Request(RequestType.SESSION)
  public JSONObject apply(String sessionId, JSONObject json)
  {
    Sensor sensor = Sensor.fromJSON(json);
    sensor.apply();

    return sensor.toJSON();
  }

  @Request(RequestType.SESSION)
  public void remove(String sessionId, String oid)
  {
    Sensor sensor = Sensor.get(oid);

    if (sensor != null)
    {
      sensor.delete();
    }
  }

  @Request(RequestType.SESSION)
  public JSONObject get(String sessionId, String oid)
  {
    return Sensor.get(oid).toJSON();
  }

  @Request(RequestType.SESSION)
  public JSONObject lock(String sessionId, String oid)
  {
    return Sensor.lock(oid).toJSON();
  }

  @Request(RequestType.SESSION)
  public JSONObject unlock(String sessionId, String oid)
  {
    return Sensor.unlock(oid).toJSON();
  }

  @Request(RequestType.SESSION)
  public JSONObject newInstance(String sessionId)
  {
    return new Sensor().toJSON();
  }
}
