package gov.geoplatform.uasdm.bus;

import gov.geoplatform.uasdm.service.ProjectManagementService;

import java.io.File;
import java.util.List;

import net.geoprism.GeoprismUser;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.business.rbac.UserDAO;
import com.runwaysdk.constants.UserInfo;
import com.runwaysdk.dataaccess.DuplicateDataException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

public class UploadArchiveTest
{
//  private static ProjectManagementService service;
//
//  private static String                   siteId;
//
//  private static String                   projectId1;
//
//  private static String                   missionId1;

  private static String                   collectionId1;

  /**
   * The test user object
   */
  private static GeoprismUser             newUser;

  /**
   * The username for the user
   */
  private final static String             USERNAME     = "btables";

  /**
   * The password for the user
   */
  private final static String             PASSWORD     = "1234";

  private final static int                sessionLimit = 2;

  @BeforeClass
  @Request
  public static void classSetUp()
  {
    createSiteHierarchyTransaction();

//    service = new ProjectManagementService();
  }

  @Transaction
  private static void createSiteHierarchyTransaction()
  {

    try
    {
      // Create a new user
      newUser = new GeoprismUser();
      newUser.setValue(UserInfo.USERNAME, USERNAME);
      newUser.setValue(UserInfo.PASSWORD, PASSWORD);
      newUser.setValue(UserInfo.SESSION_LIMIT, Integer.toString(sessionLimit));
      newUser.setFirstName("test");
      newUser.setLastName("test");
      newUser.setEmail("test@email.com");
      newUser.apply();

      // Make the user an admin
      RoleDAO adminRole = RoleDAO.findRole(RoleDAO.ADMIN_ROLE).getBusinessDAO();
      adminRole.assignMember(UserDAO.get(newUser.getOid()));
    }
    catch (DuplicateDataException e)
    {
      newUser = GeoprismUser.getByUsername(USERNAME);
    }

    Site site = new Site();
    site.setName("Site_Unit_Test");
    site.setFolderName("Site_Unit_Test");
    site.applyWithParent(null);
    // System.out.println("S3: "+site.getS3location());
//    siteId = site.getOid();

    Project project1 = new Project();
    project1.setName("Project1");
    project1.setFolderName("Project1");
    project1.applyWithParent(site);
    // System.out.println("S3: "+project1.getS3location());
//    projectId1 = project1.getOid();

    Project project2 = new Project();
    project2.setName("Project2");
    project2.setFolderName("Project2");
    project2.applyWithParent(site);

    Mission mission1 = new Mission();
    mission1.setName("Mission1");
    mission1.setFolderName("Mission1");
    mission1.applyWithParent(project1);
    // System.out.println("S3: "+mission1.getS3location());
//    missionId1 = mission1.getOid();

    Collection collection1 = new Collection();
    collection1.setName("Collection1");
    collection1.setFolderName("Collection1");
    collection1.applyWithParent(mission1);
    // System.out.println("S3: "+collection1.getS3location());
    collectionId1 = collection1.getOid();
  }

  @AfterClass
  @Request
  public static void classTearDown()
  {
    classTearDownTransaction();
  }

  @Transaction
  public static void classTearDownTransaction()
  {
    QueryFactory qf = new QueryFactory();

    SiteQuery sq = new SiteQuery(qf);

    OIterator<? extends Site> i = sq.getIterator();

    try
    {
      for (Site site : i)
      {
        if (!site.getName().equals(Site.DEFAULT_SITE_NAME))
        {
          site.delete();
          System.out.println("Site deleted: " + site.getName());
        }
      }
    }
    finally
    {
      i.close();
    }

    if (newUser.isAppliedToDB())
    {
      newUser.delete();
    }
  }

  // @Test
  // @Request
  // public void testZipArchive()
  // {
  // System.out.println("Starting");
  //
  // Collection collection = Collection.get(collectionId1);
  // collection.uploadArchive(new
  // File("C:/Users/admin/Documents/TerraFrame/OSMRE/OSMRE.zip"));
  //
  // System.out.println("Finished");
  // }

  @Test
  @Request
  public void testTarGzArchive()
  {
    Collection collection = Collection.get(collectionId1);

    WorkflowTask task = new WorkflowTask();
    task.setGeoprismUser(newUser);
    task.setCollection(collection);
    task.setUpLoadId("testID");
    task.setStatus("Test Status");
    task.apply();

    collection.uploadArchive(task, new File("C:/Users/admin/Documents/TerraFrame/OSMRE/OSMRE.tar.gz"));

    List<WorkflowAction> actions = task.getActions();

    Assert.assertEquals(0, actions.size());
  }

//  @Test
//  @Request
//  public void testUploadMetadata() throws FileNotFoundException, IOException
//  {
//    Mission mission = Mission.get(missionId1);
//
//    try (FileInputStream istream = new FileInputStream("C:/Users/admin/Documents/TerraFrame/OSMRE/mission_1_uasmeta.xml"))
//    {
//      mission.uploadMetadata("mission_1_uasmeta.xml", istream);
//    }
//
//    try (FileInputStream istream = new FileInputStream("C:/Users/admin/Documents/TerraFrame/OSMRE/mission_1_uasmeta.xml"))
//    {
//      mission.uploadMetadata("mission_1_uasmeta.xml", istream);
//    }
//
//    List<QueryResult> results = SolrService.query(mission.getName());
//
//    Assert.assertEquals(3, results.size());
//  }
//
}
