package gov.geoplatform.uasdm.remote.s3;

import java.io.IOException;
import java.io.InputStream;

import com.amazonaws.services.s3.model.S3Object;

import gov.geoplatform.uasdm.remote.RemoteFileMetadata;
import gov.geoplatform.uasdm.remote.RemoteFileObject;

public class S3ObjectWrapper implements RemoteFileObject
{
  private S3Object object;

  public S3ObjectWrapper(S3Object object)
  {
    this.object = object;
  }

  @Override
  public RemoteFileMetadata getObjectMetadata()
  {
    return new ObjectMetadataWrapper(this.object.getObjectMetadata());
  }

  @Override
  public InputStream getObjectContent()
  {
    return this.object.getObjectContent();
  }

  @Override
  public void close() throws IOException
  {
    this.object.close();
  }

}
