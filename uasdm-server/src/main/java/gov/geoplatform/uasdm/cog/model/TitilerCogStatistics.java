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
package gov.geoplatform.uasdm.cog.model;

import java.util.Iterator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.runwaysdk.dataaccess.ProgrammingErrorException;

public class TitilerCogStatistics
{
  private ObjectNode json;

  public TitilerCogStatistics(String json)
  {
    ObjectMapper objectMapper = new ObjectMapper();

    try
    {
      this.json = (ObjectNode) objectMapper.readTree(json);
    }
    catch (JsonProcessingException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public TitilerCogBandStatistic getBandStatistic(String band)
  {
    ObjectMapper objectMapper = new ObjectMapper();

    JsonNode bandJson = json.get(band);

    return objectMapper.convertValue(bandJson, TitilerCogBandStatistic.class);
  }

  public TitilerCogBandStatistic getBandStatistic()
  {
    ObjectMapper objectMapper = new ObjectMapper();

    Iterator<String> fieldNames = json.fieldNames();
    JsonNode bandJson = json.get(fieldNames.next());

    return objectMapper.convertValue(bandJson, TitilerCogBandStatistic.class);
  }

  public Iterator<String> getBandNames()
  {
    return this.json.fieldNames();
  }
}
