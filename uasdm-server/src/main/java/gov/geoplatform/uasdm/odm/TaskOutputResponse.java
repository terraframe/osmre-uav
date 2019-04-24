package gov.geoplatform.uasdm.odm;

import org.json.JSONArray;

public class TaskOutputResponse extends ODMResponse
{

  public TaskOutputResponse(HTTPResponse httpResp)
  {
    super(httpResp);
  }
  
  public Boolean hasOutput()
  {
    return http.isJSONArray();
  }

  public JSONArray getOutput()
  {
    return http.getJSONArray();
  }
  
}
