package gov.geoplatform.uasdm.remote;

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
    return "";
  }

}
