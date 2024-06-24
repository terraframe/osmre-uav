/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.geoplatform.uasdm;

import gov.geoplatform.uasdm.processing.report.CollectionReportFacade;
import gov.geoplatform.uasdm.service.IndexService;
import net.geoprism.build.GeoprismDatabaseBuilder;
import net.geoprism.build.GeoprismDatabaseBuilderIF;

public class IDMDatabaseBuilder extends GeoprismDatabaseBuilder implements GeoprismDatabaseBuilderIF
{
  /**
   * IMPORTANT - THIS METHOD IS ONLY INVOKE WHEN THIS CLASS IS RUN FROM
   * GeoprismDatabaseBuilder.main(args)
   **/
  @Override
  public void close()
  {
    // Place mandatory shutdown hooks (without which will cause the patcher to
    // hang) here.

    IndexService.shutdown();
    CollectionReportFacade.shutdown();
  }
}
