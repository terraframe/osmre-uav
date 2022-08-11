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
package gov.geoplatform.uasdm.graph;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.business.rbac.UserDAO;
import com.runwaysdk.constants.UserInfo;
import com.runwaysdk.dataaccess.DuplicateDataException;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.resource.FileResource;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.bus.Bureau;
import gov.geoplatform.uasdm.bus.WorkflowAction;
import gov.geoplatform.uasdm.bus.WorkflowTask;
import gov.geoplatform.uasdm.mock.MockRemoteFileService;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.util.FileTestUtils;
import net.geoprism.GeoprismUser;

public class UploadArchiveTest
{
  // private static ProjectManagementService service;
  //
  // private static String siteId;
  //
  // private static String projectId1;
  //
  // private static String missionId1;

  private static String collectionId1;

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

  @BeforeClass
  @Request
  public static void classSetUp()
  {
    RemoteFileFacade.setService(new MockRemoteFileService());

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

    Bureau bureau = Bureau.getByKey("OSMRE");

    Site site = new Site();
    site.setName("Site_Unit_Test");
    site.setFolderName("Site_Unit_Test");
    site.setBureau(bureau);
    site.applyWithParent(null);
    // System.out.println("S3: "+site.getS3location());
    // siteId = site.getOid();

    Project project1 = new Project();
    project1.setName("Project1");
    project1.setFolderName("Project1");
    project1.applyWithParent(site);
    // System.out.println("S3: "+project1.getS3location());
    // projectId1 = project1.getOid();

    Project project2 = new Project();
    project2.setName("Project2");
    project2.setFolderName("Project2");
    project2.applyWithParent(site);

    Mission mission1 = new Mission();
    mission1.setName("Mission1");
    mission1.setFolderName("Mission1");
    mission1.applyWithParent(project1);
    // System.out.println("S3: "+mission1.getS3location());
    // missionId1 = mission1.getOid();

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
  public void testZipArchive() throws URISyntaxException
  {
    Collection collection = Collection.get(collectionId1);

    WorkflowTask task = new WorkflowTask();
    task.setGeoprismUser(newUser);
    task.setComponent(collection.getOid());
    task.setUploadId("testID2");
    task.setStatus("Test Status");
    task.apply();

    File file = FileTestUtils.getTestResourceFile(this.getClass().getResource("/small-fix-with-video.zip.test").toURI());

    final FileResource resource = new FileResource(file);

    List<String> results = collection.uploadArchive(task, resource, "raw");

    Assert.assertEquals(6, results.size());

    List<WorkflowAction> actions = task.getActions();

    Assert.assertEquals(0, actions.size());
  }

  @Test(expected = UnsupportedOperationException.class)
  @Request
  public void testUploadUnknownUploadTarget() throws URISyntaxException
  {
    Collection collection = Collection.get(collectionId1);

    WorkflowTask task = new WorkflowTask();
    task.setGeoprismUser(newUser);
    task.setComponent(collection.getOid());
    task.setUploadId("testID2");
    task.setStatus("Test Status");
    task.apply();

    File file = FileTestUtils.getTestResourceFile(this.getClass().getResource("/small-fix-with-video.zip.test").toURI());

    final FileResource resource = new FileResource(file);

    collection.uploadArchive(task, resource, "RAW");
  }

  @Test
  @Request
  public void testTarGzArchive() throws URISyntaxException
  {
    Collection collection = Collection.get(collectionId1);

    WorkflowTask task = new WorkflowTask();
    task.setGeoprismUser(newUser);
    task.setComponent(collection.getOid());
    task.setUploadId("testID");
    task.setStatus("Test Status");
    task.apply();

    File file = FileTestUtils.getTestResourceFile(this.getClass().getResource("/small-fix-with-video.tar.gz.test").toURI());

    final FileResource resource = new FileResource(file);

    List<String> results = collection.uploadArchive(task, resource, "raw");

    Assert.assertEquals(5, results.size());

    List<WorkflowAction> actions = task.getActions();

    Assert.assertEquals(0, actions.size());
  }

  @Test
  @Request
  public void testUploadOrtho() throws URISyntaxException
  {
    Collection collection = Collection.get(collectionId1);

    WorkflowTask task = new WorkflowTask();
    task.setGeoprismUser(newUser);
    task.setComponent(collection.getOid());
    task.setUploadId("testID2");
    task.setStatus("Test Status");
    task.apply();

    File file = new File(this.getClass().getResource("/odm_orthophoto_test.tif").toURI());

    final FileResource resource = new FileResource(file);

    List<String> results = collection.uploadArchive(task, resource, "ortho");

    Assert.assertEquals(1, results.size());

    List<WorkflowAction> actions = task.getActions();

    Assert.assertEquals(0, actions.size());
  }

  @Test
  @Request
  public void testUploadDem() throws URISyntaxException
  {
    Collection collection = Collection.get(collectionId1);

    WorkflowTask task = new WorkflowTask();
    task.setGeoprismUser(newUser);
    task.setComponent(collection.getOid());
    task.setUploadId("testID2");
    task.setStatus("Test Status");
    task.apply();

    File file = new File(this.getClass().getResource("/dsm.tif").toURI());

    final FileResource resource = new FileResource(file);

    List<String> results = collection.uploadArchive(task, resource, "dem");

    Assert.assertEquals(1, results.size());

    List<WorkflowAction> actions = task.getActions();

    Assert.assertEquals(0, actions.size());
  }

  @Test
  @Request
  public void testUploadPtcloud() throws URISyntaxException
  {
    Collection collection = Collection.get(collectionId1);

    WorkflowTask task = new WorkflowTask();
    task.setGeoprismUser(newUser);
    task.setComponent(collection.getOid());
    task.setUploadId("testID2");
    task.setStatus("Test Status");
    task.apply();

    File file = new File(this.getClass().getResource("/odm_georeferenced_model.laz").toURI());

    final FileResource resource = new FileResource(file);

    List<String> results = collection.uploadArchive(task, resource, "ptcloud");

    Assert.assertEquals(1, results.size());

    List<WorkflowAction> actions = task.getActions();

    Assert.assertEquals(0, actions.size());
  }
}
