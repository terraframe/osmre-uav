package gov.geoplatform.uasdm.odm;

public class ODMResponse
{
  HTTPResponse http;
  
  public ODMResponse(HTTPResponse httpResp)
  {
    this.http = httpResp;
  }
  
  public boolean hasError()
  {
    return http.isJSONObject() && http.getJSONObject().has("error");
  }
  
  public HTTPResponse getHTTPResponse()
  {
    return http;
  }
  
  public String getError()
  {
    return http.getJSONObject().getString("error");
  }
}
