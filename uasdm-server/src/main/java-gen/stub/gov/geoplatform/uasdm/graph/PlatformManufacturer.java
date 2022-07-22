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

import com.runwaysdk.business.graph.GraphQuery;

import gov.geoplatform.uasdm.GenericException;
import gov.geoplatform.uasdm.model.Page;

public class PlatformManufacturer extends PlatformManufacturerBase implements Classification
{
  private static final long serialVersionUID = 1353153000;
  
  public static final String PARROT_ANAFI_USA = "Parrot Anafi USA";
  
  public static final String DJI = "DJI";
  
  public static final String TRIMBLE = "Trimble";

  public PlatformManufacturer()
  {
    super();
  }
  
  public static PlatformManufacturer getByName(String name)
  {
    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM platform_manufacturer WHERE name=:name");

    final GraphQuery<PlatformManufacturer> query = new GraphQuery<PlatformManufacturer>(statement.toString());
    query.setParameter("name", name);
    
    return query.getSingleResult();
  }

  @Override
  public void delete()
  {
    if (Platform.isPlatformManufacturerReferenced(this))
    {
      GenericException message = new GenericException();
      message.setUserMessage("The manufacturer cannot be deleted because it is being used in a platform");
      throw message;
    }

    super.delete();
  }

  public static Long getCount()
  {
    return Classification.getCount(PlatformManufacturer.CLASS);
  }

  public static Page<Classification> getPage(JSONObject criteria)
  {
    return Classification.getPage(PlatformManufacturer.CLASS, criteria);
  }

  public static JSONArray getAll()
  {
    return Classification.getAll(PlatformManufacturer.CLASS);
  }

  public static PlatformManufacturer fromJSON(JSONObject json)
  {
    PlatformManufacturer classification = null;

    if (json.has(PlatformManufacturer.OID))
    {
      String oid = json.getString(PlatformManufacturer.OID);

      if (oid != null)
      {
        classification = PlatformManufacturer.get(oid);
      }
    }

    if (classification == null)
    {
      classification = new PlatformManufacturer();
    }

    classification.setName(json.getString(PlatformManufacturer.NAME));

    if (json.has(PlatformManufacturer.SEQ))
    {
      classification.setSeq(json.getLong(PlatformManufacturer.SEQ));
    }

    return classification;
  }

}
