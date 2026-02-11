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
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.hibernate.validator.constraints.NotEmpty;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.UserInviteDTO;
import gov.geoplatform.uasdm.controller.body.UserBody;
import gov.geoplatform.uasdm.controller.body.CompleteUserInviteBody;
import gov.geoplatform.uasdm.controller.body.UploadUsersBody;
import gov.geoplatform.uasdm.controller.body.UserInviteBody;
import gov.geoplatform.uasdm.service.request.AccountService;
import net.geoprism.account.GeoprismUserView;
import net.geoprism.rbac.RoleView;
import net.geoprism.registry.service.request.OrganizationServiceIF;
import net.geoprism.registry.service.request.RoleServiceIF;

@RestController
@Validated
@RequestMapping("/api/uasdm-account")
public class UASDMAccountController extends AbstractController
{
  @Autowired
  protected AccountService        service;

  @Autowired
  protected RoleServiceIF         roleService;

  @Autowired
  protected OrganizationServiceIF orgService;

  @GetMapping("/get")
  public GeoprismUserView get() throws JSONException
  {
    return this.service.getCurrentUser(getSessionId());
  }

  @PostMapping("/uploadUsers")
  public void uploadUsers(@Valid @ModelAttribute UploadUsersBody body) throws IOException
  {
    this.service.uploadUsers(getSessionId(), body.getFile().getInputStream(), body.getFile().getName());
  }

  @PostMapping("/inviteUser")
  public void inviteUser(@RequestBody UserInviteBody body) throws JSONException
  {
    UserInviteDTO.initiate(this.getClientRequest(), body.getInvite().toString(), body.getRoleIds().toString(), getBaseUrl());
  }

  @PostMapping("/newInvite")
  public ResponseEntity<String> newInvite() throws JSONException
  {
    List<RoleView> roles = this.roleService.getAllAssignableRoles(this.getSessionId());

    final String defaultRole = "geoprism.admin.DashboardBuilder"; // IDM's
                                                                  // fieldworker
                                                                  // role
    roles.stream().forEach(role -> role.setAssigned(role.getRoleName().equals(defaultRole)));

    JSONObject user = new JSONObject();
    user.put("newInstance", true);

    if (AppProperties.requireKeycloakLogin())
    {
      user.put("externalProfile", true);
    }

    JSONArray groups = this.createRoleMap(roles);

    JSONObject response = new JSONObject();
    response.put("user", user);
    response.put("groups", groups);

    return new ResponseEntity<String>(response.toString(), HttpStatus.CREATED);
  }

  @PostMapping("/inviteComplete")
  public void inviteComplete(@RequestBody CompleteUserInviteBody body) throws JSONException
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

  @GetMapping("/page")
  public ResponseEntity<String> page(@NotEmpty @RequestParam(name = "criteria") String criteria) throws JSONException
  {
    JSONObject response = this.service.page(this.getClientRequest().getSessionId(), new JSONObject(criteria));

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping("/export")
  public ResponseEntity<?> export(HttpServletRequest request)
  {
    InputStream is = this.service.export(getSessionId());

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Content-Type", "application/zip");
    httpHeaders.set("Content-Disposition", "attachment; filename=\"idm-users.zip\"");

    return new ResponseEntity<InputStreamResource>(new InputStreamResource(is), httpHeaders, HttpStatus.OK);
  }

  @PostMapping("/edit")
  public ResponseEntity<String> edit(@RequestBody String oid) throws JSONException
  {
    List<RoleView> roles = this.roleService.getAllAssignableRoles(this.getSessionId(), oid);

    JSONArray groups = this.createRoleMap(roles);

    JSONObject response = new JSONObject();
    response.put("user", this.service.get(this.getClientRequest().getSessionId(), oid));
    response.put("groups", groups);

    return new ResponseEntity<String>(response.toString(), HttpStatus.CREATED);
  }

  @PostMapping("/remove")
  public void remove(@RequestBody String oid) throws JSONException
  {
    this.service.remove(this.getClientRequest().getSessionId(), oid);
  }

  @PostMapping("/apply")
  public ResponseEntity<String> apply(@RequestBody UserBody body) throws JSONException
  {
    final String roleIds = body.getRoleIds() == null ? null : body.getRoleIds().toString();

    JSONObject response = this.service.apply(this.getClientRequest().getSessionId(), new JSONObject(body.getAccount().toString()), roleIds);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

}
