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

import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.gson.JsonObject;

import gov.geoplatform.uasdm.service.ErosService;

@Controller
@Validated
@RequestMapping("/api/eros")
public class ErosController extends AbstractController
{
  private ErosService service;

  public ErosController()
  {
    this.service = new ErosService();
  }

  @PostMapping("/push")
  public ResponseEntity<String> push(@RequestParam String collectionId) throws JSONException
  {
    JsonObject response = this.service.push(getSessionId(), collectionId);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }
}