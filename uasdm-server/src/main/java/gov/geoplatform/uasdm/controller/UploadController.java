package gov.geoplatform.uasdm.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import gov.geoplatform.uasdm.service.request.UploadService;
import net.geoprism.registry.controller.RunwaySpringController;

@Controller
@CrossOrigin(exposedHeaders = { "Location", "Upload-Offset" })
public class UploadController extends RunwaySpringController
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

}