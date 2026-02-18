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
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
      value = { "/api/tus-upload", "/api/tus-upload/**" }, //
      method = { RequestMethod.POST, RequestMethod.PATCH, RequestMethod.HEAD, RequestMethod.DELETE, RequestMethod.GET })
  public void upload(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException
  {
    this.uploadService.upload(getSessionId(), servletRequest, servletResponse);
  }

  @GetMapping("/api/upload/get-task")
  public ResponseEntity<String> getTask(@RequestParam(name = "uploadUrl", required = true) String uploadUrl) throws IOException
  {
    Optional<JSONObject> task = this.uploadService.getTask(getSessionId(), uploadUrl);

    return ResponseEntity.of(task.map(t -> t.toString()));
  }

  @PostMapping("/api/upload/remove-upload")
  public ResponseEntity<Void> removeUpload(@RequestBody RemoveUploadBody request) throws IOException
  {
    this.uploadService.removeUpload(getSessionId(), request.getUploadUrl());

    return ResponseEntity.ok(null);
  }

}