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
package gov.geoplatform.uasdm.mock;

import java.time.Instant;

import gov.geoplatform.uasdm.remote.RemoteFileMetadata;

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
  public String getContentRange()
  {
    return "Range: bytes=0-499";
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
  public Instant getLastModified()
  {
    return null;
  }

}
