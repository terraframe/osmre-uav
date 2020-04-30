package gov.geoplatform.uasdm.remote;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;

import org.apache.commons.io.IOUtils;

import com.runwaysdk.controller.RequestManager;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.request.ResponseDecorator;

public class RemoteFileGetResponse implements ResponseIF
{
  private RemoteFileObject object;

  public RemoteFileGetResponse(RemoteFileObject object)
  {
    this.object = object;
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
        contentDisposition = "attachment; filename=\"" + this.object.getFilename() + "\"";
      }

      ResponseDecorator resp = (ResponseDecorator) manager.getResp();
      resp.setStatus(200);
      resp.setContentType(metadata.getContentType());
      resp.setHeader("Content-Encoding", metadata.getContentEncoding());
      resp.setHeader("Content-Disposition", contentDisposition);
      resp.setHeader("Content-Length", Long.toString(metadata.getContentLength()));
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
