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
    JSONObject json = http.getJSONObject();
    
    if (json == null || !json.has("uuid"))
    {
      return null;
    }
    else
    {
      return json.getString("uuid");
    }
  }
  
  public Date getDateCreated()
  {
    JSONObject json = http.getJSONObject();
    
    if (json == null || !json.has("dateCreated"))
    {
      return null;
    }
    else
    {
      return new Date(json.getLong("dateCreated"));
    }
  }
  
  public Long getProcessingTime()
  {
    JSONObject json = http.getJSONObject();
    
    if (json == null || !json.has("processingTime"))
    {
      return -1L;
    }
    else
    {
      return json.getLong("processingTime");
    }
  }
  
  public Long getImagesCount()
  {
    JSONObject json = http.getJSONObject();
    
    if (json == null || !json.has("imagesCount"))
    {
      return -1L;
    }
    else
    {
      return json.getLong("imagesCount");
    }
  }
  
  public JSONObject getOptions()
  {
    return http.getJSONObject().getJSONObject("options");
  }
  
  public ODMStatus getStatus()
  {
    JSONObject json = http.getJSONObject();
    
    if (json == null || !json.has("status"))
    {
      return null;
    }
    
    JSONObject status = json.getJSONObject("status");
    
    if (status == null || !status.has("code"))
    {
      return null;
    }
    else
    {
      return ODMStatus.getByCode(status.getInt("code"));
    }
  }
  
  public String getStatusError()
  {
    JSONObject json = http.getJSONObject();
    
    if (json == null || !json.has("status"))
    {
      return null;
    }
    
    JSONObject status = json.getJSONObject("status");
    
    if (status == null || !status.has("errorMessage"))
    {
      return null;
    }
    else
    {
      return status.getString("errorMessage");
    }
  }

}
