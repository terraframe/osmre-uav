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

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.geoplatform.uasdm.controller.body.UserAccessBody;
import gov.geoplatform.uasdm.service.request.UserAccessEntityService;

@RestController
@RequestMapping("/api/user-access")
public class UserAccessEntityController extends AbstractController
{
  @Autowired
  private UserAccessEntityService service;

  @GetMapping("list-users")
  public ResponseEntity<String> listUsers(@RequestParam(name = "componentId") String componentId)
  {
    JSONArray response = service.listUsers(this.getSessionId(), componentId);

    return ResponseEntity.ok(response.toString());
  }

  @PostMapping("grant-access")
  public ResponseEntity<String> grantAccess(@RequestBody UserAccessBody body)
  {
    JSONObject response = this.service.grantAccess(this.getSessionId(), body.getComponentId(), body.getIdentifier());

    return ResponseEntity.ok(response.toString());
  }

  @PostMapping("remove-access")
  public ResponseEntity<Void> removeAccess(@RequestBody UserAccessBody body)
  {
    this.service.removeAccess(this.getSessionId(), body.getComponentId(), body.getIdentifier());

    return ResponseEntity.ok(null);
  }

}
