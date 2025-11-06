package gov.geoplatform.uasdm.controller;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import gov.geoplatform.uasdm.service.request.CrudService;
import net.geoprism.registry.controller.RunwaySpringController;

public abstract class AbstractCrudController extends RunwaySpringController
{
  public abstract CrudService getService();

  @GetMapping("/page")
  public ResponseEntity<String> page(@RequestParam(name = "criteria") String criteria)
  {
    JSONObject page = this.getService().page(this.getSessionId(), new JSONObject(criteria));

    return ResponseEntity.ok(page.toString());
  }

  @GetMapping("/get-all")
  public ResponseEntity<String> getAll()
  {
    JSONArray list = this.getService().getAll(this.getSessionId());

    return ResponseEntity.ok(list.toString());
  }

  @PostMapping("/apply")
  public ResponseEntity<String> apply(@RequestBody String classificationJSON)
  {
    JSONObject classification = new JSONObject(classificationJSON);

    JSONObject response = this.getService().apply(this.getSessionId(), classification);

    return ResponseEntity.ok(response.toString());
  }

  @PostMapping("/remove")
  public ResponseEntity<Void> remove(@RequestBody OidBody body)
  {
    this.getService().remove(this.getSessionId(), body.getOid());

    return ResponseEntity.ok(null);
  }

  @PostMapping("/new-instance")
  public ResponseEntity<String> newInstance()
  {
    JSONObject response = this.getService().newInstance(this.getSessionId());

    return ResponseEntity.ok(response.toString());
  }

  @GetMapping("/get")
  public ResponseEntity<String> get(@RequestParam(name = "oid") String oid)
  {
    JSONObject response = this.getService().get(this.getSessionId(), oid);

    return ResponseEntity.ok(response.toString());
  }

}
