package gov.geoplatform.uasdm.remote;

import java.io.IOException;
import java.io.InputStream;

public interface RemoteFileObject extends AutoCloseable
{

  public RemoteFileMetadata getObjectMetadata();

  public InputStream getObjectContent();

  public void close() throws IOException;

  public String getFilename();

}
