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

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.geoplatform.uasdm.service.ODMRunService;

@RestController
@Validated
@RequestMapping("/api/odmrun")
public class ODMRunController extends AbstractController
{
  @Autowired
  private ODMRunService service;

  @GetMapping("/estimateRuntime")
  public ResponseEntity<String> estimateRuntime(@RequestParam(name = "collectionId") String collectionId, @RequestParam(name = "configJson") String configJson)
  {
    JSONObject response = service.estimateRuntime(this.getSessionId(), collectionId, configJson);

    return ResponseEntity.ok(response.toString());
  }
}
