package gov.geoplatform.uasdm.bus;

import java.util.List;
import java.util.Locale;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.SmartExceptionDTO;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.business.rbac.UserDAO;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.dataaccess.DuplicateDataException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.SessionFacade;
import com.runwaysdk.system.SingleActor;

import gov.geoplatform.uasdm.service.ProjectManagementService;
import gov.geoplatform.uasdm.view.SiteItem;
import net.geoprism.GeoprismUser;


public class TestSiteHierarchy
{
  
  private static ProjectManagementService service;

  private static String                   siteId;
  
  private static String                   projectId1;
  
  private static String                   missionId1;

  
  /**
   * The test user object
   */
  private static GeoprismUser             newUser;
  
  /**
   * The username for the user
   */
  private final static String             USERNAME       = "btables";

  /**
   * The password for the user
   */
  private final static String             PASSWORD       = "1234";

  private final static int                sessionLimit   = 2;
  
  @BeforeClass
  @Request
  public static void classSetUp()
  {
    createSiteHierarchyTransaction();
    
    service = new ProjectManagementService();
  }
  
  @Transaction
  private static void createSiteHierarchyTransaction()
  {    
    
    try
    {
      newUser = new GeoprismUser();
      newUser.setUsername(USERNAME);
      newUser.setPassword(PASSWORD);
      newUser.setFirstName("Bobby");
      newUser.setLastName("Tables");
      newUser.setEmail("bobby@tables.com");
      newUser.setSessionLimit(sessionLimit);
      newUser.apply();
      
      // Make the user an admin
      RoleDAO adminRole = RoleDAO.findRole(RoleDAO.ADMIN_ROLE).getBusinessDAO();
      adminRole.assignMember((UserDAO)BusinessFacade.getEntityDAO(newUser));
    }
    catch (DuplicateDataException e)
    {
      
    }

    Site site = new Site();
    site.setValue(UasComponent.NAME,"Site_Unit_Test");
    site.applyWithParent(null);
//System.out.println("S3: "+site.getS3location());
    siteId = site.getOid();
    
    Project project1 = new Project();
    project1.setValue(UasComponent.NAME,"Project1");
    project1.applyWithParent(site);
//System.out.println("S3: "+project1.getS3location());
    projectId1 = project1.getOid();
    
    
    Project project2 = new Project();
    project2.setValue(UasComponent.NAME,"Project2");
    project2.applyWithParent(site);
    
    Mission mission1 = new Mission();
    mission1.setValue(UasComponent.NAME,"Mission1");
    mission1.applyWithParent(project1);
//System.out.println("S3: "+mission1.getS3location());
    missionId1 = mission1.getOid();
    
    Collection collection1 = new Collection();
    collection1.setValue(UasComponent.NAME,"Collection1");
    collection1.applyWithParent(mission1);
//System.out.println("S3: "+collection1.getS3location()); 
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
      for(Site site : i)
      {
        if (!site.getValue(UasComponent.NAME).equals(Site.DEFAULT_SITE_NAME))
        {
          site.delete();
          System.out.println("Site deleted: "+site.getValue(UasComponent.NAME));
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
  public void testRelationships()
  {
    Site site = Site.get(siteId);
    
    OIterator<? extends Project> projects = site.getAllProjects();
    
    int projectCount = 0;
    int missionCount = 0;
    int collectionCount = 0;
    for (Project project : projects)
    {
      projectCount++;
      
      OIterator<? extends Mission> missions = project.getAllMissions();
      
      for (Mission mission : missions)
      {
        missionCount++;
        
        OIterator<? extends Collection> collections = mission.getAllCollections();
        
        for (@SuppressWarnings("unused") Collection collection : collections)
        {
          collectionCount++;
        }
      }
    }
    
    Assert.assertEquals("Incorrect number of projects", 2, projectCount);
    Assert.assertEquals("Incorrect number of missions", 1, missionCount);
    Assert.assertEquals("Incorrect number of collections", 1, collectionCount);
  }
  

  public void testServiceGetChildren()
  {
    String sessionId = this.logInAdmin();

    try
    {
      List<SiteItem> siteItems = service.getChildren(sessionId, siteId);

      Assert.assertEquals("Wrong number of projects returned", 2, siteItems.size());
      
      SiteItem siteItem1 = siteItems.get(0);
      SiteItem siteItem2 = siteItems.get(1);
      
      boolean siteItem1ValidName = false;
      if (siteItem1.getValue(UasComponent.NAME).equals("Project1") || siteItem1.getValue(UasComponent.NAME).equals("Project2"))
      {
        siteItem1ValidName = true;
      }    
      
      boolean siteItem2ValidName = false;
      if (siteItem2.getValue(UasComponent.NAME).equals("Project1") || siteItem2.getValue(UasComponent.NAME).equals("Project2"))
      {
        siteItem2ValidName = true;
      }  
      
      Assert.assertEquals("Wrong site item name", true, siteItem1ValidName);
      Assert.assertEquals("Wrong site item name", true, siteItem2ValidName);
      
    }
    finally
    {
      logOutAdmin(sessionId);
    }
  }

  @Test
  public void testInvalidName()
  {
    String sessionId = this.logInAdmin();
    
    String projectId = null;

    try
    {
      SiteItem newProject = service.newChild(sessionId, siteId);
      newProject.setValue(UasComponent.NAME,"Project ");
      newProject = service.applyWithParent(sessionId, newProject, siteId);
      projectId = newProject.getId();
      Assert.fail("A Project was created with an invalid name.");
    }
    catch (SmartExceptionDTO re)
    {
System.out.println(re.getLocalizedMessage());
    }
    finally
    { 
      if (projectId != null)
      {
        service.remove(sessionId, projectId);
      }

      logOutAdmin(sessionId);
    }
  }
  
  @Test
  public void testDuplicateName()
  {
    String sessionId = this.logInAdmin();
    
    String projectId = null;

    try
    {
      SiteItem newProject = service.newChild(sessionId, siteId);
      newProject.setValue(UasComponent.NAME,"Project1");
      newProject = service.applyWithParent(sessionId, newProject, siteId);
      projectId = newProject.getId();
      Assert.fail("A Project was created with a duplicate name");
    }
    catch (SmartExceptionDTO re)
    {
System.out.println(re.getLocalizedMessage());
    }
    finally
    { 
      if (projectId != null)
      {
        service.remove(sessionId, projectId);
      }

      logOutAdmin(sessionId);
    }
  }
  
  @Test
  public void testServiceNewChild()
  {
    String sessionId = this.logInAdmin();
    
    String projectId = null;
    
    String missionId = null;
    
    String collectionId = null;

    try
    {
      int siteChildren = service.getChildren(sessionId, siteId).size();
      SiteItem newProject = service.newChild(sessionId, siteId);
      
      Assert.assertFalse("HasChildren property on SiteItem should be false but returned true.", newProject.getHasChildren());

      newProject.setValue(UasComponent.NAME,"ProjectX");
      projectId = newProject.getId();
      newProject = service.applyWithParent(sessionId, newProject, siteId);
      
      Assert.assertTrue(siteChildren + 1 == service.getChildren(sessionId, siteId).size());
      
      
      Assert.assertTrue(0 == service.getChildren(sessionId, projectId).size());
      
      SiteItem newMission = service.newChild(sessionId, projectId1);
      
      Assert.assertFalse("HasChildren property on SiteItem should be false but returned true.", newMission.getHasChildren());
      
      newMission.setValue(UasComponent.NAME,"MissionX");
      missionId = newMission.getId();
      service.applyWithParent(sessionId, newMission, projectId);
      
      Assert.assertTrue(1 == service.getChildren(sessionId, projectId).size());
      
      
      Assert.assertTrue(0 == service.getChildren(sessionId, missionId).size());
      
      SiteItem newCollection = service.newChild(sessionId, missionId1);
      
      Assert.assertFalse("HasChildren property on SiteItem should be false but returned true.", newCollection.getHasChildren());
      
      newCollection.setValue(UasComponent.NAME,"CollectionX");
      collectionId = newCollection.getId();
      service.applyWithParent(sessionId, newCollection, missionId);
      
      Assert.assertTrue(1 == service.getChildren(sessionId, missionId).size());
      
      
      newProject = service.edit(sessionId, projectId);
      Assert.assertTrue("HasChildren property on SiteItem should be true but returned false.", newProject.getHasChildren());
      
    }
    catch (RuntimeException re)
    {
      re.printStackTrace();
    }
    finally
    {

      if (collectionId != null)
      {
        service.remove(sessionId, collectionId);
      }
      
      if (missionId != null)
      {
        service.remove(sessionId, missionId);
      }
      
      if (projectId != null)
      {
        service.remove(sessionId, projectId);
      }

      logOutAdmin(sessionId);
    }
  }
  
  @Test
  public void testUploadMetadataCheck()
  {
    String sessionId = this.logInAdmin();
    
    String collectionId = null;

    try
    { 
      SiteItem newCollection = service.newChild(sessionId, missionId1);
            
      newCollection.setValue(UasComponent.NAME,"CollectionTest");
      collectionId = newCollection.getId();
      service.applyWithParent(sessionId, newCollection, missionId1);
      
      this.checkMetadata(sessionId, collectionId);
    }
    catch (RuntimeException re)
    {
      re.printStackTrace();
    }
    finally
    {

      if (collectionId != null)
      {
        service.remove(sessionId, collectionId);
      }

      logOutAdmin(sessionId);
    }
  }
  @Request(RequestType.SESSION)
  private void checkMetadata(String sessionId, String collectionId)
  {    
    
    SingleActor singleActor = GeoprismUser.getCurrentUser();
    
    WorkflowTask workflowTask = new WorkflowTask();
    workflowTask.setGeoprismUserId(singleActor.getOid());
    workflowTask.setStatus("Test Status");
    workflowTask.setTaskLabel("Test Task");
    workflowTask.setUpLoadId("123");
    workflowTask.setCollectionId(collectionId);
    workflowTask.apply();

    try
    {
      List<Mission> missionList = Mission.getMissingMetadata();
    
//      System.out.println("Missions that need metadata.");
//      System.out.println("----------------------------");
      
      Assert.assertTrue("Check for the Metadata XML file has failed.", missionList.size() > 0);
      for (Mission mission : missionList)
      {
        mission.lock();
        mission.setMetadataUploaded(true);
        mission.apply();
      }
      
      missionList = Mission.getMissingMetadata();
      Assert.assertTrue("Check for the Metadata XML file has failed.", missionList.size() == 0);
    }
    finally
    {
      workflowTask.delete();
    }
  }
  
//  @Test
//  public void testUpdate()
//  {
//    String sessionId = this.logInAdmin();
//    
//
//    try
//    {
//      SiteItem siteItem = service.edit(sessionId, siteId);
//      
//      siteItem.setValue(UasComponent.NAME,"Site_Unit_Test-X");
//   
//      service.update(sessionId, siteItem);  
//
//      siteItem = service.edit(sessionId, siteId);
//      
//      Assert.assertEquals("SiteItem is not correctly updated in the database", "Site_Unit_Test-X", siteItem.getValue(UasComponent.NAME));
//      
//      
//      siteItem.setValue(UasComponent.NAME,"Site_Unit_Test");
//      
//      service.update(sessionId, siteItem);  
//      
//      siteItem = service.edit(sessionId, siteId);
//      
//      Assert.assertEquals("SiteItem is not correctly updated in the database", "Site_Unit_Test", siteItem.getValue(UasComponent.NAME)); 
//      
//    }
//    finally
//    {
//      
//      logOutAdmin(sessionId);
//    }
//  }
  
  @Test
  public void testSiteTypeAndLabel()
  {
    String sessionId = this.logInAdmin();
    

    try
    {
      SiteItem siteItem = service.edit(sessionId, siteId);
      
      Assert.assertEquals("SiteItem did not return the type", "Site", siteItem.getType());
      
      Assert.assertEquals("SiteItem did not return the type label", "Site", siteItem.getTypeLabel()); 
      
    }
    finally
    {
      
      logOutAdmin(sessionId);
    }
  }
  
  /**
   * Logs in admin user and returns session id of the user.
   * 
   * @return
   */
  @Request
  private String logInAdmin()
  {
    return SessionFacade.logIn(USERNAME, PASSWORD, new Locale[] { CommonProperties.getDefaultLocale() });
  }
  
  /**
   * Log out the admin user with the given session id.
   * 
   * @param sessionId
   */
  @Request
  private void logOutAdmin(String sessionId)
  {
    SessionFacade.closeSession(sessionId);
  }
      
  
}
