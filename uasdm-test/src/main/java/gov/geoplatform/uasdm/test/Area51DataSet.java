package gov.geoplatform.uasdm.test;

import java.util.Date;

import com.runwaysdk.business.rbac.RoleDAO;

import gov.geoplatform.uasdm.graph.Bureau;
import gov.geoplatform.uasdm.graph.PlatformManufacturer;
import gov.geoplatform.uasdm.graph.PlatformType;
import gov.geoplatform.uasdm.graph.SensorType;
import gov.geoplatform.uasdm.graph.WaveLength;

public class Area51DataSet extends TestDataSet
{
  
  public static final String TEST_DATA_KEY = "BDS";
  
  public static final String POINT_AREA_51 = "Point(115.7930 37.2431)";
  
  public static final TestUserInfo USER_ADMIN = new TestUserInfo(TEST_DATA_KEY + "_" + "admin", "admin", "admin@area51.com", new String[] { RoleDAO.ADMIN_ROLE });
  
  public static final TestSensorInfo SENSOR = new TestSensorInfo("RGB", "RGB", "rgb-123", SensorType.getByName(SensorType.LASER), 10, 10, 10, 10, WaveLength.getByName(WaveLength.NATURAL_COLOR_RGB));
  
  public static final TestPlatformInfo PLATFORM = new TestPlatformInfo("RGB_Platform", "RGB Platform", PlatformType.getByName(PlatformType.FIXED_WING), PlatformManufacturer.getByName(PlatformManufacturer.DJI), SENSOR);
  
  public static final TestUavInfo UAV = new TestUavInfo(PLATFORM, "123", "321", "aliens", Bureau.getByName("OSMRE"));
  
  public static final TestSiteInfo SITE_AREA_51 = new TestSiteInfo(Bureau.getByName("OSMRE"), "area_51", POINT_AREA_51);
  
  public static final TestProjectInfo PROJECT_DREAMLAND = new TestProjectInfo("Dreamland");
  
  public static final TestMissionInfo MISSION_HAVE_DOUGHNUT = new TestMissionInfo("Have Doughnut");
  
  public static final TestCollectionInfo COLLECTION_FISHBED = new TestCollectionInfo("Fishbed-E", new Date(), UAV, SENSOR);

  {
    managedUsers.add(USER_ADMIN);
    
    managedSensors.add(SENSOR);
    
    managedPlatforms.add(PLATFORM);
    
    managedUavs.add(UAV);
    
    managedSites.add(SITE_AREA_51);
    
    managedProjects.add(PROJECT_DREAMLAND);
    
    managedMissions.add(MISSION_HAVE_DOUGHNUT);
    
    managedCollections.add(COLLECTION_FISHBED);
  }
  
  @Override
  public String getTestDataKey()
  {
    return TEST_DATA_KEY;
  }

}
