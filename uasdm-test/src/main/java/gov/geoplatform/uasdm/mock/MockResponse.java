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
package gov.geoplatform.uasdm.mock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gov.geoplatform.uasdm.odm.Response;

public class MockResponse implements Response
{
  private int statusCode = 200;

  private String response = "Mock Message";

  @Override
  public boolean isJSONArray()
  {
    return false;
  }

  @Override
  public boolean isJSONObject()
  {
    return false;
  }

  @Override
  public JSONObject getJSONObject() throws JSONException
  {
    return null;
  }

  @Override
  public JSONArray getJSONArray() throws JSONException
  {
    return null;
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
  public int getStatusCode()
  {
    return this.statusCode;
  }

  @Override
  public void setStatusCode(int statusCode)
  {
    this.statusCode = statusCode;
  }

  @Override
  public boolean isError()
  {
    return false;
  }

  @Override
  public boolean isUnreachableHost()
  {
    return false;
  }

}
