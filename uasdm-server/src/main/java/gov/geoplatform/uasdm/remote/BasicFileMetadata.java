package gov.geoplatform.uasdm.remote;

import java.util.Date;

public class BasicFileMetadata implements RemoteFileMetadata
{
  private String contentType;

  private long   contentLength;

  public BasicFileMetadata(String contentType, long contentLength)
  {
    super();
    this.contentType = contentType;
    this.contentLength = contentLength;
  }

  @Override
  public String getContentType()
  {
    return this.contentType;
  }

  @Override
  public String getContentDisposition()
  {
    return null;
  }

  @Override
  public long getContentLength()
  {
    return this.contentLength;
  }

  @Override
  public Long[] getContentRange()
  {

    return null;
  }

  @Override
  public String getContentEncoding()
  {

    return null;
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
