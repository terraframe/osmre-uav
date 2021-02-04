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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.RestResponse;
import com.runwaysdk.request.ServletRequestIF;
import com.runwaysdk.system.SingleActorDTO;

import gov.geoplatform.uasdm.UserInviteDTO;
import gov.geoplatform.uasdm.service.AccountService;
import net.geoprism.GeoprismUserDTO;
import net.geoprism.RoleViewDTO;
import net.geoprism.account.ExternalProfileDTO;

@Controller(url = "uasdm-account")
public class UASDMAccountController
{
  private AccountService service;

  public UASDMAccountController()
  {
    this.service = new AccountService();
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF inviteUser(ClientRequestIF request, ServletRequestIF sr, @RequestParamter(name = "invite") String sInvite, @RequestParamter(name = "roleIds") String roleIds) throws JSONException
  {
    UserInviteDTO.initiate(request, sInvite, roleIds, getBaseUrl(sr));

    return new RestResponse();
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF newInvite(ClientRequestIF request) throws JSONException
  {
    RoleViewDTO[] roles = RoleViewDTO.getRoles(request, null);
    JSONObject user = new JSONObject();
    user.put("newInstance", true);

    JSONArray groups = this.createRoleMap(roles);

    RestResponse response = new RestResponse();
    response.set("user", user);
    response.set("groups", groups);
    response.set("bureaus", this.service.getBureaus(request.getSessionId()));

    return response;
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF newInstance(ClientRequestIF request) throws JSONException
  {
    GeoprismUserDTO user = UserInviteDTO.newUserInst(request);

    return new RestBodyResponse(user);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF inviteComplete(ClientRequestIF request, @RequestParamter(name = "user") String user, @RequestParamter(name = "token") String token) throws JSONException
  {
    this.service.inviteComplete(request.getSessionId(), token, user);

    return new RestResponse();
  }

  public static String getBaseUrl(ServletRequestIF request)
  {
    String scheme = request.getScheme() + "://";
    String serverName = request.getServerName();
    String serverPort = ( request.getServerPort() == 80 ) ? "" : ":" + request.getServerPort();
    String contextPath = request.getContextPath();
    return scheme + serverName + serverPort + contextPath;
  }

  private JSONArray createRoleMap(RoleViewDTO[] roles) throws JSONException
  {
    Map<String, JSONArray> map = new HashMap<String, JSONArray>();

    for (RoleViewDTO role : roles)
    {
      if (!map.containsKey(role.getGroupName()))
      {
        map.put(role.getGroupName(), new JSONArray());
      }

      JSONObject object = new JSONObject();
      object.put(RoleViewDTO.ASSIGNED, role.getAssigned());
      object.put(RoleViewDTO.DISPLAYLABEL, role.getDisplayLabel());
      object.put(RoleViewDTO.ROLEID, role.getRoleId());

      map.get(role.getGroupName()).put(object);
    }

    JSONArray groups = new JSONArray();

    Set<Entry<String, JSONArray>> entries = map.entrySet();

    for (Entry<String, JSONArray> entry : entries)
    {
      JSONObject group = new JSONObject();
      group.put("name", entry.getKey());
      group.put("roles", entry.getValue());

      groups.put(group);
    }
    return groups;
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF page(ClientRequestIF request, @RequestParamter(name = "number") Integer number) throws JSONException
  {
    JSONObject response = this.service.page(request.getSessionId(), GeoprismUserDTO.USERNAME, true, 10, number);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF edit(ClientRequestIF request, @RequestParamter(name = "oid") String oid) throws JSONException
  {
    GeoprismUserDTO user = GeoprismUserDTO.get(request, oid);
    RoleViewDTO[] roles = RoleViewDTO.getRoles(request, user);

    JSONArray groups = this.createRoleMap(roles);

    RestResponse response = new RestResponse();
    response.set("user", this.service.lock(request.getSessionId(), oid));
    response.set("groups", groups);
    response.set("bureaus", this.service.getBureaus(request.getSessionId()));

    return response;
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF remove(ClientRequestIF request, @RequestParamter(name = "oid") String oid) throws JSONException
  {
    this.service.remove(request.getSessionId(), oid);

    return new RestResponse();
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF unlock(ClientRequestIF request, @RequestParamter(name = "oid") String oid) throws JSONException
  {
    this.service.unlock(request.getSessionId(), oid);

    return new RestBodyResponse("");
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF apply(ClientRequestIF request, @RequestParamter(name = "account") String account, @RequestParamter(name = "roleIds") String roleIds) throws JSONException
  {
    JSONObject response = this.service.apply(request.getSessionId(), new JSONObject(account), roleIds);

    return new RestBodyResponse(response);
  }

}
