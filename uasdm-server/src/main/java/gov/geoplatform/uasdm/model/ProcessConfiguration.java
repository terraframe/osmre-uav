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
package gov.geoplatform.uasdm.model;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import gov.geoplatform.uasdm.lidar.LidarProcessConfiguration;
import gov.geoplatform.uasdm.odm.ODMProcessConfiguration;
import gov.geoplatform.uasdm.view.RequestParserIF;

public interface ProcessConfiguration
{
  public static final String TYPE = "type";

  public enum ProcessType {
    ODM, LIDAR
  }

  public ProcessType getType();

  public String getProductName();
  
  public void setProductName(String productName);

  public JsonObject toJson();

  public default boolean isODM()
  {
    return this.getType().equals(ProcessType.ODM);
  }

  public default boolean isLidar()
  {
    return this.getType().equals(ProcessType.LIDAR);
  }

  @SuppressWarnings("unchecked")
  public default <T extends ProcessConfiguration> T toType()
  {
    return (T) this;
  }

  public default ODMProcessConfiguration toODM()
  {
    return this.toType();
  }

  public default LidarProcessConfiguration toLidar()
  {
    return this.toType();
  }

  public static ProcessConfiguration parse(String jsonString)
  {
    JsonObject object = JsonParser.parseString(jsonString).getAsJsonObject();

    if (object.has(TYPE))
    {
      JsonElement element = object.get(TYPE);

      if (!element.isJsonNull())
      {
        ProcessType type = ProcessType.valueOf(object.get(TYPE).getAsString());

        if (type.equals(ProcessType.LIDAR))
        {
          return LidarProcessConfiguration.parse(jsonString);
        }

        return ODMProcessConfiguration.parse(jsonString);
      }
    }

    throw new UnsupportedOperationException("TYPE parameter missing. jsonString: " + jsonString);
  }

  public static ProcessConfiguration parse(RequestParserIF parser)
  {
    if (!StringUtils.isEmpty(parser.getCustomParams().get(TYPE)))
    {
      ProcessType type = ProcessType.valueOf(parser.getCustomParams().get(TYPE));

      if (type.equals(ProcessType.LIDAR))
      {
        return LidarProcessConfiguration.parse(parser);
      }

      return ODMProcessConfiguration.parse(parser);
    }

    throw new UnsupportedOperationException("TYPE parameter missing. Params: " + serialize(parser.getCustomParams()));
  }
  
  private static String serialize(Object obj) {
    try {
      return new ObjectMapper().writeValueAsString(obj);
    } catch (Throwable t) {
      return String.valueOf(obj);
    }
  }

}
