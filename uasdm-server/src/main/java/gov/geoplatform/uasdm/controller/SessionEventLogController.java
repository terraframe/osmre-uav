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

import java.io.InputStream;

import jakarta.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.geoplatform.uasdm.service.SessionEventService;
import net.geoprism.registry.controller.RunwaySpringController;

@RestController
@Validated
public class SessionEventLogController extends RunwaySpringController
{
  public static final String API_PATH = "session-event";
  
  @Autowired
  private SessionEventService service;

  public SessionEventLogController()
  {
  }

  @GetMapping(API_PATH + "/page")
  public ResponseEntity<?> page(HttpServletRequest request, @RequestParam(required = true) Integer pageNumber, @RequestParam(required = true) Integer pageSize)
  {
    JSONObject page = this.service.page(getSessionId(), pageNumber, pageSize);

    return new ResponseEntity<String>(page.toString(), HttpStatus.OK);
  }
  
  @GetMapping(API_PATH + "/export")
  public ResponseEntity<?> export(HttpServletRequest request)
  {
    InputStream is = this.service.export(getSessionId());

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Content-Type", "application/zip");
    httpHeaders.set("Content-Disposition", "attachment; filename=\"idm-session-log.zip\"");
    
    return new ResponseEntity<InputStreamResource>(new InputStreamResource(is), httpHeaders, HttpStatus.OK);
  }
}