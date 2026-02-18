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

import java.util.Date;

import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.runwaysdk.constants.ClientConstants;
import com.runwaysdk.constants.ClientRequestIF;

import gov.geoplatform.uasdm.remote.RemoteFileMetadata;
import gov.geoplatform.uasdm.remote.RemoteFileObject;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public abstract class AbstractController
{
  public static class OidBody
  {
    @NotBlank
    private String oid;

    public String getOid()
    {
      return oid;
    }

    public void setOid(String oid)
    {
      this.oid = oid;
    }
  }

  @Autowired
  private ServletContext      context;

  @Autowired
  private HttpServletRequest  request;

  @Autowired
  private HttpServletResponse response;

  protected HttpServletRequest getRequest()
  {
    return request;
  }

  protected HttpServletResponse getResponse()
  {
    return response;
  }

  protected String getSessionId()
  {
    return getClientRequest().getSessionId();
  }

  public ClientRequestIF getClientRequest()
  {
    return (ClientRequestIF) request.getAttribute(ClientConstants.CLIENTREQUEST);
  }

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
      httpHeaders.setDate("Last-Modified", Date.from(metadata.getLastModified()).getTime());
    }

    return new ResponseEntity<InputStreamResource>(new InputStreamResource(file.getObjectContent()), httpHeaders, HttpStatus.OK);
  }

}
