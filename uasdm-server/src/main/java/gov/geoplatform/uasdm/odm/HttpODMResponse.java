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

public class HttpODMResponse implements ODMResponse
{
  Response http;
  
  public HttpODMResponse(Response httpResp)
  {
    this.http = httpResp;
  }
  
  @Override
  public boolean hasError()
  {
    return http.isJSONObject() && http.getJSONObject().has("error");
  }
  
  @Override
  public Response getHTTPResponse()
  {
    return http;
  }
  
  @Override
  public String getError()
  {
    return http.getJSONObject().getString("error");
  }
}
