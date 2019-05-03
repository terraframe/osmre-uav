package gov.geoplatform.uasdm.odm;

public class TaskRemoveResponse extends ODMResponse
{

  public TaskRemoveResponse(HTTPResponse httpResp)
  {
    super(httpResp);
  }
  
  public boolean isSuccess()
  {
    return !this.getHTTPResponse().isError() && !this.hasError() && this.getHTTPResponse().getJSONObject().getBoolean("success") == true;
  }
  
}
