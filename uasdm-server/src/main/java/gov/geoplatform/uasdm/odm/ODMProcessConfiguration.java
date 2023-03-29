package gov.geoplatform.uasdm.odm;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import gov.geoplatform.uasdm.view.RequestParserIF;

public class ODMProcessConfiguration
{
  private boolean includeGeoLocationFile;

  private String  outFileNamePrefix;

  public ODMProcessConfiguration()
  {
    this.includeGeoLocationFile = false;
    this.outFileNamePrefix = null;
  }

  public ODMProcessConfiguration(String outFileNamePrefix)
  {
    this.includeGeoLocationFile = false;
    this.outFileNamePrefix = outFileNamePrefix;
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

  public JsonObject toJson()
  {
    JsonObject object = new JsonObject();
    object.addProperty("includeGeoLocationFile", this.includeGeoLocationFile);
    object.addProperty("outFileNamePrefix", this.outFileNamePrefix);

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

    return configuration;
  }

  public static ODMProcessConfiguration parse(RequestParserIF parser)
  {
    String outFileNamePrefix = parser.getCustomParams().get("outFileName");
    Boolean includeGeoLocationFile = Boolean.valueOf(parser.getCustomParams().get("includeGeoLocationFile"));

    ODMProcessConfiguration configuration = new ODMProcessConfiguration();
    configuration.setOutFileNamePrefix(outFileNamePrefix);
    configuration.setIncludeGeoLocationFile(includeGeoLocationFile);

    return configuration;
  }
}
