/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.geoplatform.uasdm.remote.s3;

import java.util.Date;

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

  @Override
  public String getETag()
  {
    return this.metadata.getETag();
  }

  @Override
  public Date getLastModified()
  {
    return this.metadata.getLastModified();
  }
}
