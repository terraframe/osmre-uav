package gov.geoplatform.uasdm.odm;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import gov.geoplatform.uasdm.view.RequestParserIF;

public class ODMProcessConfiguration
{
  private boolean    includeGeoLocationFile;

  private String     outFileNamePrefix;

  private BigDecimal resolution;

  public ODMProcessConfiguration()
  {
    this.includeGeoLocationFile = false;
    this.outFileNamePrefix = "";
    this.resolution = new BigDecimal(5);
  }

  public ODMProcessConfiguration(String outFileNamePrefix)
  {
    this.includeGeoLocationFile = false;
    this.outFileNamePrefix = outFileNamePrefix;
    this.resolution = new BigDecimal(5);
  }

  public boolean isIncludeGeoLocationFile()
  {
    return includeGeoLocationFile;
  }

  public void setIncludeGeoLocationFile(boolean includeGeoLocationFile)
  {
    this.includeGeoLocationFile = includeGeoLocationFile;
  }

  public String getOutFileNamePrefix()
  {
    return outFileNamePrefix;
  }

  public void setOutFileNamePrefix(String outFileNamePrefix)
  {
    this.outFileNamePrefix = outFileNamePrefix;
  }

  public BigDecimal getResolution()
  {
    return resolution;
  }

  public void setResolution(BigDecimal resolution)
  {
    this.resolution = resolution;
  }

  public JsonObject toJson()
  {
    JsonObject object = new JsonObject();
    object.addProperty("includeGeoLocationFile", this.includeGeoLocationFile);
    object.addProperty("outFileNamePrefix", this.outFileNamePrefix);
    object.addProperty("resolution", this.resolution.toString());

    return object;
  }

  public static ODMProcessConfiguration parse(String jsonString)
  {
    JsonObject object = JsonParser.parseString(jsonString).getAsJsonObject();

    ODMProcessConfiguration configuration = new ODMProcessConfiguration();

    if (object.has("includeGeoLocationFile"))
    {
      JsonElement element = object.get("includeGeoLocationFile");

      if (!element.isJsonNull())
      {
        configuration.setIncludeGeoLocationFile(object.get("includeGeoLocationFile").getAsBoolean());
      }
    }

    if (object.has("outFileNamePrefix"))
    {
      JsonElement element = object.get("outFileNamePrefix");

      if (!element.isJsonNull())
      {
        configuration.setOutFileNamePrefix(element.getAsString());
      }
    }

    if (object.has("resolution"))
    {
      JsonElement element = object.get("resolution");

      if (!element.isJsonNull())
      {
        configuration.setResolution(new BigDecimal(element.getAsString()));
      }
    }

    return configuration;
  }

  public static ODMProcessConfiguration parse(RequestParserIF parser)
  {
    ODMProcessConfiguration configuration = new ODMProcessConfiguration();

    if (!StringUtils.isEmpty(parser.getCustomParams().get("outFileName")))
    {
      String outFileNamePrefix = parser.getCustomParams().get("outFileName");
      configuration.setOutFileNamePrefix(outFileNamePrefix);
    }

    if (!StringUtils.isEmpty(parser.getCustomParams().get("includeGeoLocationFile")))
    {
      Boolean includeGeoLocationFile = Boolean.valueOf(parser.getCustomParams().get("includeGeoLocationFile"));
      configuration.setIncludeGeoLocationFile(includeGeoLocationFile);
    }

    if (!StringUtils.isEmpty(parser.getCustomParams().get("resolution")))
    {
      BigDecimal resolution = new BigDecimal(parser.getCustomParams().get("resolution"));
      configuration.setResolution(resolution);
    }

    return configuration;
  }
}
