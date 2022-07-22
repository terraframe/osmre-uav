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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.openide.util.io.NullInputStream;

import com.runwaysdk.resource.CloseableFile;

public class MockRemoteFileObject implements RemoteFileObject
{

  @Override
  public RemoteFileMetadata getObjectMetadata()
  {
    return new MockRemoteFileMetadata();
  }

  @Override
  public InputStream getObjectContent()
  {
    return new NullInputStream();
  }

  @Override
  public void close()
  {
  }

  @Override
  public InputStream openNewStream()
  {
    return null;
  }

  @Override
  public CloseableFile openNewFile()
  {
    return null;
  }

  @Override
  public File getUnderlyingFile()
  {
    return null;
  }

  @Override
  public String getName()
  {
    return null;
  }

  @Override
  public String getBaseName()
  {
    return null;
  }

  @Override
  public String getNameExtension()
  {
    return null;
  }

  @Override
  public boolean isRemote()
  {
    return false;
  }

  @Override
  public void delete()
  {
    
  }

  @Override
  public String getAbsolutePath()
  {
    return null;
  }

  @Override
  public boolean exists()
  {
    return true;
  }

}
