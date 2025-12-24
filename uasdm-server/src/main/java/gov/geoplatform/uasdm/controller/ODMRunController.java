package gov.geoplatform.uasdm.controller;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.geoplatform.uasdm.service.ODMRunService;

@RestController
@Validated
@RequestMapping("/api/odmrun")
public class ODMRunController extends AbstractController
{
  @Autowired
  private ODMRunService service;

  @GetMapping("/estimateRuntime")
  public ResponseEntity<String> estimateRuntime(@RequestParam(name = "collectionId") String collectionId, @RequestParam(name = "configJson") String configJson)
  {
    JSONObject response = service.estimateRuntime(this.getSessionId(), collectionId, configJson);

    return ResponseEntity.ok(response.toString());
  }
}
