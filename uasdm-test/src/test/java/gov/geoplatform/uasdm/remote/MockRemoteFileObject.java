package gov.geoplatform.uasdm.remote;

import java.io.IOException;
import java.io.InputStream;

import org.openide.util.io.NullInputStream;

public class MockRemoteFileObject implements RemoteFileObject
{

  @Override
  public RemoteFileMetadata getObjectMetadata()
  {
    return new MockRemoteFileMetadata();
  }

  @Override
  public InputStream getObjectContent()
  {
    return new NullInputStream();
  }

  @Override
  public void close() throws IOException
  {
  }
  
  @Override
  public String getFilename()
  {
    return "";
  }

}
