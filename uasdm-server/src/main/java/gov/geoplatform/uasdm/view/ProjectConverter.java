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

import gov.geoplatform.uasdm.model.ProjectIF;
import gov.geoplatform.uasdm.model.UasComponentIF;

public class ProjectConverter extends Converter
{
  public ProjectConverter()
  {
    super();
  }

  @Override
  protected UasComponentIF convert(SiteItem siteItem, UasComponentIF uasComponent)
  {
    return super.convert(siteItem, uasComponent);
  }

  @Override
  protected SiteItem convert(UasComponentIF uasComponent, boolean metadata, boolean hasChildren)
  {
    return super.convert(uasComponent, metadata, hasChildren);
  }

  protected ProjectIF convertNew(UasComponentIF uasComponent, SiteItem siteItem)
  {
    return (ProjectIF) super.convertNew(uasComponent, siteItem);
  }
}
