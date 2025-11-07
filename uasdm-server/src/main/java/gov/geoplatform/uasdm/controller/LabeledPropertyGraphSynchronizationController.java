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
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
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

import gov.geoplatform.uasdm.controller.body.SynchronizeLabeledPropertyGraphBody;
import gov.geoplatform.uasdm.controller.body.UpdateRemoteVersionBody;
import gov.geoplatform.uasdm.service.request.IDMLabeledPropertyGraphSynchronizationService;
import net.geoprism.registry.service.request.LabeledPropertyGraphTypeVersionServiceIF;

@RestController
@Validated
@RequestMapping("/labeled-property-graph-synchronization")
public class LabeledPropertyGraphSynchronizationController extends AbstractController
{
  @Autowired
  private IDMLabeledPropertyGraphSynchronizationService service;

  @Autowired
  private LabeledPropertyGraphTypeVersionServiceIF      vService;

  @GetMapping("/page")
  public ResponseEntity<String> page(@NotEmpty @RequestParam String criteria) throws JSONException
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

  @GetMapping("/get-for-organization")
  public ResponseEntity<String> getForOrganization(@NotEmpty @RequestParam String organizationCode) throws JSONException
  {
    JsonArray list = this.service.getForOrganization(getSessionId(), organizationCode);

    return new ResponseEntity<String>(list.toString(), HttpStatus.OK);
  }

  @GetMapping("/get")
  public ResponseEntity<String> get(@NotEmpty @RequestParam String oid) throws JSONException
  {
    JsonObject response = this.service.get(getSessionId(), oid);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping("/roots")
  public ResponseEntity<String> roots(@NotEmpty @RequestParam String oid, @NotEmpty @RequestParam Boolean includeRoot) throws JSONException
  {
    JsonObject response = this.service.roots(getSessionId(), oid, includeRoot);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping("/select")
  public ResponseEntity<String> select(@NotEmpty @RequestParam String oid, @NotEmpty @RequestParam String parentType, @NotEmpty @RequestParam String parentId, @NotEmpty @RequestParam Boolean includeMetadata) throws JSONException
  {
    JsonObject response = this.service.select(getSessionId(), oid, parentType, parentId, includeMetadata);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping("/get-object")
  public ResponseEntity<String> getObject(@NotEmpty @RequestParam String synchronizationId, @NotEmpty @RequestParam String oid) throws JSONException
  {
    JsonObject response = this.service.getObject(getSessionId(), synchronizationId, oid);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping("/apply")
  public ResponseEntity<String> apply(@Valid @RequestBody SynchronizeLabeledPropertyGraphBody body) throws JSONException
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
  public ResponseEntity<Void> execute(@Valid @RequestBody OidBody body)
  {
    this.service.execute(this.getSessionId(), body.getOid());

    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
  }

  @GetMapping("/get-status")
  public ResponseEntity<String> getStatus(@NotEmpty @RequestParam String oid)
  {
    JsonObject response = this.service.getStatus(getSessionId(), oid);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping("/update-remote-version")
  public ResponseEntity<String> updateRemoteVersion(@Valid @RequestBody UpdateRemoteVersionBody body) throws JSONException
  {
    JsonObject response = this.service.updateRemoteVersion(this.getSessionId(), body.getOid(), body.getVersionId(), body.getVersionNumber());

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping("/new-instance")
  public ResponseEntity<String> newInstance() throws JSONException
  {
    JsonObject response = this.service.newInstance(this.getSessionId());

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping("/tile")
  public ResponseEntity<InputStreamResource> tile(@RequestParam Integer x, @RequestParam Integer y, @RequestParam Integer z, @NotEmpty @RequestParam String config) throws JSONException
  {
    JSONObject object = new JSONObject(config);
    object.put("x", x);
    object.put("y", y);
    object.put("z", z);

    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.CONTENT_TYPE, "application/x-protobuf");

    InputStreamResource isr = new InputStreamResource(this.vService.getTile(this.getSessionId(), object));
    return new ResponseEntity<InputStreamResource>(isr, headers, HttpStatus.OK);
  }

  @PostMapping("/create-tiles")
  public ResponseEntity<Void> createTiles(@Valid @RequestBody OidBody body)
  {
    this.vService.createTiles(getSessionId(), body.getOid());

    return new ResponseEntity<Void>(HttpStatus.OK);
  }

}