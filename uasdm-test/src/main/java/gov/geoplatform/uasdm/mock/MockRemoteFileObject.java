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

import java.io.File;
import java.io.InputStream;

import org.openide.util.io.NullInputStream;

import com.runwaysdk.resource.CloseableFile;

import gov.geoplatform.uasdm.remote.RemoteFileMetadata;
import gov.geoplatform.uasdm.remote.RemoteFileObject;

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
  public InputStream openNewStream()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public CloseableFile openNewFile()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public File getUnderlyingFile()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getName()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getBaseName()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getNameExtension()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isRemote()
  {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void delete()
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public String getAbsolutePath()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean exists()
  {
    return true;
  }

  @Override
  public void close()
  {
    // TODO Auto-generated method stub
  }

}
