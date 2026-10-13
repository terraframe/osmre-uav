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
package gov.geoplatform.uasdm.resource;

import java.io.InputStream;

import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.resource.CloseableFile;

public class ApplicationResourceDecorator implements ApplicationResource
{
  private ApplicationResource resource;

  public ApplicationResourceDecorator(ApplicationResource resource)
  {
    super();
    this.resource = resource;
  }

  protected ApplicationResource getResource()
  {
    return resource;
  }

  @Override
  public String getAbsolutePath()
  {
    return this.getResource().getAbsolutePath();
  }

  @Override
  public InputStream openNewStream()
  {
    return this.getResource().openNewStream();
  }

  @Override
  public CloseableFile openNewFile()
  {
    return this.getResource().openNewFile();
  }

  @Override
  public String getName()
  {
    return this.getResource().getName();
  }

  @Override
  public String getBaseName()
  {
    return this.getResource().getBaseName();
  }

  @Override
  public String getNameExtension()
  {
    return this.getResource().getNameExtension();
  }

  @Override
  public boolean isRemote()
  {
    return this.getResource().isRemote();
  }

  @Override
  public boolean exists()
  {
    return this.getResource().exists();
  }

  @Override
  public void close()
  {
    this.getResource().close();
  }

  @Override
  public void delete()
  {
    this.getResource().delete();
  }

}
