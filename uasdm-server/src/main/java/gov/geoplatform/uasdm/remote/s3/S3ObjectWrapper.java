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

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;

import com.amazonaws.services.s3.model.S3Object;

import gov.geoplatform.uasdm.remote.RemoteFileMetadata;
import gov.geoplatform.uasdm.remote.RemoteFileObject;

public class S3ObjectWrapper implements RemoteFileObject
{
  private S3Object object;

  public S3ObjectWrapper(S3Object object)
  {
    this.object = object;
  }

  @Override
  public RemoteFileMetadata getObjectMetadata()
  {
    return new ObjectMetadataWrapper(this.object.getObjectMetadata());
  }

  @Override
  public InputStream getObjectContent()
  {
    return this.object.getObjectContent();
  }

  @Override
  public void close() throws IOException
  {
    this.object.close();
  }

  @Override
  public String getFilename()
  {
    return FilenameUtils.getName(this.object.getKey());
  }

}
