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

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import gov.geoplatform.uasdm.service.ProcessingReportService;
import net.geoprism.registry.controller.RunwaySpringController;
import software.amazon.awssdk.utils.IoUtils;

@Controller
@Validated
public class ProcessingReportController extends RunwaySpringController
{
  private static final Logger logger = LoggerFactory.getLogger(ProcessingReportController.class);

  public static final String API_PATH = "processing-report";
  
  @Autowired
  private ProcessingReportService service;
  
  @GetMapping(API_PATH + "/generate")
  public ResponseEntity<ByteArrayResource> generate(HttpServletRequest request, @RequestParam(required = false) String date) throws IOException, ParseException
  {
    Date _date = null;
    if (date != null && !date.trim().equals(""))
    {
      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
      df.setTimeZone(TimeZone.getTimeZone("UTC"));
      _date = df.parse(date);
    }
    
    InputStream is = this.service.generate(getSessionId(), _date);
    
    byte[] bytes = IoUtils.toByteArray(is);
    
    return ResponseEntity.ok()
        .header("Content-Disposition", "attachment; filename=report.csv")
        .contentLength(bytes.length)
        .contentType(MediaType.parseMediaType("text/csv"))
        .body(new ByteArrayResource(bytes));
  }
}
