package gov.geoplatform.uasdm.model;

import org.apache.commons.lang3.StringUtils;

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

  public JsonObject toJson();

  public default boolean isODM()
  {
    return this.getType().equals(ProcessType.ODM);
  }

  public default boolean isLidar()
  {
    return this.getType().equals(ProcessType.ODM);
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

    throw new UnsupportedOperationException();
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

    throw new UnsupportedOperationException();
  }

}
