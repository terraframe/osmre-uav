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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.commons.io.IOUtils;

import com.runwaysdk.controller.RequestManager;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.request.ResponseDecorator;

public class RemoteFileGetResponse implements ResponseIF
{
  private RemoteFileObject object;
  
  private Map<String, String> headers = new HashMap<String, String>();

  public RemoteFileGetResponse(RemoteFileObject object)
  {
    this.object = object;
  }
  
  public RemoteFileGetResponse setHeader(String key, String value)
  {
    headers.put(key, value);
    return this;
  }

  @Override
  public void handle(RequestManager manager) throws ServletException, IOException
  {
    try
    {
      RemoteFileMetadata metadata = this.object.getObjectMetadata();
      String contentDisposition = metadata.getContentDisposition();

      if (contentDisposition == null)
      {
        contentDisposition = "attachment; filename=\"" + this.object.getName() + "\"";
      }

      ResponseDecorator resp = (ResponseDecorator) manager.getResp();
      resp.setStatus(200);
      resp.setContentType(metadata.getContentType());
      resp.setHeader("Content-Encoding", metadata.getContentEncoding());
      resp.setHeader("Content-Disposition", contentDisposition);
      resp.setHeader("Content-Length", Long.toString(metadata.getContentLength()));
      resp.setHeader("ETag", metadata.getETag());
      
      for (String key : headers.keySet())
      {
        resp.setHeader(key, headers.get(key));
      }
      
      if (metadata.getLastModified() != null)
      {
        resp.getResponse().setDateHeader("Last-Modified", metadata.getLastModified().getTime());
      }

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
