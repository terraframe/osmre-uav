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

import gov.geoplatform.uasdm.graph.Mission;
import gov.geoplatform.uasdm.graph.UasComponent;

public class TestMissionInfo extends TestUasComponentInfo
{
  private TestProjectInfo project;

  public TestMissionInfo(String name, TestProjectInfo project)
  {
    super(name, name, project.getS3location() + name, null);

    this.project = project;
  }

  public TestProjectInfo getProject()
  {
    return project;
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
    super.apply(this.project);
  }

  /**
   * Creates a new instance of the server object type.
   */
  @Override
  public UasComponent instantiate()
  {
    return new Mission();
  }
}
