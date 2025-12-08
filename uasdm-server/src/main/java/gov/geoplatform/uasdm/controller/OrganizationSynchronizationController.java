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

import javax.validation.Valid;

import org.hibernate.validator.constraints.NotEmpty;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import gov.geoplatform.uasdm.controller.body.SynchronizeOrganizationBody;
import gov.geoplatform.uasdm.service.OrganizationSynchronizationService;

@RestController
@Validated
@RequestMapping("/api/organization-synchronization")
public class OrganizationSynchronizationController extends AbstractController
{
  @Autowired
  private OrganizationSynchronizationService service;

  @GetMapping("/page")
  public ResponseEntity<String> page(@NotEmpty @RequestParam(name = "criteria") String criteria) throws JSONException
  {
    JsonObject page = this.service.page(getSessionId(), JsonParser.parseString(criteria).getAsJsonObject());

    return new ResponseEntity<String>(page.toString(), HttpStatus.OK);
  }

  @GetMapping("/get-all")
  public ResponseEntity<String> getAll() throws JSONException
  {
    JsonArray list = this.service.getAll(getSessionId());

    return new ResponseEntity<String>(list.toString(), HttpStatus.OK);
  }

  @GetMapping("/get")
  public ResponseEntity<String> get(@NotEmpty @RequestParam(name = "oid") String oid) throws JSONException
  {
    JsonObject response = this.service.get(getSessionId(), oid);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping("/apply")
  public ResponseEntity<String> apply(@Valid @RequestBody SynchronizeOrganizationBody body) throws JSONException
  {
    JsonObject response = this.service.apply(this.getSessionId(), body.getSync());

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping("/remove")
  public ResponseEntity<Void> remove(@Valid @RequestBody OidBody body) throws JSONException
  {
    this.service.remove(this.getSessionId(), body.getOid());

    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
  }

  @PostMapping("/execute")
  public ResponseEntity<Void> execute(@Valid @RequestBody OidBody body) throws JSONException
  {
    this.service.execute(this.getSessionId(), body.getOid());

    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
  }

  @PostMapping("/new-instance")
  public ResponseEntity<String> newInstance() throws JSONException
  {
    JsonObject response = this.service.newInstance(this.getSessionId());

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }
}