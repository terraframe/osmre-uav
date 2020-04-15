package gov.geoplatform.uasdm.remote;

import java.util.Date;

public interface RemoteFileMetadata
{

  public String getContentType();

  public String getContentDisposition();

  public long getContentLength();

  public Long[] getContentRange();

  public String getContentEncoding();

  public String getETag();

  public Date getLastModified();
}
