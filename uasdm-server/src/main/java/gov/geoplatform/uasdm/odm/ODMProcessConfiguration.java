package gov.geoplatform.uasdm.odm;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
    configuration.setIncludeGeoLocationFile(object.get("includeGeoLocationFile").getAsBoolean());
    configuration.setOutFileNamePrefix(object.get("outFileNamePrefix").getAsString());

    return configuration;
  }
}
