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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.openide.util.io.NullInputStream;

import com.runwaysdk.resource.CloseableFile;

import gov.geoplatform.uasdm.remote.RemoteFileMetadata;
import gov.geoplatform.uasdm.remote.RemoteFileObject;

public class MockRemoteFileObject implements RemoteFileObject
{
  private File file;

  public MockRemoteFileObject()
  {
    this.file = null;
  }

  public MockRemoteFileObject(File file)
  {
    this.file = file;
  }

  @Override
  public RemoteFileMetadata getObjectMetadata()
  {
    return new MockRemoteFileMetadata();
  }

  @Override
  public InputStream getObjectContent()
  {
    if (this.file != null)
    {
      try
      {
        return new FileInputStream(this.file);
      }
      catch (FileNotFoundException e)
      {
        throw new RuntimeException(e);
      }
    }

    return new NullInputStream();
  }

  @Override
  public InputStream openNewStream()
  {
    return this.getObjectContent();
  }

  @Override
  public CloseableFile openNewFile()
  {
    if (this.file != null)
    {
      return new CloseableFile(file);
    }

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

  @Override
  public void close()
  {

  }

}
