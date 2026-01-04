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

import java.time.Instant;

import gov.geoplatform.uasdm.remote.RemoteFileMetadata;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

public class ObjectMetadataWrapper implements RemoteFileMetadata
{
  private GetObjectResponse metadata;

  public ObjectMetadataWrapper(GetObjectResponse metadata)
  {
    this.metadata = metadata;
  }

  @Override
  public String getContentType()
  {
    return this.metadata.contentType();
  }

  @Override
  public String getContentDisposition()
  {
    return this.metadata.contentDisposition();
  }

  @Override
  public long getContentLength()
  {
    return this.metadata.contentLength();
  }

  @Override
  public String getContentRange()
  {
    return this.metadata.contentRange();
  }

  @Override
  public String getContentEncoding()
  {
    return this.metadata.contentEncoding();
  }

  @Override
  public String getETag()
  {
    return this.metadata.eTag();
  }

  @Override
  public Instant getLastModified()
  {
    return this.metadata.lastModified();
  }
}
