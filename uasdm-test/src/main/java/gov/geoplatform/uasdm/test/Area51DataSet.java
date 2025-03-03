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

import java.util.Date;

import com.runwaysdk.business.rbac.RoleDAO;

import gov.geoplatform.uasdm.graph.PlatformManufacturer;
import gov.geoplatform.uasdm.graph.PlatformType;
import gov.geoplatform.uasdm.graph.SensorType;
import gov.geoplatform.uasdm.graph.WaveLength;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ProcessConfiguration.ProcessType;

public class Area51DataSet extends TestDataSet
{

  public static final String               TEST_DATA_KEY         = "BDS";

  public static final String               POINT_AREA_51         = "Point(1.5 1.5)";

  public static final TestUserInfo         USER_ADMIN            = new TestUserInfo(TEST_DATA_KEY + "_" + "admin", "admin", "admin@area51.com", new String[] { RoleDAO.ADMIN_ROLE });

  public static final TestOrganizationInfo ORGANIZATION          = new TestOrganizationInfo("OSMRE", "OSMRE");

  public static final TestSensorInfo       SENSOR                = new TestSensorInfo("RGB", "RGB", "rgb-123", SensorType.LASER, 10, 10, 10, 10, WaveLength.NATURAL_COLOR_RGB);

  public static final TestPlatformInfo     PLATFORM              = new TestPlatformInfo("RGB_Platform", "RGB Platform", PlatformType.FIXED_WING, PlatformManufacturer.DJI, SENSOR);

  public static final TestUavInfo          UAV                   = new TestUavInfo(PLATFORM, "123", "321", "aliens", ORGANIZATION);

  public static final TestSiteInfo         SITE_AREA_51          = new TestSiteInfo(ORGANIZATION, "area_51", POINT_AREA_51);

  public static final TestProjectInfo      PROJECT_DREAMLAND     = new TestProjectInfo("Dreamland", SITE_AREA_51);

  public static final TestMissionInfo      MISSION_HAVE_DOUGHNUT = new TestMissionInfo("Have_Doughnut", PROJECT_DREAMLAND);

  public static final TestCollectionInfo   COLLECTION_FISHBED    = new TestCollectionInfo("Fishbed_E", new Date(), UAV, SENSOR, TestDataSet.DEFAULT_SITE_POINT, MISSION_HAVE_DOUGHNUT);

  public static final TestProductInfo      PRODUCT               = new TestProductInfo(COLLECTION_FISHBED, "fishbed_p1", true);
  
  public static final TestDocumentInfo     RAW_DOCUMENT          = new TestDocumentInfo(PRODUCT, COLLECTION_FISHBED, ImageryComponent.RAW + "/test.jpg", "test.jpg");

  public static final TestDocumentInfo     ORTHO_DOCUMENT        = new TestDocumentInfo(PRODUCT, COLLECTION_FISHBED, ImageryComponent.ORTHO + "/test.cog.tif", "test.cog.tif");

  public static final TestDocumentInfo     IMAGE_DOCUMENT        = new TestDocumentInfo(PRODUCT, COLLECTION_FISHBED, ImageryComponent.ORTHO + "/test.png", "test.png");
  
  public static final TestProcessingRunInfo ODM_RUN              = new TestProcessingRunInfo(COLLECTION_FISHBED, ProcessType.ODM, PRODUCT);

  {
    managedOrganizations.add(ORGANIZATION);

    managedUsers.add(USER_ADMIN);

    managedSensors.add(SENSOR);

    managedPlatforms.add(PLATFORM);

    managedUavs.add(UAV);

    managedSites.add(SITE_AREA_51);

    managedProjects.add(PROJECT_DREAMLAND);

    managedMissions.add(MISSION_HAVE_DOUGHNUT);

    managedCollections.add(COLLECTION_FISHBED);

    managedDocuments.add(RAW_DOCUMENT);
    managedDocuments.add(ORTHO_DOCUMENT);
    managedDocuments.add(IMAGE_DOCUMENT);
    
    managedProducts.add(PRODUCT);
    
    managedProcessingRuns.add(ODM_RUN);
  }

  @Override
  public String getTestDataKey()
  {
    return TEST_DATA_KEY;
  }

}
