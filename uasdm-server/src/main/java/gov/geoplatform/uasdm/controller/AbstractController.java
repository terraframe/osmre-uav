package gov.geoplatform.uasdm.controller;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import gov.geoplatform.uasdm.remote.RemoteFileMetadata;
import gov.geoplatform.uasdm.remote.RemoteFileObject;
import net.geoprism.registry.controller.RunwaySpringController;

public abstract class AbstractController extends RunwaySpringController
{
  @Autowired
  private ServletContext context;

  public ServletContext getContext()
  {
    return context;
  }

  protected ResponseEntity<InputStreamResource> getRemoteFile(RemoteFileObject file)
  {
    RemoteFileMetadata metadata = file.getObjectMetadata();
    String contentDisposition = metadata.getContentDisposition();

    if (contentDisposition == null)
    {
      contentDisposition = "attachment; filename=\"" + file.getName() + "\"";
    }

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Content-Type", metadata.getContentType());
    httpHeaders.set("Content-Encoding", metadata.getContentEncoding());
    httpHeaders.set("Content-Disposition", contentDisposition);
    httpHeaders.set("Content-Length", Long.toString(metadata.getContentLength()));
    httpHeaders.set("ETag", metadata.getETag());

    if (metadata.getLastModified() != null)
    {
      httpHeaders.setDate("Last-Modified", metadata.getLastModified().getTime());
    }

    return new ResponseEntity<InputStreamResource>(new InputStreamResource(file.getObjectContent()), httpHeaders, HttpStatus.OK);
  }

}
