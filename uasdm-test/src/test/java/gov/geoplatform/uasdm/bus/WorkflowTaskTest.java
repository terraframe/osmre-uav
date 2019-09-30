package gov.geoplatform.uasdm.bus;

import org.json.JSONArray;
import org.json.JSONObject;
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

import net.geoprism.GeoprismUser;

public class WorkflowTaskTest
{
  private static String       collectionId1;

  /**
   * The test user object
   */
  private static GeoprismUser newUser;

  /**
   * The username for the user
   */
  private final static String USERNAME     = "btables";

  /**
   * The password for the user
   */
  private final static String PASSWORD     = "1234";

  private final static int    sessionLimit = 2;

  @BeforeClass
  @Request
  public static void classSetUp()
  {
    createSiteHierarchyTransaction();
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

    Project project1 = new Project();
    project1.setName("Project1");
    project1.setFolderName("Project1");
    project1.applyWithParent(site);

    Project project2 = new Project();
    project2.setName("Project2");
    project2.setFolderName("Project2");
    project2.applyWithParent(site);

    Mission mission1 = new Mission();
    mission1.setName("Mission1");
    mission1.setFolderName("Mission1");
    mission1.applyWithParent(project1);

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

  @Test
  @Request
  public void testToJSON()
  {
    Collection collection = Collection.get(collectionId1);

    WorkflowTask task = new WorkflowTask();
    task.setGeoprismUser(newUser);
    task.setComponent(collection);
    task.setUploadId("testID");
    task.setStatus("Test Status");
    task.setTaskLabel("Test label");
    task.apply();

    WorkflowAction action = new WorkflowAction();
    action.setActionType("TEST");
    action.setDescription("TEST");
    action.setWorkflowTask(task);
    action.apply();

    JSONObject json = task.toJSON();

    Assert.assertTrue(json.has("actions"));
    Assert.assertEquals(task.getTaskLabel(), json.getString("label"));

    JSONArray actions = json.getJSONArray("actions");

    Assert.assertEquals(1, actions.length());
  }

}
