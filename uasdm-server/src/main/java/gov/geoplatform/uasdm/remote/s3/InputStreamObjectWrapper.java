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

import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;

import com.runwaysdk.resource.StreamResource;

import gov.geoplatform.uasdm.remote.RemoteFileMetadata;
import gov.geoplatform.uasdm.remote.RemoteFileObject;

public class InputStreamObjectWrapper extends StreamResource implements RemoteFileObject
{
  private RemoteFileMetadata metadata;
  
  private String key;
  
  public InputStreamObjectWrapper(String key, InputStream is, RemoteFileMetadata metadata)
  {
    super(is, FilenameUtils.getName(key));
    
    this.key = key;
    this.metadata = metadata;
  }

  @Override
  public RemoteFileMetadata getObjectMetadata()
  {
    return metadata;
  }

  @Override
  public InputStream getObjectContent()
  {
    return this.openNewStream();
  }
  
  @Override
  public String getAbsolutePath()
  {
    return key;
  }

  @Override
  public boolean isRemote()
  {
    return true;
  }
}
