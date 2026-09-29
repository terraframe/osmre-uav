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

import java.io.InputStream;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import gov.geoplatform.uasdm.service.request.SessionEventService;

@RestController
@Validated
@RequestMapping("/api/session-event")
public class SessionEventLogController extends AbstractController
{
  @Autowired
  private SessionEventService service;

  @GetMapping("/page")
  public ResponseEntity<String> page(@RequestParam(required = true, name = "pageNumber") Integer pageNumber, @RequestParam(required = true, name = "pageSize") Integer pageSize)
  {
    JSONObject page = this.service.page(getSessionId(), pageNumber, pageSize);

    return new ResponseEntity<String>(page.toString(), HttpStatus.OK);
  }

  @GetMapping("/export")
  public ResponseEntity<StreamingResponseBody> export()
  {
    StreamingResponseBody responseBody = ostream -> this.service.export(getSessionId(), ostream);

    return ResponseEntity.ok() //
        .header(HttpHeaders.CONTENT_TYPE, "application/zip") //
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"idm-session-log.zip\"") //
        .body(responseBody);

  }
}