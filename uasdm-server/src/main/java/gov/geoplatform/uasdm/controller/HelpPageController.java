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

import com.google.gson.JsonObject;

import gov.geoplatform.uasdm.service.request.HelpPageService;

@RestController
@Validated
@RequestMapping("/api/help")
public class HelpPageController extends AbstractController
{
  @Autowired
  private HelpPageService service;

  @GetMapping("/content")
  public ResponseEntity<String> content(@RequestParam(name = "orgCode", required = false) String orgCode)
  {
    JsonObject json = this.service.content(this.getSessionId(), orgCode);

    return new ResponseEntity<String>(json.toString(), HttpStatus.OK);
  }
  
  public static class HelpEditRequest {

      private String orgCode;
      private String content;
  
      public String getOrgCode() {
          return orgCode;
      }
  
      public void setOrgCode(String orgCode) {
          this.orgCode = orgCode;
      }
  
      public String getContent() {
          return content;
      }
  
      public void setContent(String content) {
          this.content = content;
      }
  }
  
  @PostMapping("/edit")
  public ResponseEntity<Void> edit(@RequestBody HelpEditRequest request)
  {
    this.service.edit(this.getSessionId(), request.getOrgCode(), request.getContent());

    return new ResponseEntity<Void>(HttpStatus.OK);
  }
}
