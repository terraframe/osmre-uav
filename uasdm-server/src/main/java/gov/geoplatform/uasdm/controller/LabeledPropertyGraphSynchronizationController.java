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
package gov.geoplatform.uasdm.controller;

import org.json.JSONException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.RestResponse;

import gov.geoplatform.uasdm.service.LabeledPropertyGraphSynchronizationService;

@Controller(url = "labeled-property-graph-synchronization")
public class LabeledPropertyGraphSynchronizationController
{
  private LabeledPropertyGraphSynchronizationService service;

  public LabeledPropertyGraphSynchronizationController()
  {
    this.service = new LabeledPropertyGraphSynchronizationService();
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF page(ClientRequestIF request, @RequestParamter(name = "criteria") String criteria) throws JSONException
  {
    JsonObject page = this.service.page(request.getSessionId(), JsonParser.parseString(criteria).getAsJsonObject());

    return new RestBodyResponse(page);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get-all")
  public ResponseIF getAll(ClientRequestIF request) throws JSONException
  {
    JsonArray list = this.service.getAll(request.getSessionId());

    return new RestBodyResponse(list);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "apply")
  public ResponseIF apply(ClientRequestIF request, @RequestParamter(name = "sync") String sync) throws JSONException
  {
    JsonObject object = JsonParser.parseString(sync).getAsJsonObject();

    JsonObject response = this.service.apply(request.getSessionId(), object);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "remove")
  public ResponseIF remove(ClientRequestIF request, @RequestParamter(name = "oid") String oid) throws JSONException
  {
    this.service.remove(request.getSessionId(), oid);

    return new RestResponse();
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "execute")
  public ResponseIF execute(ClientRequestIF request, @RequestParamter(name = "oid") String oid) throws JSONException
  {
    this.service.execute(request.getSessionId(), oid);

    return new RestResponse();
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "update-remote-version")
  public ResponseIF updateRemoteVersion(ClientRequestIF request, @RequestParamter(name = "oid", required = true) String oid, @RequestParamter(name = "versionId", required = true) String versionId, @RequestParamter(name = "versionNumber", required = true) Integer versionNumber) throws JSONException
  {
    JsonObject response = this.service.updateRemoteVersion(request.getSessionId(), oid, versionId, versionNumber);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "newInstance")
  public ResponseIF newInstance(ClientRequestIF request) throws JSONException
  {
    JsonObject response = this.service.newInstance(request.getSessionId());

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get")
  public ResponseIF get(ClientRequestIF request, @RequestParamter(name = "oid") String oid) throws JSONException
  {
    JsonObject response = this.service.get(request.getSessionId(), oid);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "roots")
  public ResponseIF roots(ClientRequestIF request, @RequestParamter(name = "oid", required = true) String oid, @RequestParamter(name = "includeRoot", required = true) Boolean includeRoot) throws JSONException
  {
    JsonArray response = this.service.roots(request.getSessionId(), oid, includeRoot);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "children")
  public ResponseIF children(ClientRequestIF request, @RequestParamter(name = "oid", required = true) String oid, @RequestParamter(name = "parentType", required = true) String parentType, @RequestParamter(name = "parentId", required = true) String parentId) throws JSONException
  {
    JsonArray response = this.service.children(request.getSessionId(), oid, parentType, parentId);

    return new RestBodyResponse(response);
  }
}