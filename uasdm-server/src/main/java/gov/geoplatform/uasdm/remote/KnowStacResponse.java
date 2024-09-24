/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.geoplatform.uasdm.remote;

import gov.geoplatform.uasdm.odm.Response;

public class KnowStacResponse implements KnowStacResponseIF
{
  Response http;

  public KnowStacResponse(Response httpResp)
  {
    this.http = httpResp;
  }

  public boolean hasError()
  {
    return http.getStatusCode() != 200;
  }

  public Response getHTTPResponse()
  {
    return http;
  }

  public String getError()
  {
    // Give the server error message if one exists
    if (http.isJSONObject() && http.getJSONObject().has("localizedMessage"))
    {
      return http.getJSONObject().getString("localizedMessage");
    }

    // Otherwise give a generic exception
    return "A problem occurred while communicating with the GeoPlatform server. Please try your request again later.";
  }
}
