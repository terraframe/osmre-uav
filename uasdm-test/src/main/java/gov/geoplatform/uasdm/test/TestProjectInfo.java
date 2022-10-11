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
package gov.geoplatform.uasdm.test;

import gov.geoplatform.uasdm.graph.Project;
import gov.geoplatform.uasdm.graph.UasComponent;

public class TestProjectInfo extends TestUasComponentInfo
{
  private TestSiteInfo site;

  public TestProjectInfo(String name, TestSiteInfo site)
  {
    super(name, name, name, null);

    this.site = site;
  }

  public TestSiteInfo getSite()
  {
    return site;
  }

  /**
   * Populates the component with the values contained within this wrapper
   */
  @Override
  public void populate(UasComponent component)
  {
    super.populate(component);
  }

  @Override
  public void apply()
  {
    super.apply(this.site);
  }

  /**
   * Creates a new instance of the server object type.
   */
  @Override
  public UasComponent instantiate()
  {
    return new Project();
  }
}
