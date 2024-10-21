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

import java.io.IOException;

import org.json.JSONObject;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.RestResponse;

import gov.geoplatform.uasdm.service.UserAccessEntityService;

@Controller(url = "user-access")
public class UserAccessEntityController
{
  private UserAccessEntityService service;

  public UserAccessEntityController()
  {
    this.service = new UserAccessEntityService();
  }

  @Endpoint(url = "list-users", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF listUsers(ClientRequestIF request, @RequestParamter(name = "componentId") String componentId) throws IOException
  {
    return new RestBodyResponse(service.listUsers(request.getSessionId(), componentId));
  }

  @Endpoint(url = "grant-access", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF grantAccess(ClientRequestIF request, @RequestParamter(name = "componentId") String componentId, @RequestParamter(name = "identifier") String identifier) throws IOException
  {
    JSONObject response = this.service.grantAccess(request.getSessionId(), componentId, identifier);

    return new RestBodyResponse(response);
  }

  @Endpoint(url = "remove-access", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF removeAccess(ClientRequestIF request, @RequestParamter(name = "componentId") String componentId, @RequestParamter(name = "identifier") String identifier) throws IOException
  {
    this.service.removeAccess(request.getSessionId(), componentId, identifier);

    return new RestResponse();
  }

}
