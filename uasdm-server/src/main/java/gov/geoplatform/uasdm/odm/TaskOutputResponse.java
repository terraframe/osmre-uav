package gov.geoplatform.uasdm.odm;

import org.json.JSONArray;

public interface TaskOutputResponse extends ODMResponse
{
  public Boolean hasOutput();

  public JSONArray getOutput();
}