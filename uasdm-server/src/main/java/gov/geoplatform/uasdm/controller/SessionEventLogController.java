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
package gov.geoplatform.uasdm.controller;

import org.json.JSONException;
import org.json.JSONObject;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;

import gov.geoplatform.uasdm.service.SessionEventService;

@Controller(url = "session-event")
public class SessionEventLogController
{
  private SessionEventService service;

  public SessionEventLogController()
  {
    this.service = new SessionEventService();
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF page(ClientRequestIF request, @RequestParamter(name = "pageNumber") Integer pageNumber, @RequestParamter(name = "pageSize") Integer pageSize) throws JSONException
  {
    JSONObject page = this.service.page(request.getSessionId(), pageNumber, pageSize);

    return new RestBodyResponse(page);
  }
}