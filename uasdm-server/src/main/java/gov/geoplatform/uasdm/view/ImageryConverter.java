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

import gov.geoplatform.uasdm.bus.Imagery;
import gov.geoplatform.uasdm.model.UasComponentIF;

public class ImageryConverter extends Converter
{
  public ImageryConverter()
  {
    super();
  }

  @Override
  protected UasComponentIF convert(SiteItem siteItem, UasComponentIF uasComponent)
  {
    Imagery imagery = (Imagery)super.convert(siteItem, uasComponent);
    
    return imagery;
  }

  @Override
  protected SiteItem convert(UasComponentIF uasComponent, boolean metadata, boolean hasChildren)
  {
    SiteItem siteItem = super.convert(uasComponent, metadata, hasChildren);

    return siteItem;
  }

  protected Imagery convertNew(UasComponentIF uasComponent, SiteItem siteItem)
  {
    Imagery imagery = (Imagery)super.convertNew(uasComponent, siteItem);
    
    return imagery;
  }
}
