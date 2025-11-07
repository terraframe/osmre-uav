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

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import gov.geoplatform.uasdm.service.CollectionReportService;

@Controller
@Validated
@RequestMapping("/collection-report")
public class CollectionReportController extends AbstractController
{
  @Autowired
  private CollectionReportService service;

  @GetMapping("/page")
  public ResponseEntity<String> page(@RequestParam(required = true) String criteria) throws JSONException
  {
    JSONObject page = this.service.page(getSessionId(), new JSONObject(criteria));

    return new ResponseEntity<String>(page.toString(), HttpStatus.OK);
  }

  @GetMapping("/export-csv")
  public ResponseEntity<InputStreamResource> exportCSV(@RequestParam(required = false) String criteria) throws JSONException
  {
    JSONObject json = criteria != null ? new JSONObject(criteria) : null;

    InputStream inputStream = this.service.exportCSV(getSessionId(), json);
    InputStreamResource inputStreamResource = new InputStreamResource(inputStream);

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Content-Type", "text/csv");
    httpHeaders.set("Content-disposition", "attachment; filename=report.csv");
    // httpHeaders.setContentLength(contentLengthOfStream);

    return new ResponseEntity<InputStreamResource>(inputStreamResource, httpHeaders, HttpStatus.OK);
  }
}