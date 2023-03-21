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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.request.ServletRequestIF;
import com.runwaysdk.system.SingleActorDTO;

import gov.geoplatform.uasdm.UserInviteDTO;
import gov.geoplatform.uasdm.service.AccountService;
import net.geoprism.GeoprismUserDTO;
import net.geoprism.account.ExternalProfileDTO;
import net.geoprism.account.GeoprismUserView;
import net.geoprism.rbac.RoleServiceIF;
import net.geoprism.rbac.RoleView;
import net.geoprism.registry.controller.RunwaySpringController;
import net.geoprism.spring.JsonArrayDeserializer;
import net.geoprism.spring.JsonObjectDeserializer;

@RestController
@Validated
public class UASDMAccountController extends RunwaySpringController
{
  public static final String API_PATH = "uasdm-account";
  
  protected AccountService service;
  
  @Autowired
  protected RoleServiceIF roleService;
  
  public static class InviteBody
  {
    @NotNull
    @JsonDeserialize(using = JsonObjectDeserializer.class)
    JsonObject invite;

    @JsonDeserialize(using = JsonArrayDeserializer.class)
    JsonArray  roleIds;
    
    public InviteBody()
    {
      
    }

    public JsonObject getInvite()
    {
      return invite;
    }

    public void setInvite(JsonObject invite)
    {
      this.invite = invite;
    }

    public JsonArray getRoleIds()
    {
      return roleIds;
    }

    public void setRoleIds(JsonArray roleIds)
    {
      this.roleIds = roleIds;
    }
  }
  
  public static class InviteCompleteBody
  {
    @NotNull
    @JsonDeserialize(using = JsonObjectDeserializer.class)
    JsonObject user;

    @NotEmpty
    String     token;
    
    public InviteCompleteBody()
    {
      
    }

    public JsonObject getUser()
    {
      return user;
    }

    public void setUser(JsonObject user)
    {
      this.user = user;
    }

    public String getToken()
    {
      return token;
    }

    public void setToken(String token)
    {
      this.token = token;
    }
  }
  
  public static class ApplyBody
  {
    @NotEmpty
    @JsonDeserialize(using = JsonObjectDeserializer.class)
    private JsonObject account;
    
    @Nullable
    @JsonDeserialize(using = JsonArrayDeserializer.class)
    private JsonArray roleIds;
    
    public ApplyBody()
    {
      
    }

    public JsonObject getAccount()
    {
      return account;
    }

    public void setAccount(JsonObject account)
    {
      this.account = account;
    }

    public JsonArray getRoleIds()
    {
      return roleIds;
    }

    public void setRoleIds(JsonArray roleIds)
    {
      this.roleIds = roleIds;
    }
  }

  public UASDMAccountController()
  {
    this.service = new AccountService();
  }
  
  @GetMapping(API_PATH + "/get")
  public GeoprismUserView get() throws JSONException
  {
    SingleActorDTO user = GeoprismUserDTO.getCurrentUser(this.getClientRequest());
    
    if (!(user instanceof ExternalProfileDTO))
    {
      return GeoprismUserView.fromGeoprismUserDTO((GeoprismUserDTO) user);
    }
    else
    {
      ExternalProfileDTO ep = (ExternalProfileDTO) user;
      
      return new GeoprismUserView(ep.getDisplayName(), ep.getEmail(), ep.getFirstName(), ep.getLastName(), ep.getPhoneNumber(), ep.getOid());
    }
  }

  @PostMapping(API_PATH + "/inviteUser")
  public void inviteUser(@RequestBody InviteBody body) throws JSONException
  {
    UserInviteDTO.initiate(this.getClientRequest(), body.getInvite().toString(), body.getRoleIds().toString(), getBaseUrl());
  }

  @PostMapping(API_PATH + "/newInvite")
  public ResponseEntity<String> newInvite() throws JSONException
  {
    List<RoleView> roles = this.roleService.getAllAssignableRoles(this.getSessionId());
    
    JSONObject user = new JSONObject();
    user.put("newInstance", true);

    JSONArray groups = this.createRoleMap(roles);

    JSONObject response = new JSONObject();
    response.put("user", user);
    response.put("groups", groups);
    response.put("bureaus", this.service.getBureaus(this.getClientRequest().getSessionId()));

    return new ResponseEntity<String>(response.toString(), HttpStatus.CREATED);
  }

  @PostMapping(API_PATH + "/inviteComplete")
  public void inviteComplete(@RequestBody InviteCompleteBody body) throws JSONException
  {
    this.service.inviteComplete(this.getClientRequest().getSessionId(), body.getToken(), body.getUser().toString());
  }

  public String getBaseUrl()
  {
    String scheme = this.getRequest().getScheme() + "://";
    String serverName = this.getRequest().getServerName();
    String serverPort = ( this.getRequest().getServerPort() == 80 ) ? "" : ":" + this.getRequest().getServerPort();
    String contextPath = this.getRequest().getContextPath();
    return scheme + serverName + serverPort + contextPath;
  }

  private JSONArray createRoleMap(List<RoleView> roles) throws JSONException
  {
    final String groupName = "adminRoles";
    
    Map<String, JSONArray> map = new HashMap<String, JSONArray>();

    for (RoleView role : roles)
    {
      if (!map.containsKey(groupName))
      {
        map.put(groupName, new JSONArray());
      }

      JSONObject object = new JSONObject();
      object.put("assigned", role.getAssigned());
      object.put("displayLabel", role.getDisplayLabel());
      object.put("roleId", role.getOid());

      map.get(groupName).put(object);
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

  @GetMapping(API_PATH + "/page")
  public ResponseEntity<String> page(@NotEmpty @RequestParam String criteria) throws JSONException
  {
    JSONObject response = this.service.page(this.getClientRequest().getSessionId(), new JSONObject(criteria));

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/edit")
  public ResponseEntity<String> edit(@RequestBody String oid) throws JSONException
  {
    List<RoleView> roles = this.roleService.getAllAssignableRoles(this.getSessionId(), oid);

    JSONArray groups = this.createRoleMap(roles);

    JSONObject response = new JSONObject();
    response.put("user", this.service.get(this.getClientRequest().getSessionId(), oid));
    response.put("groups", groups);
    response.put("bureaus", this.service.getBureaus(this.getClientRequest().getSessionId()));

    return new ResponseEntity<String>(response.toString(), HttpStatus.CREATED);
  }

  @PostMapping(API_PATH + "/remove")
  public void remove(@RequestBody String oid) throws JSONException
  {
    this.service.remove(this.getClientRequest().getSessionId(), oid);
  }

  @PostMapping(API_PATH + "/apply")
  public ResponseEntity<String> apply(@RequestBody ApplyBody body) throws JSONException
  {
    final String roleIds = body.getRoleIds() == null ? null : body.getRoleIds().toString();
    
    JSONObject response = this.service.apply(this.getClientRequest().getSessionId(), new JSONObject(body.getAccount().toString()), roleIds);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

}
