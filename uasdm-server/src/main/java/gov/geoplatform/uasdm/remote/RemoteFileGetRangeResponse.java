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
package gov.geoplatform.uasdm.remote;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.runwaysdk.controller.RequestManager;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.request.ResponseDecorator;

public class RemoteFileGetRangeResponse implements ResponseIF
{
  private RemoteFileObject object;

  public RemoteFileGetRangeResponse(RemoteFileObject object)
  {
    this.object = object;
  }

  @Override
  public void handle(RequestManager manager) throws ServletException, IOException
  {
    try
    {
      RemoteFileMetadata metadata = this.object.getObjectMetadata();
      final Long start = metadata.getContentRange()[0];
      final Long end = metadata.getContentRange()[1];
      final String contentRange = "bytes " + start + "-" + end + "/*";
      final String contentType = metadata.getContentType();
      final String contentEncoding = metadata.getContentEncoding();

      ResponseDecorator resp = (ResponseDecorator) manager.getResp();
      resp.getResponse().reset();
      resp.setContentType(contentType);
      resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
      resp.setHeader("Accept-Ranges", "bytes");
      resp.setHeader("Content-Disposition", metadata.getContentDisposition());
      resp.setHeader("Content-Encoding", contentEncoding);
      resp.setHeader("Content-Length", Long.toString(metadata.getContentLength()));
      resp.setHeader("Content-Range", contentRange);
      resp.setHeader("Content-Type", contentType);
      resp.setHeader("ETag", metadata.getETag());
      resp.getResponse().setDateHeader("Last-Modified", metadata.getLastModified().getTime());

      try (OutputStream ostream = resp.getOutputStream())
      {
        try (InputStream istream = this.object.getObjectContent())
        {
          IOUtils.copy(istream, ostream);
        }
      }
    }
    finally
    {
      this.object.close();
    }
  }
}
