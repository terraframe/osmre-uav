package gov.geoplatform.uasdm.remote;

import java.util.Date;

public class MockRemoteFileMetadata implements RemoteFileMetadata
{

  @Override
  public String getContentType()
  {
    return "content-type/test";
  }

  @Override
  public String getContentDisposition()
  {
    return "content-disposition/test";
  }

  @Override
  public long getContentLength()
  {
    return 0;
  }

  @Override
  public Long[] getContentRange()
  {
    return new Long[] { 0L };
  }

  @Override
  public String getContentEncoding()
  {
    return "video/mp4";
  }

  @Override
  public String getETag()
  {
    return null;
  }

  @Override
  public Date getLastModified()
  {
    return null;
  }

}
