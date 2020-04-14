package gov.geoplatform.uasdm.remote;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;

import org.apache.commons.io.IOUtils;

import com.runwaysdk.controller.RequestManager;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.request.ResponseDecorator;
import com.runwaysdk.request.ServletResponseIF;

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
      final String contentRange = "bytes " + start + "-" + end + "/" + metadata.getContentLength();
      final String contentType = metadata.getContentType();
      final String contentEncoding = metadata.getContentEncoding();

      ResponseDecorator resp = (ResponseDecorator) manager.getResp();
      resp.setStatus(200);
//      resp.setContentType(contentType);
      resp.setContentType("video/mp4");
      resp.setHeader("Accept-Ranges", "bytes");
//      resp.setHeader("Content-Disposition", metadata.getContentDisposition());
      resp.setHeader("Content-Encoding", contentEncoding);
      resp.setHeader("Content-Length", Long.toString(metadata.getContentLength()));
//      resp.setHeader("Content-Range", contentRange);
      resp.getResponse().setCharacterEncoding(null);
      

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
