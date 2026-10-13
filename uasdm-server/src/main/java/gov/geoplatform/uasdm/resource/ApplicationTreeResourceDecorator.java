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

import java.util.Optional;
import java.util.function.Consumer;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.resource.ApplicationTreeResource;

public class ApplicationTreeResourceDecorator extends ApplicationResourceDecorator implements ApplicationTreeResource
{

  public ApplicationTreeResourceDecorator(ApplicationTreeResource resource)
  {
    super(resource);
  }

  @Override
  protected ApplicationTreeResource getResource()
  {
    return (ApplicationTreeResource) super.getResource();
  }

  @Override
  public void forAllChildren(Consumer<ApplicationTreeResource> action)
  {
    this.getResource().forAllChildren(action);
  }

  @Override
  public OIterator<ApplicationTreeResource> getChildren()
  {
    return this.getResource().getChildren();
  }

  @Override
  public boolean hasChildren()
  {
    return this.getResource().hasChildren();
  }

  @Override
  public Optional<ApplicationTreeResource> getChild(String path)
  {
    return this.getResource().getChild(path);
  }

  @Override
  public Optional<ApplicationTreeResource> getParent()
  {
    return this.getResource().getParent();
  }

}
