package gov.geoplatform.uasdm.controller;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import gov.geoplatform.uasdm.controller.body.RemoveUploadBody;
import gov.geoplatform.uasdm.service.request.UploadService;

@Controller
@CrossOrigin(exposedHeaders = { "Location", "Upload-Offset" })
public class UploadController extends AbstractController
{
  final Logger                log = LoggerFactory.getLogger(UploadController.class);

  private final UploadService uploadService;

  public UploadController(UploadService uploadService)
  {
    this.uploadService = uploadService;

  }

  @RequestMapping( //
      value = { "/tus-upload", "/tus-upload/**" }, //
      method = { RequestMethod.POST, RequestMethod.PATCH, RequestMethod.HEAD, RequestMethod.DELETE, RequestMethod.GET })
  public void upload(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException
  {
    this.uploadService.upload(getSessionId(), servletRequest, servletResponse);
  }

  @GetMapping("/upload/get-task")
  public ResponseEntity<String> getTask(@RequestParam(name = "uploadUrl", required = true) String uploadUrl) throws IOException
  {
    Optional<JSONObject> task = this.uploadService.getTask(getSessionId(), uploadUrl);

    return ResponseEntity.of(task.map(t -> t.toString()));
  }

  @PostMapping("/upload/remove-upload")
  public ResponseEntity<Void> removeUpload(@RequestBody RemoveUploadBody request) throws IOException
  {
    this.uploadService.removeUpload(getSessionId(), request.getUploadUrl());

    return ResponseEntity.ok(null);
  }

}