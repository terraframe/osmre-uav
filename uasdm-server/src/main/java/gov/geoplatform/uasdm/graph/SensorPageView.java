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
package gov.geoplatform.uasdm.graph;

import org.json.JSONObject;

import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.model.JSONSerializable;

public class SensorPageView implements JSONSerializable
{
  private Sensor sensor;

  public SensorPageView(Sensor sensor)
  {
    super();

    this.sensor = sensor;
  }

  @Override
  public Object toJSON()
  {
    JSONObject object = new JSONObject();
    object.put(Sensor.OID, this.sensor.getOid());
    object.put(Sensor.NAME, this.sensor.getName());
    object.put(Sensor.DESCRIPTION, this.sensor.getDescription());
    object.put(Sensor.MODEL, this.sensor.getModel());
    object.put(Sensor.PIXELSIZEHEIGHT, this.sensor.getRealPixelSizeHeight());
    object.put(Sensor.PIXELSIZEWIDTH, this.sensor.getRealPixelSizeWidth());
    object.put(Sensor.SENSORHEIGHT, this.sensor.getRealSensorHeight());
    object.put(Sensor.SENSORWIDTH, this.sensor.getRealSensorWidth());

    if (this.sensor.getDateCreated() != null)
    {
      object.put(Sensor.DATECREATED, Util.formatIso8601(this.sensor.getDateCreated(), false));
    }

    if (this.sensor.getDateUpdated() != null)
    {
      object.put(Sensor.DATEUPDATED, Util.formatIso8601(this.sensor.getDateUpdated(), false));
    }

    SensorType sensorType = this.sensor.getSensorType();

    if (sensorType != null)
    {
      object.put(Sensor.SENSORTYPE, sensorType.getName());
    }

    if (this.sensor.getSeq() != null)
    {
      object.put(Sensor.SEQ, this.sensor.getSeq());
    }

    return object;
  }

}
