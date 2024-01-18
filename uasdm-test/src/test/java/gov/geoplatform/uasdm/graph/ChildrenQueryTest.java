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

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.InstanceTestClassListener;
import gov.geoplatform.uasdm.SpringInstanceTestClassRunner;
import gov.geoplatform.uasdm.TestConfig;
import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.test.Area51DataSet;
import gov.geoplatform.uasdm.test.TestDataSet;
import net.geoprism.GeoprismUser;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class ChildrenQueryTest implements InstanceTestClassListener
{
  private static Area51DataSet testData;

  private Site                 site;

  private Project              project;

  private Mission              mission;

  private Collection           collection;

  @Override
  public void beforeClassSetup() throws Exception
  {
    testData = new Area51DataSet();
    testData.setUpSuiteData();
  }

  @Override
  public void afterClassSetup() throws Exception
  {
    if (testData != null)
    {
      testData.tearDownMetadata();
    }
  }

  @Before
  @Request
  public void setUp() throws Exception
  {
    testData.setUpInstanceData();

    site = (Site) Area51DataSet.SITE_AREA_51.getServerObject();
    project = (Project) Area51DataSet.PROJECT_DREAMLAND.getServerObject();
    mission = (Mission) Area51DataSet.MISSION_HAVE_DOUGHNUT.getServerObject();
    collection = Area51DataSet.COLLECTION_FISHBED.getServerObject();

    testData.logIn();

    TestDataSet.executeRequestAsUser(TestDataSet.USER_ADMIN, () -> {

      collection = Collection.get(collection.getOid());
      collection.setOwner(GeoprismUser.getCurrentUser());
      collection.apply();
    });

  }

  @After
  @Request
  public void tearDown()
  {
    testData.logOut();

    testData.tearDownInstanceData();
  }

  @Test
  @Request
  public void testCollectionCondition()
  {
    // Create the bureau condition
    JSONObject bureau = new JSONObject();
    bureau.put("field", Collection.COLLECTIONDATE);
    bureau.put("value", Util.formatIso8601(collection.getCollectionDate(), false));

    JSONArray array = new JSONArray();
    array.put(bureau);

    JSONObject conditions = new JSONObject();
    conditions.put("array", array);

    ChildrenQuery query = new ChildrenQuery(this.site, conditions.toString());

    System.out.println(query.getStatement());

    List<UasComponentIF> results = query.getResults();

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(project.getOid(), results.get(0).getOid());
  }

  @Test
  @Request
  public void testBadCollectionCondition()
  {
    // Create the bureau condition
    JSONObject bureau = new JSONObject();
    bureau.put("field", Collection.SENSOR);
    bureau.put("value", "BAD");

    JSONArray array = new JSONArray();
    array.put(bureau);

    JSONObject conditions = new JSONObject();
    conditions.put("array", array);

    ChildrenQuery query = new ChildrenQuery(this.mission, conditions.toString());

    System.out.println(query.getStatement());

    List<UasComponentIF> results = query.getResults();

    Assert.assertEquals(0, results.size());
  }

  @Test
  @Request
  public void testProjectCondition()
  {
    // Create the bureau condition
    JSONObject bureau = new JSONObject();
    bureau.put("field", Project.PROJECTTYPE);
    bureau.put("value", project.getProjectType());

    JSONArray array = new JSONArray();
    array.put(bureau);

    JSONObject conditions = new JSONObject();
    conditions.put("array", array);

    ChildrenQuery query = new ChildrenQuery(this.site, conditions.toString());

    System.out.println(query.getStatement());

    List<UasComponentIF> results = query.getResults();

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(project.getOid(), results.get(0).getOid());
  }

  @Test
  @Request
  public void testSensorCondition()
  {
    // Create the bureau condition
    JSONObject bureau = new JSONObject();
    bureau.put("field", Collection.SENSOR);
    bureau.put("value", collection.getSensor().getName());

    JSONArray array = new JSONArray();
    array.put(bureau);

    JSONObject conditions = new JSONObject();
    conditions.put("array", array);

    ChildrenQuery query = new ChildrenQuery(this.mission, conditions.toString());

    System.out.println(query.getStatement());

    List<UasComponentIF> results = query.getResults();

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(collection.getOid(), results.get(0).getOid());
  }

  @Test
  @Request
  public void testUavCondition()
  {
    // Create the bureau condition
    JSONObject bureau = new JSONObject();
    bureau.put("field", Collection.UAV);
    bureau.put("value", collection.getUav().getFaaNumber());

    JSONArray array = new JSONArray();
    array.put(bureau);

    JSONObject conditions = new JSONObject();
    conditions.put("array", array);

    ChildrenQuery query = new ChildrenQuery(this.mission, conditions.toString());

    System.out.println(query.getStatement());

    List<UasComponentIF> results = query.getResults();

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(collection.getOid(), results.get(0).getOid());
  }

  @Test
  @Request
  public void testPlatformCondition()
  {
    // Create the bureau condition
    JSONObject bureau = new JSONObject();
    bureau.put("field", UAV.PLATFORM);
    bureau.put("value", collection.getUav().getPlatform().getName());

    JSONArray array = new JSONArray();
    array.put(bureau);

    JSONObject conditions = new JSONObject();
    conditions.put("array", array);

    ChildrenQuery query = new ChildrenQuery(this.site, conditions.toString());

    System.out.println(query.getStatement());

    List<UasComponentIF> results = query.getResults();

    Assert.assertEquals(1, results.size());

    Assert.assertEquals(project.getOid(), results.get(0).getOid());
  }

  @Test
  @Request
  public void testNoCondition()
  {
    ChildrenQuery query = new ChildrenQuery(this.site, null);

    System.out.println(query.getStatement());

    List<UasComponentIF> results = query.getResults();

    Assert.assertEquals(1, results.size());

    Assert.assertEquals(project.getOid(), results.get(0).getOid());
  }

}
