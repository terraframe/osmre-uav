package gov.geoplatform.uasdm.bus;

import gov.geoplatform.uasdm.service.ProjectManagementService;
import gov.geoplatform.uasdm.view.SiteItem;

import java.util.List;
import java.util.Locale;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.business.rbac.UserDAO;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.constants.UserInfo;
import com.runwaysdk.dataaccess.DuplicateDataException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.SessionFacade;


public class TestSiteHierarchy
{
  
  private static ProjectManagementService service;

  private static String                   siteId;
  
  private static String                   projectId1;
  
  private static String                   missionId1;

  
  /**
   * The test user object
   */
  private static UserDAO                  newUser;
  
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
      // Create a new user
      newUser = UserDAO.newInstance();
      newUser.setValue(UserInfo.USERNAME, USERNAME);
      newUser.setValue(UserInfo.PASSWORD, PASSWORD);
      newUser.setValue(UserInfo.SESSION_LIMIT, Integer.toString(sessionLimit));
      newUser.apply();
      
      // Make the user an admin
      RoleDAO adminRole = RoleDAO.findRole(RoleDAO.ADMIN_ROLE).getBusinessDAO();
      adminRole.assignMember(newUser);
    }
    catch (DuplicateDataException e)
    {
      
    }

    Site site = new Site();
    site.setName("Cottonwood");
    site.apply();
    siteId = site.getOid();
    
    Project project1 = new Project();
    project1.setName("Project 1");
    project1.apply();
    site.addProjects(project1).apply();
    projectId1 = project1.getOid();
    
    
    Project project2 = new Project();
    project2.setName("Project 2");
    project2.apply();
    site.addProjects(project2).apply();
    
    Mission mission1 = new Mission();
    mission1.setName("Mission 1");
    mission1.apply();
    project1.addMissions(mission1).apply();
    missionId1 = mission1.getOid();
    
    Collection collection1 = new Collection();
    collection1.setName("Collection 1");
    collection1.apply();
    
    mission1.addCollections(collection1).apply();
    
  }
  
  @Transaction
  private static void createDefaultStructure()
  {
    Site site = new Site();
    site.setName("Cottonwood");
    site.apply();
    siteId = site.getOid();
    
    Project project1 = new Project();
    project1.setName("Project 1");
    project1.apply();
    site.addProjects(project1).apply();
    projectId1 = project1.getOid();
    
    Mission mission1 = new Mission();
    mission1.setName("Mission 1");
    mission1.apply();
    project1.addMissions(mission1).apply();
    missionId1 = mission1.getOid();
    
    Collection collection1 = new Collection();
    collection1.setName("Collection 1");
    collection1.apply();
    
    mission1.addCollections(collection1).apply();
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
    if (newUser.isAppliedToDB())
    {
      newUser.getBusinessDAO().delete();
    }
      
    QueryFactory qf = new QueryFactory();
    
    SiteQuery sq = new SiteQuery(qf);
    
    OIterator<? extends Site> i = sq.getIterator();
    
    try
    {
      for(Site site : i)
      {
        site.delete();
        System.out.println("Site deleted: "+site.getName());
      }
    }
    finally
    {
      i.close();
    }
    
    // Setup a default tree structure for demos
    createDefaultStructure();
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
      if (siteItem1.getName().equals("Project 1") || siteItem1.getName().equals("Project 2"))
      {
        siteItem1ValidName = true;
      }    
      
      boolean siteItem2ValidName = false;
      if (siteItem2.getName().equals("Project 1") || siteItem2.getName().equals("Project 2"))
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

      newProject.setName("Project X");
      projectId = newProject.getId();
      service.applyWithParent(sessionId, newProject, siteId);
      
      Assert.assertTrue(siteChildren + 1 == service.getChildren(sessionId, siteId).size());
      
      
      Assert.assertTrue(0 == service.getChildren(sessionId, projectId).size());
      
      SiteItem newMission = service.newChild(sessionId, projectId1);
      
      Assert.assertFalse("HasChildren property on SiteItem should be false but returned true.", newMission.getHasChildren());
      
      newMission.setName("Mission X");
      missionId = newMission.getId();
      service.applyWithParent(sessionId, newMission, projectId);
      
      Assert.assertTrue(1 == service.getChildren(sessionId, projectId).size());
      
      
      Assert.assertTrue(0 == service.getChildren(sessionId, missionId).size());
      
      SiteItem newCollection = service.newChild(sessionId, missionId1);
      
      Assert.assertFalse("HasChildren property on SiteItem should be false but returned true.", newCollection.getHasChildren());
      
      newCollection.setName("Collection X");
      collectionId = newCollection.getId();
      service.applyWithParent(sessionId, newCollection, missionId);
      
      Assert.assertTrue(1 == service.getChildren(sessionId, missionId).size());
      
      
      newProject = service.edit(sessionId, projectId);
      Assert.assertTrue("HasChildren property on SiteItem should be true but returned false.", newProject.getHasChildren());
      
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
  public void testUpdate()
  {
    String sessionId = this.logInAdmin();
    

    try
    {
      SiteItem siteItem = service.edit(sessionId, siteId);
      
      siteItem.setName("Cottonwood X");
   
      service.update(sessionId, siteItem);  

      siteItem = service.edit(sessionId, siteId);
      
      Assert.assertEquals("SiteItem is not correctly updated in the database", "Cottonwood X", siteItem.getName());
      
      
      siteItem.setName("Cottonwood");
      
      service.update(sessionId, siteItem);  
      
      siteItem = service.edit(sessionId, siteId);
      
      Assert.assertEquals("SiteItem is not correctly updated in the database", "Cottonwood", siteItem.getName()); 
      
    }
    finally
    {
      
      logOutAdmin(sessionId);
    }
  }
  
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
