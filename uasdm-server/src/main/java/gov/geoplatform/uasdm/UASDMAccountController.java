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
package gov.geoplatform.uasdm;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.ParseType;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.RestResponse;
import com.runwaysdk.request.ServletRequestIF;

import net.geoprism.GeoprismUserDTO;
import net.geoprism.RoleViewDTO;

@Controller(url = "uasdm-account")
public class UASDMAccountController
{
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF inviteUser(ClientRequestIF request, ServletRequestIF sr, @RequestParamter(name = "invite") String sInvite, @RequestParamter(name = "roleIds") String roleIds) throws JSONException
  {
    UserInviteDTO.initiate(request, sInvite, roleIds, getBaseUrl(sr));
    
    return new RestResponse();
  }
  
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF newInvite(ClientRequestIF request, ServletRequestIF sr, @RequestParamter(name = "invite") UserInviteDTO invite) throws JSONException
  {
    RoleViewDTO[] roles = RoleViewDTO.getRoles(request, new GeoprismUserDTO(request));

    JSONArray groups = this.createRoleMap(roles);

    RestResponse response = new RestResponse();
    response.set("email", "");
    response.set("groups", groups);

    return response;
  }
  
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF newInstance(ClientRequestIF request) throws JSONException
  {
    GeoprismUserDTO user = UserInviteDTO.newUserInst(request);

    return new RestBodyResponse(user);
  }
  
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF inviteComplete(ClientRequestIF request, @RequestParamter(name = "user", parser = ParseType.BASIC_JSON) GeoprismUserDTO user, @RequestParamter(name = "token") String token) throws JSONException
  {
    UserInviteDTO.complete(request, token, user);
    
    return new RestResponse();
  }
  
  public static String getBaseUrl(ServletRequestIF request) {
    String scheme = request.getScheme() + "://";
    String serverName = request.getServerName();
    String serverPort = (request.getServerPort() == 80) ? "" : ":" + request.getServerPort();
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
}
