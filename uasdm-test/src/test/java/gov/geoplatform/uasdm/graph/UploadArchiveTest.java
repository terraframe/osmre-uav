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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

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

import gov.geoplatform.uasdm.Area51DataTest;
import gov.geoplatform.uasdm.InstanceTestClassListener;
import gov.geoplatform.uasdm.SpringInstanceTestClassRunner;
import gov.geoplatform.uasdm.TestConfig;
import gov.geoplatform.uasdm.bus.Bureau;
import gov.geoplatform.uasdm.bus.WorkflowAction;
import gov.geoplatform.uasdm.bus.WorkflowTask;
import gov.geoplatform.uasdm.mock.MockIndex;
import gov.geoplatform.uasdm.mock.MockRemoteFileService;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.service.IndexService;
import gov.geoplatform.uasdm.test.Area51DataSet;
import gov.geoplatform.uasdm.util.FileTestUtils;
import net.geoprism.GeoprismUser;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class UploadArchiveTest extends Area51DataTest implements InstanceTestClassListener
{
  private Product              product;

  private Site                 site;

  private Project              project;

  private Mission              mission;

  private Collection           collection;

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
  
  @Before
  @Request
  public void setUp()
  {
    testData.setUpInstanceData();
    
    RemoteFileFacade.setService(new MockRemoteFileService());
    IndexService.setIndex(new MockIndex());

    createSiteHierarchyTransaction();

    site = (Site) Area51DataSet.SITE_AREA_51.getServerObject();
    project = (Project) Area51DataSet.PROJECT_DREAMLAND.getServerObject();
    mission = (Mission) Area51DataSet.MISSION_HAVE_DOUGHNUT.getServerObject();
    collection = Area51DataSet.COLLECTION_FISHBED.getServerObject();
    product = collection.getProducts().get(0);

    testData.logIn();
  }

  @After
  @Request
  public void tearDown()
  {
    testData.logOut();

    if (product != null)
    {
      product.delete();
    }

    testData.tearDownInstanceData();
  }

  @Request
  public void beforeClassSetup() throws Exception
  {
    RemoteFileFacade.setService(new MockRemoteFileService());
    IndexService.setIndex(new MockIndex());

    createSiteHierarchyTransaction();
  }

  @Transaction
  private void createSiteHierarchyTransaction()
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
  }

  @Request
  public void afterClassSetup() throws Exception
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
  public void testZipArchive() throws URISyntaxException, IOException
  {
    WorkflowTask task = new WorkflowTask();
    task.setGeoprismUser(newUser);
    task.setComponent(collection.getOid());
    task.setUploadId("testID2");
    task.setStatus("Test Status");
    task.apply();

    File file = FileTestUtils.createZip(this.getClass().getResource("/raw").toURI());

    final FileResource resource = new FileResource(file);

    List<String> results = collection.uploadArchive(task, resource, "RAW", product);

    Assert.assertEquals(5, results.size());

    List<WorkflowAction> actions = task.getActions();

    Assert.assertEquals(0, actions.size());
  }

  @Test(expected = UnsupportedOperationException.class)
  @Request
  public void testUploadUnknownUploadTarget() throws URISyntaxException, IOException
  {
    WorkflowTask task = new WorkflowTask();
    task.setGeoprismUser(newUser);
    task.setComponent(collection.getOid());
    task.setUploadId("testID2");
    task.setStatus("Test Status");
    task.apply();

    File file = FileTestUtils.createZip(this.getClass().getResource("/raw").toURI());

    final FileResource resource = new FileResource(file);

    collection.uploadArchive(task, resource, "RAW", product);
  }

  @Test
  @Request
  public void testTarGzArchive() throws URISyntaxException, IOException
  {
    WorkflowTask task = new WorkflowTask();
    task.setGeoprismUser(newUser);
    task.setComponent(collection.getOid());
    task.setUploadId("testID");
    task.setStatus("Test Status");
    task.apply();

    File file = FileTestUtils.createZip(this.getClass().getResource("/raw").toURI());

    final FileResource resource = new FileResource(file);

    List<String> results = collection.uploadArchive(task, resource, "raw", product);

    Assert.assertEquals(5, results.size());

    List<WorkflowAction> actions = task.getActions();

    Assert.assertEquals(0, actions.size());
  }

  @Test
  @Request
  public void testUploadOrtho() throws URISyntaxException
  {
    WorkflowTask task = new WorkflowTask();
    task.setGeoprismUser(newUser);
    task.setComponent(collection.getOid());
    task.setUploadId("testID2");
    task.setStatus("Test Status");
    task.apply();

    File file = new File(this.getClass().getResource("/odm_orthophoto_test.tif").toURI());

    final FileResource resource = new FileResource(file);

    List<String> results = collection.uploadArchive(task, resource, "ortho", product);

    Assert.assertEquals(1, results.size());

    List<WorkflowAction> actions = task.getActions();

    Assert.assertEquals(0, actions.size());
  }

  @Test
  @Request
  public void testUploadDem() throws URISyntaxException
  {
    WorkflowTask task = new WorkflowTask();
    task.setGeoprismUser(newUser);
    task.setComponent(collection.getOid());
    task.setUploadId("testID2");
    task.setStatus("Test Status");
    task.apply();

    File file = new File(this.getClass().getResource("/dsm.tif").toURI());

    final FileResource resource = new FileResource(file);

    List<String> results = collection.uploadArchive(task, resource, "dem", product);

    Assert.assertEquals(1, results.size());

    List<WorkflowAction> actions = task.getActions();

    Assert.assertEquals(0, actions.size());
  }

  @Test
  @Request
  public void testUploadPtcloud() throws URISyntaxException
  {
    WorkflowTask task = new WorkflowTask();
    task.setGeoprismUser(newUser);
    task.setComponent(collection.getOid());
    task.setUploadId("testID2");
    task.setStatus("Test Status");
    task.apply();

    File file = new File(this.getClass().getResource("/odm_georeferenced_model.laz").toURI());

    final FileResource resource = new FileResource(file);

    List<String> results = collection.uploadArchive(task, resource, "ptcloud", product);

    Assert.assertEquals(1, results.size());

    List<WorkflowAction> actions = task.getActions();

    Assert.assertEquals(0, actions.size());
  }
}
