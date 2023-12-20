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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.geoplatform.uasdm.service.IDMOrganizationService;
import net.geoprism.registry.controller.RunwaySpringController;
import net.geoprism.registry.model.OrganizationView;
import net.geoprism.registry.view.Page;

@RestController
@Validated
public class OrganizationController extends RunwaySpringController
{
  public static class MoveOrganizationBody
  {
    @NotEmpty
    String code;

    @NotEmpty
    String parentCode;

    public String getCode()
    {
      return code;
    }

    public void setCode(String code)
    {
      this.code = code;
    }

    public String getParentCode()
    {
      return parentCode;
    }

    public void setParentCode(String parentCode)
    {
      this.parentCode = parentCode;
    }
  }

  public static final String     API_PATH = "organization";

  @Autowired
  private IDMOrganizationService service;

  @ResponseBody
  @GetMapping(API_PATH + "/get-all")
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
  @GetMapping(API_PATH + "/get")
  public ResponseEntity<String> get(@NotEmpty @RequestParam String code) throws ParseException
  {
    OrganizationDTO[] orgs = this.service.getOrganizations(this.getSessionId(), new String[] { code });

    return new ResponseEntity<String>(orgs[0].toJSON().toString(), HttpStatus.OK);
  }

  @ResponseBody
  @GetMapping(API_PATH + "/search")
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

  @GetMapping(API_PATH + "/get-children")
  public ResponseEntity<String> getChildren(@RequestParam(required = false) String code, @RequestParam(required = false) Integer pageSize, @RequestParam(required = false) Integer pageNumber)
  {
    JsonObject page = this.service.getChildren(this.getSessionId(), code, pageSize, pageNumber);

    return new ResponseEntity<String>(page.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get-ancestor-tree")
  public ResponseEntity<String> getAncestorTree(@RequestParam(required = false) String rootCode, @NotEmpty @RequestParam String code, @RequestParam(required = false) Integer pageSize)
  {
    JsonObject page = this.service.getAncestorTree(this.getSessionId(), rootCode, code, pageSize);

    return new ResponseEntity<String>(page.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/patch")
  public ResponseEntity<Void> patch()
  {
    this.service.patch(this.getSessionId());

    return new ResponseEntity<Void>(HttpStatus.OK);
  }
  
  @GetMapping(API_PATH + "/page")
  public ResponseEntity<String> page(@RequestParam(required = false) Integer pageSize, @RequestParam(required = false) Integer pageNumber)
  {
    Page<OrganizationView> page = this.service.getPage(this.getSessionId(), pageSize, pageNumber);

    return new ResponseEntity<String>(page.toJSON().toString(), HttpStatus.OK);
  }
}
