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
/**
 * Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package gov.geoplatform.uasdm.odm;

import org.apache.commons.httpclient.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HttpResponse implements Response
{
  private String response;
  
  private int statusCode;
  
  public HttpResponse(String response, int statusCode)
  {
    this.response = response;
    this.statusCode = statusCode;
  }
  
  @Override
  public boolean isJSONArray()
  {
    if (this.response != null && this.response.length() > 0)
    {
      try
      {
        new JSONArray(response);
        
        return true;
      }
      catch(JSONException e)
      {
        
      }
    }
    
    return false;
  }
  
  @Override
  public boolean isJSONObject()
  {
    if (this.response != null && this.response.length() > 0)
    {
      try
      {
        new JSONObject(response);
        
        return true;
      }
      catch(JSONException e)
      {
        
      }
    }
    
    return false;
  }

  @Override
  public JSONObject getJSONObject() throws JSONException
  {
    if (response == null)
    {
      throw new UnexpectedODMResponseException(null);
    }
    
    return new JSONObject(response);
  }
  
  @Override
  public JSONArray getJSONArray() throws JSONException
  {
    if (response == null)
    {
      throw new UnexpectedODMResponseException(null);
    }
    
    return new JSONArray(response);
  }

  @Override
  public String getResponse()
  {
    return this.response;
  }
  
  @Override
  public void setResponse(String response)
  {
    this.response = response;
  }

  @Override
  public int getStatusCode() {
    return statusCode;
  }

  @Override
  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }
  
  @Override
  public boolean isError()
  {
    if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_CREATED)
    {
      return false;
    }
    
    return true;
  }

  @Override
  public boolean isUnreachableHost()
  {
    return statusCode == HttpStatus.SC_FORBIDDEN || statusCode == HttpStatus.SC_BAD_GATEWAY || statusCode == HttpStatus.SC_GATEWAY_TIMEOUT || statusCode == HttpStatus.SC_NOT_FOUND;
  }
}
