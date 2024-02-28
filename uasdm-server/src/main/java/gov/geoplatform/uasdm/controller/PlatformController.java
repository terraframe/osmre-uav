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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import gov.geoplatform.uasdm.service.PlatformService;
import net.geoprism.registry.controller.RunwaySpringController;

@Controller
@Validated
public class PlatformController extends RunwaySpringController
{
  public static final String API_PATH = "platform";
  
  public static class ApplyBody
  {
    @NotEmpty
    String platform;

    public String getPlatform()
    {
      return platform;
    }

    public void setPlatform(String platform)
    {
      this.platform = platform;
    }
  }
  
  @Autowired
  private PlatformService service;

  public PlatformController()
  {
  }

  @GetMapping(API_PATH + "/page")
  public ResponseEntity<String> page(@RequestParam(name = "criteria") String criteria) throws JSONException
  {
    JSONObject page = this.service.page(getSessionId(), new JSONObject(criteria));

    return new ResponseEntity<String>(page.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get-all")
  public ResponseEntity<String> getAll() throws JSONException
  {
    JSONArray list = this.service.getAll(getSessionId());

    return new ResponseEntity<String>(list.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/apply")
  public ResponseEntity<String> apply(@Valid @RequestBody ApplyBody body) throws JSONException
  {
    JSONObject platform = new JSONObject(body.platform);

    JSONObject response = this.service.apply(getSessionId(), platform);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/remove")
  @ResponseBody
  public void remove(@RequestParam(name = "oid") String oid) throws JSONException
  {
    this.service.remove(getSessionId(), oid);
  }

  @GetMapping(API_PATH + "/newInstance")
  public ResponseEntity<String> newInstance() throws JSONException
  {
    JSONObject response = this.service.newInstance(getSessionId());

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get")
  public ResponseEntity<String> get(@RequestParam(name = "oid") String oid) throws JSONException
  {
    JSONObject response = this.service.get(getSessionId(), oid);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/search")
  public ResponseEntity<String> search(@RequestParam(name = "text") String text)
  {
    JSONArray response = this.service.search(getSessionId(), text);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

}