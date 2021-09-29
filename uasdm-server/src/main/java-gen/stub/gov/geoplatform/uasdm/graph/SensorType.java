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

import org.json.JSONArray;
import org.json.JSONObject;

import gov.geoplatform.uasdm.GenericException;
import gov.geoplatform.uasdm.model.Page;

public class SensorType extends SensorTypeBase implements Classification
{
  private static final long serialVersionUID = 826086552;

  public SensorType()
  {
    super();
  }

  @Override
  public void delete()
  {
    if (Sensor.isSensorTypeReferenced(this))
    {
      GenericException message = new GenericException();
      message.setUserMessage("The sensor type cannot be deleted because it is being used in a sensor");
      throw message;
    }

    super.delete();
  }

  @Override
  public JSONObject toJSON()
  {
    JSONObject object = new JSONObject();
    object.put(SensorType.OID, this.getOid());
    object.put(SensorType.NAME, this.getName());
    object.put(SensorType.ISMULTISPECTRAL, this.getIsMultispectral());

    if (this.getSeq() != null)
    {
      object.put(SensorType.SEQ, this.getSeq());
    }

    return object;
  }

  public static Long getCount()
  {
    return Classification.getCount(SensorType.CLASS);
  }

  public static Page<Classification> getPage(JSONObject criteria)
  {
    return Classification.getPage(SensorType.CLASS, criteria);
  }

  public static JSONArray getAll()
  {
    return Classification.getAll(SensorType.CLASS);
  }

  public static SensorType fromJSON(JSONObject json)
  {
    SensorType classification = null;

    if (json.has(SensorType.OID))
    {
      String oid = json.getString(SensorType.OID);

      if (oid != null)
      {
        classification = SensorType.get(oid);
      }
    }

    if (classification == null)
    {
      classification = new SensorType();
    }

    classification.setName(json.getString(SensorType.NAME));

    if (json.has(SensorType.ISMULTISPECTRAL))
    {
      classification.setIsMultispectral(json.getBoolean(SensorType.ISMULTISPECTRAL));
    }
    else
    {
      classification.setIsMultispectral(Boolean.FALSE);
    }

    if (json.has(SensorType.SEQ))
    {
      classification.setSeq(json.getLong(SensorType.SEQ));
    }

    return classification;
  }
}
