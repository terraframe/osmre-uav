package gov.geoplatform.uasdm;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;

import org.apache.commons.io.IOUtils;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.runwaysdk.controller.RequestManager;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.request.ServletResponseIF;

public class S3GetResponse implements ResponseIF
{
  private S3Object object;

  public S3GetResponse(S3Object object)
  {
    this.object = object;
  }

  @Override
  public void handle(RequestManager manager) throws ServletException, IOException
  {
    try
    {
      ObjectMetadata metadata = this.object.getObjectMetadata();
      
      ServletResponseIF resp = manager.getResp();
      resp.setStatus(200);
      resp.setContentType(metadata.getContentType());
      resp.setHeader("Content-disposition", metadata.getContentDisposition());

      try (OutputStream ostream = resp.getOutputStream())
      {
        try (S3ObjectInputStream istream = this.object.getObjectContent())
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
