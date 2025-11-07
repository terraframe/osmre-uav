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

import java.text.ParseException;
import java.util.List;

import org.commongeoregistry.adapter.metadata.OrganizationDTO;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.geoplatform.uasdm.service.request.IDMOrganizationService;
import net.geoprism.registry.model.OrganizationView;
import net.geoprism.registry.view.Page;

@RestController
@Validated
@RequestMapping("/organization")
public class OrganizationController extends AbstractController
{
  @Autowired
  private IDMOrganizationService service;

  @ResponseBody
  @GetMapping("/get-all")
  public ResponseEntity<String> getOrganizations() throws ParseException
  {
    OrganizationDTO[] orgs = this.service.getOrganizations(this.getSessionId(), null);

    JsonArray orgsJson = new JsonArray();
    for (OrganizationDTO org : orgs)
    {
      orgsJson.add(org.toJSON());
    }

    return new ResponseEntity<String>(orgsJson.toString(), HttpStatus.OK);
  }

  @ResponseBody
  @GetMapping("/get")
  public ResponseEntity<String> get(@NotEmpty @RequestParam String code) throws ParseException
  {
    OrganizationDTO[] orgs = this.service.getOrganizations(this.getSessionId(), new String[] { code });

    return new ResponseEntity<String>(orgs[0].toJSON().toString(), HttpStatus.OK);
  }

  @ResponseBody
  @GetMapping("/search")
  public ResponseEntity<String> search(@NotEmpty @RequestParam String text) throws ParseException
  {
    List<OrganizationDTO> orgs = this.service.search(this.getSessionId(), text);

    JsonArray orgsJson = new JsonArray();
    for (OrganizationDTO org : orgs)
    {
      orgsJson.add(org.toJSON());
    }

    return new ResponseEntity<String>(orgsJson.toString(), HttpStatus.OK);
  }

  @GetMapping("/get-children")
  public ResponseEntity<String> getChildren(@RequestParam(required = false) String code, @RequestParam(required = false) Integer pageSize, @RequestParam(required = false) Integer pageNumber)
  {
    JsonObject page = this.service.getChildren(this.getSessionId(), code, pageSize, pageNumber);

    return new ResponseEntity<String>(page.toString(), HttpStatus.OK);
  }

  @GetMapping("/get-ancestor-tree")
  public ResponseEntity<String> getAncestorTree(@RequestParam(required = false) String rootCode, @NotEmpty @RequestParam String code, @RequestParam(required = false) Integer pageSize)
  {
    JsonObject page = this.service.getAncestorTree(this.getSessionId(), rootCode, code, pageSize);

    return new ResponseEntity<String>(page.toString(), HttpStatus.OK);
  }

  @GetMapping("/patch")
  public ResponseEntity<Void> patch()
  {
    this.service.patch(this.getSessionId());

    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  @GetMapping("/page")
  public ResponseEntity<String> page(@RequestParam(required = false) Integer pageSize, @RequestParam(required = false) Integer pageNumber)
  {
    Page<OrganizationView> page = this.service.getPage(this.getSessionId(), pageSize, pageNumber);

    return new ResponseEntity<String>(page.toJSON().toString(), HttpStatus.OK);
  }
}
