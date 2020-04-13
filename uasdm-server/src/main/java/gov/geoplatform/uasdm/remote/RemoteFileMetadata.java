package gov.geoplatform.uasdm.remote;

public interface RemoteFileMetadata
{

  public String getContentType();

  public String getContentDisposition();

  public long getContentLength();

  public Long[] getContentRange();

  public String getContentEncoding();
}
