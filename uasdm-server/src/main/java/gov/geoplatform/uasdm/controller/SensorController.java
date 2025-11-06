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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.runwaysdk.constants.ClientRequestIF;

import gov.geoplatform.uasdm.service.request.CrudService;
import gov.geoplatform.uasdm.service.request.SensorService;

@RestController
@RequestMapping("/sensor")
public class SensorController extends AbstractCrudController
{
  @Autowired
  private SensorService service;

  @Override
  public CrudService getService()
  {
    return this.service;
  }

  @GetMapping("/search")
  public ResponseEntity<String> search(ClientRequestIF request, @RequestParam(name = "text") String text)
  {
    JSONArray response = this.service.search(request.getSessionId(), text);

    return ResponseEntity.ok(response.toString());
  }

}