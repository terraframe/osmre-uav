package gov.geoplatform.uasdm.remote.s3;

import com.amazonaws.services.s3.model.ObjectMetadata;

import gov.geoplatform.uasdm.remote.RemoteFileMetadata;

public class ObjectMetadataWrapper implements RemoteFileMetadata
{
  private ObjectMetadata metadata;

  public ObjectMetadataWrapper(ObjectMetadata metadata)
  {
    this.metadata = metadata;
  }

  @Override
  public String getContentType()
  {
    return this.metadata.getContentType();
  }

  @Override
  public String getContentDisposition()
  {
    return this.metadata.getContentDisposition();
  }

  @Override
  public long getContentLength()
  {
    return this.metadata.getContentLength();
  }

  @Override
  public Long[] getContentRange()
  {
    return this.metadata.getContentRange();
  }

  @Override
  public String getContentEncoding()
  {
    return this.metadata.getContentEncoding();
  }
}
