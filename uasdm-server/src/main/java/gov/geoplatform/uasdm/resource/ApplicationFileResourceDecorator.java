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

import java.io.File;
import java.util.Optional;
import java.util.function.Consumer;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.resource.ApplicationFileResource;

public class ApplicationFileResourceDecorator extends ApplicationTreeResourceDecorator implements ApplicationFileResource
{

  public ApplicationFileResourceDecorator(ApplicationFileResource resource)
  {
    super(resource);
  }

  @Override
  protected ApplicationFileResource getResource()
  {
    return (ApplicationFileResource) super.getResource();
  }

  @Override
  public void forAllFileChildren(Consumer<ApplicationFileResource> action)
  {
    this.getResource().forAllFileChildren(action);
  }

  @Override
  public File getUnderlyingFile()
  {
    return this.getResource().getUnderlyingFile();
  }

  @Override
  public boolean isDirectory()
  {
    return this.getResource().isDirectory();
  }

  @Override
  public OIterator<ApplicationFileResource> getChildrenFiles()
  {
    return this.getResource().getChildrenFiles();
  }

  @Override
  public Optional<ApplicationFileResource> getChildFile(String path)
  {
    return this.getResource().getChildFile(path);
  }

  @Override
  public Optional<ApplicationFileResource> getParentFile()
  {
    return this.getResource().getParentFile();
  }

}
