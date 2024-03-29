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
package gov.geoplatform.uasdm.graph;

import java.util.List;
import java.util.Locale;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.SmartExceptionDTO;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.business.rbac.UserDAO;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.dataaccess.DuplicateDataException;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.SessionFacade;

import gov.geoplatform.uasdm.InstanceTestClassListener;
import gov.geoplatform.uasdm.SpringInstanceTestClassRunner;
import gov.geoplatform.uasdm.TestConfig;
import gov.geoplatform.uasdm.bus.Bureau;
import gov.geoplatform.uasdm.bus.CollectionReportQuery;
import gov.geoplatform.uasdm.mock.MockIndex;
import gov.geoplatform.uasdm.mock.MockRemoteFileService;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.service.IndexService;
import gov.geoplatform.uasdm.service.ProjectManagementService;
import gov.geoplatform.uasdm.view.SiteItem;
import gov.geoplatform.uasdm.view.TreeComponent;
import net.geoprism.GeoprismUser;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class TestSiteHierarchy implements InstanceTestClassListener
{

  private static ProjectManagementService service;

  private static String siteId;

  private static String projectId1;

  private static String missionId1;

  /**
   * The test user object
   */
  private static GeoprismUser newUser;

  /**
   * The username for the user
   */
  private final static String USERNAME = "btables";

  /**
   * The password for the user
   */
  private final static String PASSWORD = "1234";

  private final static int sessionLimit = 2;

  private MockRemoteFileService fService;

  @Request
  public void beforeClassSetup() throws Exception
  {
    IndexService.setIndex(new MockIndex());
    
    createSiteHierarchyTransaction();

    service = new ProjectManagementService();
  }

  @Before
  public void setup()
  {
    fService = new MockRemoteFileService();

    RemoteFileFacade.setService(fService);
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
      adminRole.assignMember((UserDAO) BusinessFacade.getEntityDAO(newUser));
    }
    catch (DuplicateDataException e)
    {

    }

    Bureau bureau = Bureau.getByKey("OSMRE");

    Site site = new Site();
    site.setValue(UasComponent.NAME, "Site_Unit_Test");
    site.setValue(UasComponent.FOLDERNAME, "Site_Unit_Test");
    site.setBureau(bureau);
    site.applyWithParent(null);
    // System.out.println("S3: "+site.getS3location());
    siteId = site.getOid();

    Project project1 = new Project();
    project1.setValue(UasComponent.NAME, "Project1");
    project1.setValue(UasComponent.FOLDERNAME, "Project1");
    project1.applyWithParent(site);
    // System.out.println("S3: "+project1.getS3location());
    projectId1 = project1.getOid();

    Project project2 = new Project();
    project2.setValue(UasComponent.NAME, "Project2");
    project2.setValue(UasComponent.FOLDERNAME, "Project2");
    project2.applyWithParent(site);

    Mission mission1 = new Mission();
    mission1.setValue(UasComponent.NAME, "Mission1");
    mission1.setValue(UasComponent.FOLDERNAME, "Mission1");
    mission1.applyWithParent(project1);
    // System.out.println("S3: "+mission1.getS3location());
    missionId1 = mission1.getOid();

    Collection collection1 = new Collection();
    collection1.setValue(UasComponent.NAME, "Collection1");
    collection1.setValue(UasComponent.FOLDERNAME, "Collection1");
    collection1.applyWithParent(mission1);
    // System.out.println("S3: "+collection1.getS3location());

    // Imagery imagery1 = new Imagery();
    // imagery1.setValue(UasComponent.NAME, "Imagery1");
    // imagery1.setValue(UasComponent.FOLDERNAME, "Imagery1");
    // imagery1.applyWithParent(project1);
    // // System.out.println("S3: "+imageryId1.getS3location());
    // imageryId1 = imagery1.getOid();
  }

  @Request
  public void afterClassSetup() throws Exception
  {
    classTearDownTransaction();
  }

  @Transaction
  public static void classTearDownTransaction()
  {
    new CollectionReportQuery(new QueryFactory()).getIterator().forEach(r -> r.delete());
    
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Site.CLASS);

    final StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName());
    statement.append(" WHERE name = :name");

    final GraphQuery<Site> query = new GraphQuery<Site>(statement.toString());
    query.setParameter("name", "Site_Unit_Test");
    final List<Site> sites = query.getResults();

    for (Site site : sites)
    {
      site.delete();
      System.out.println("Site deleted: " + site.getName());
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

    final List<UasComponentIF> projects = site.getChildren();

    int projectCount = 0;
    int missionCount = 0;
    int collectionCount = 0;
    for (UasComponentIF project : projects)
    {
      projectCount++;

      final List<UasComponentIF> missions = project.getChildren();

      for (UasComponentIF mission : missions)
      {
        missionCount++;

        final List<UasComponentIF> collections = mission.getChildren();

        collectionCount += collections.size();
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
      List<TreeComponent> siteItems = service.getChildren(sessionId, siteId);

      Assert.assertEquals("Wrong number of projects returned", 2, siteItems.size());

      SiteItem siteItem1 = (SiteItem) siteItems.get(0);
      SiteItem siteItem2 = (SiteItem) siteItems.get(1);

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
      SiteItem newProject = service.newDefaultChild(sessionId, siteId);
      newProject.setValue(UasComponent.NAME, "Project 1223");
      newProject.setValue(UasComponent.FOLDERNAME, "Project ");
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

    String sitId = null;

    try
    {
      SiteItem newSite = service.newDefaultChild(sessionId, null);
      newSite.setValue(UasComponent.NAME, "Site_Unit_Test");
      newSite.setValue(UasComponent.FOLDERNAME, "Site_Unit_Test");
      newSite = service.applyWithParent(sessionId, newSite, null);
      sitId = newSite.getId();

      Assert.fail("A Site was created with a duplicate name");
    }
    catch (SmartExceptionDTO re)
    {
      System.out.println(re.getLocalizedMessage());
    }
    finally
    {
      if (sitId != null)
      {
        service.remove(sessionId, sitId);
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
      SiteItem newProject = service.newDefaultChild(sessionId, siteId);

      // Assert.assertFalse("HasChildren property on SiteItem should be false
      // but returned true.", newProject.getHasChildren());

      newProject.setValue(UasComponent.NAME, "ProjectX");
      newProject.setValue(UasComponent.FOLDERNAME, "ProjectX");
      newProject = service.applyWithParent(sessionId, newProject, siteId);
      projectId = newProject.getId();

      Assert.assertTrue(siteChildren + 1 == service.getChildren(sessionId, siteId).size());

      Assert.assertTrue(0 == service.getChildren(sessionId, projectId).size());

      SiteItem newMission = service.newDefaultChild(sessionId, projectId1);

      // Assert.assertTrue("HasChildren property on SiteItem should be true but
      // returned false.", newMission.getHasChildren());

      newMission.setValue(UasComponent.NAME, "MissionX");
      newMission.setValue(UasComponent.FOLDERNAME, "MissionX");
      newMission = service.applyWithParent(sessionId, newMission, projectId);
      missionId = newMission.getId();

      Assert.assertTrue(1 == service.getChildren(sessionId, projectId).size());

      Assert.assertTrue(0 == service.getChildren(sessionId, missionId).size());

      SiteItem newCollection = service.newDefaultChild(sessionId, missionId1);

      // Assert.assertTrue("HasChildren property on SiteItem should be true but
      // returned false.", newCollection.getHasChildren());

      newCollection.setValue(UasComponent.NAME, "CollectionX");
      newCollection.setValue(UasComponent.FOLDERNAME, "CollectionX");
      newCollection = service.applyWithParent(sessionId, newCollection, missionId);
      collectionId = newCollection.getId();

      Assert.assertTrue(1 == service.getChildren(sessionId, missionId).size());

      newProject = service.edit(sessionId, projectId);
      // Assert.assertTrue("HasChildren property on SiteItem should be true but
      // returned false.", newProject.getHasChildren());

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

  // @Test
  // public void testServiceNewImagery()
  // {
  // String sessionId = this.logInAdmin();
  //
  // String projectId = null;
  //
  // String imageryId = null;
  //
  // try
  // {
  // int siteChildren = service.getChildren(sessionId, siteId).size();
  // SiteItem newProject = service.newDefaultChild(sessionId, siteId);
  //
  //// Assert.assertFalse("HasChildren property on SiteItem should be false but
  // returned true.", newProject.getHasChildren());
  //
  // newProject.setValue(UasComponent.NAME, "ProjectX1");
  // newProject.setValue(UasComponent.FOLDERNAME, "ProjectX1");
  // projectId = newProject.getId();
  // newProject = service.applyWithParent(sessionId, newProject, siteId);
  //
  // Assert.assertTrue(siteChildren + 1 == service.getChildren(sessionId,
  // siteId).size());
  //
  // Assert.assertTrue(0 == service.getChildren(sessionId, projectId).size());
  //
  // SiteItem newImagery = service.newChild(sessionId, projectId1,
  // IMAGERY_CLASS_NAME);
  //
  //// Assert.assertTrue("HasChildren property on Imagery should be true but
  // returned false.", newImagery.getHasChildren());
  //
  // newImagery.setValue(UasComponent.NAME, "ImageryX");
  // newImagery.setValue(UasComponent.FOLDERNAME, "MissionX");
  // imageryId = newImagery.getId();
  // service.applyWithParent(sessionId, newImagery, projectId);
  //
  // Assert.assertTrue(1 == service.getChildren(sessionId, projectId).size());
  //
  // Assert.assertTrue(0 == service.getChildren(sessionId, imageryId).size());
  //
  // newProject = service.edit(sessionId, projectId);
  //// Assert.assertTrue("HasChildren property on SiteItem should be true but
  // returned false.", newProject.getHasChildren());
  //
  // }
  // catch (RuntimeException re)
  // {
  // re.printStackTrace();
  // }
  // finally
  // {
  // if (imageryId != null)
  // {
  // service.remove(sessionId, imageryId);
  // }
  //
  // if (projectId != null)
  // {
  // service.remove(sessionId, projectId);
  // }
  //
  // logOutAdmin(sessionId);
  // }
  // }

  @Test
  public void testUploadMetadataCheck()
  {
    String sessionId = this.logInAdmin();

    String collectionId = null;

    try
    {
      SiteItem newCollection = service.newDefaultChild(sessionId, missionId1);

      newCollection.setValue(UasComponent.NAME, "CollectionTest");
      newCollection.setValue(UasComponent.FOLDERNAME, "CollectionTest");
      newCollection = service.applyWithParent(sessionId, newCollection, missionId1);

      collectionId = newCollection.getId();

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
    //
    // SingleActor singleActor = GeoprismUser.getCurrentUser();
    //
    // WorkflowTask workflowTask = new WorkflowTask();
    // workflowTask.setGeoprismUserId(singleActor.getOid());
    // workflowTask.setStatus("Test Status");
    // workflowTask.setTaskLabel("Test Task");
    // workflowTask.setUploadId("123");
    // workflowTask.setCollectionId(collectionId);
    // workflowTask.apply();
    //
    // try
    // {
    // List<Mission> missionList = Mission.getMissingMetadata();
    //
    // // System.out.println("Missions that need metadata.");
    // // System.out.println("----------------------------");
    //
    // Assert.assertTrue("Check for the Metadata XML file has failed.",
    // missionList.size() > 0);
    // for (Mission mission : missionList)
    // {
    // mission.lock();
    // mission.setMetadataUploaded(true);
    // mission.apply();
    // }
    //
    // missionList = Mission.getMissingMetadata();
    // Assert.assertTrue("Check for the Metadata XML file has failed.",
    // missionList.size() == 0);
    // }
    // finally
    // {
    // workflowTask.delete();
    // }
  }

  // @Test
  // public void testUpdate()
  // {
  // String sessionId = this.logInAdmin();
  //
  //
  // try
  // {
  // SiteItem siteItem = service.edit(sessionId, siteId);
  //
  // siteItem.setValue(UasComponent.NAME,"Site_Unit_Test-X");
  //
  // service.update(sessionId, siteItem);
  //
  // siteItem = service.edit(sessionId, siteId);
  //
  // Assert.assertEquals("SiteItem is not correctly updated in the database",
  // "Site_Unit_Test-X", siteItem.getValue(UasComponent.NAME));
  //
  //
  // siteItem.setValue(UasComponent.NAME,"Site_Unit_Test");
  //
  // service.update(sessionId, siteItem);
  //
  // siteItem = service.edit(sessionId, siteId);
  //
  // Assert.assertEquals("SiteItem is not correctly updated in the database",
  // "Site_Unit_Test", siteItem.getValue(UasComponent.NAME));
  //
  // }
  // finally
  // {
  //
  // logOutAdmin(sessionId);
  // }
  // }

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
    return SessionFacade.logIn(USERNAME, PASSWORD, new Locale[] {
        CommonProperties.getDefaultLocale()
    });
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
