package gov.geoplatform.uasdm.odm;

public class NewResponse extends ODMResponse
{

  public NewResponse(HTTPResponse httpResp)
  {
    super(httpResp);
  }

  public String getUUID()
  {
    return http.getJSONObject().getString("uuid");
  }
  
}
