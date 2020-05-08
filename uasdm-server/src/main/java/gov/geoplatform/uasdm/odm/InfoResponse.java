/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
