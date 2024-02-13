package gov.geoplatform.uasdm.controller;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;

import com.runwaysdk.controller.RequestManager;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.request.ServletRequestIF;
import com.runwaysdk.request.ServletResponseIF;

public class XMLResponse implements ResponseIF
{
  private String content;

  private int    status;

  public XMLResponse(String content, int status)
  {
    this.content = content;
    this.status = status;
  }

  @Override
  public void handle(RequestManager manager) throws ServletException, IOException
  {
    ServletRequestIF req = manager.getReq();
    ServletResponseIF resp = manager.getResp();

    String encoding = ( req.getCharacterEncoding() != null ? req.getCharacterEncoding() : "UTF-8" );

    resp.setStatus(this.status);
    resp.setContentType("application/xml");

    try (OutputStream ostream = resp.getOutputStream())
    {
      if (content != null)
      {
        ostream.write(content.getBytes(encoding));
      }

      ostream.flush();
    }
  }
}
