/*******************************************************************************
 * Copyright (C) 2018 IVCC
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
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

public class HTTPResponse
{
  private String response;
  
  private int statusCode;
  
  public HTTPResponse(String response, int statusCode)
  {
    this.response = response;
    this.statusCode = statusCode;
  }
  
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

  public JSONObject getJSONObject() throws JSONException
  {
    return new JSONObject(response);
  }
  
  public JSONArray getJSONArray() throws JSONException
  {
    return new JSONArray(response);
  }

  public String getResponse()
  {
    return this.response;
  }
  
  public void setResponse(String response)
  {
    this.response = response;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }
  
  public boolean isError()
  {
    if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_CREATED)
    {
      return false;
    }
    
    return true;
  }

  public boolean isUnreachableHost()
  {
    return statusCode == HttpStatus.SC_FORBIDDEN || statusCode == HttpStatus.SC_BAD_GATEWAY || statusCode == HttpStatus.SC_GATEWAY_TIMEOUT || statusCode == HttpStatus.SC_NOT_FOUND;
  }
}
