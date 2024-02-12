package gov.geoplatform.uasdm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import gov.geoplatform.uasdm.remote.RemoteFileMetadata;
import gov.geoplatform.uasdm.remote.RemoteFileObject;
import gov.geoplatform.uasdm.service.ProxyService;
import net.geoprism.registry.controller.RunwaySpringController;

@Controller
@Validated
public class ProxyController extends RunwaySpringController
{
  public static final String API_PATH = "proxy";

  @Autowired
  private ProxyService       service;

  @GetMapping(API_PATH + "/file")
  public ResponseEntity<InputStreamResource> file(@RequestParam(required = true) String path)
  {
    RemoteFileObject object = this.service.file(getSessionId(), path);

    RemoteFileMetadata metadata = object.getObjectMetadata();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.valueOf(metadata.getContentType()));
    headers.setContentLength(metadata.getContentLength());
    headers.set(HttpHeaders.CONTENT_ENCODING, metadata.getContentEncoding());
    headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + object.getName() + "\"");
    headers.set(HttpHeaders.ETAG, metadata.getETag());

    if (metadata.getLastModified() != null)
    {
      headers.setDate("Last-Modified", metadata.getLastModified().getTime());
    }

    
    InputStreamResource isr = new InputStreamResource(object.getObjectContent());

    return new ResponseEntity<InputStreamResource>(isr, headers, HttpStatus.OK);
  }
}
