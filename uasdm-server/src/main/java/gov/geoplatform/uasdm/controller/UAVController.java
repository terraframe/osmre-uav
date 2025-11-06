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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.geoplatform.uasdm.service.request.CrudService;
import gov.geoplatform.uasdm.service.request.UAVService;

@RestController
@RequestMapping("/uav")
public class UAVController extends AbstractCrudController
{
  @Autowired
  private UAVService service;

  @Override
  public CrudService getService()
  {
    return this.service;
  }

  @GetMapping("/bureaus")
  public ResponseEntity<String> bureaus()
  {
    JSONArray response = this.service.bureaus(this.getSessionId());

    return ResponseEntity.ok(response.toString());
  }

  @GetMapping("/search")
  public ResponseEntity<String> search(@RequestParam(name = "text") String text, @RequestParam(name = "field") String field)
  {
    JSONArray response = this.service.search(this.getSessionId(), text, field);

    return ResponseEntity.ok(response.toString());
  }

  @GetMapping("/get-metadata-options")
  public ResponseEntity<String> getMetadataOptions(@RequestParam(name = "oid") String oid)
  {
    JSONObject response = this.service.getMetadataOptions(this.getSessionId(), oid);

    return ResponseEntity.ok(response.toString());
  }
}