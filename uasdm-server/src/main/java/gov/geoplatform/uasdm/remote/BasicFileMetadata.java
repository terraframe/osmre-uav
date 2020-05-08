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
