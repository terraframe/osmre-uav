package gov.geoplatform.uasdm.odm;

import java.util.Date;

import org.json.JSONObject;

public class InfoResponse extends ODMResponse
{

  public InfoResponse(HTTPResponse httpResp)
  {
    super(httpResp);
  }
  
  public String getUUID()
  {
    return http.getJSONObject().getString("uuid");
  }
  
  public Date getDateCreated()
  {
    return new Date(http.getJSONObject().getLong("dateCreated"));
  }
  
  public Long getProcessingTime()
  {
    return http.getJSONObject().getLong("processingTime");
  }
  
  public Long getImagesCount()
  {
    return http.getJSONObject().getLong("imagesCount");
  }
  
  public JSONObject getOptions()
  {
    return http.getJSONObject().getJSONObject("options");
  }
  
  public ODMStatus getStatus()
  {
    JSONObject status = http.getJSONObject().getJSONObject("status");
    
    return ODMStatus.getByCode(status.getInt("code"));
  }
  
  public String getStatusError()
  {
    JSONObject status = http.getJSONObject().getJSONObject("status");
    
    return status.getString("errorMessage");
  }

}
