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
package gov.geoplatform.uasdm.view;

import java.util.List;

public class ComponentImageSetView
{
  private String           componentId;

  private List<ImageSetView> sets;

  private String           componentType;

  public String getComponentId()
  {
    return componentId;
  }

  public void setComponentId(String componentId)
  {
    this.componentId = componentId;
  }

  public List<ImageSetView> getSets()
  {
    return sets;
  }

  public void setSets(List<ImageSetView> sets)
  {
    this.sets = sets;
  }

  public String getComponentType()
  {
    return componentType;
  }

  public void setComponentType(String componentType)
  {
    this.componentType = componentType;
  }

}
