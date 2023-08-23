package gov.geoplatform.uasdm.graph;

import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.geom.Point;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.TestConfig;
import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.mock.MockRegistryConnectionBuilder;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.test.Area51DataSet;
import gov.geoplatform.uasdm.test.TestDataSet;
import net.geoprism.GeoprismUser;
import net.geoprism.graph.LabeledPropertyGraphSynchronization;
import net.geoprism.graph.LabeledPropertyGraphSynchronizationQuery;
import net.geoprism.graph.adapter.RegistryConnectorFactory;
import net.geoprism.graph.adapter.RegistryConnectorIF;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringJUnit4ClassRunner.class)
public class SiteQueryTest
{
  private static Area51DataSet                       testData;

  private static LabeledPropertyGraphSynchronization synchronization;

  private static boolean                             isSetup = false;

  private Site                                       site;

  private Project                                    project;

  private Collection                                 collection;

  @BeforeClass
  public static void setUpClass()
  {
    testData = new Area51DataSet();
    testData.setUpSuiteData();
  }

  @AfterClass
  public static void cleanUpClass()
  {
    if (synchronization != null)
    {
      TestDataSet.executeRequestAsUser(TestDataSet.USER_ADMIN, () -> {
        synchronization.delete();
      });

      synchronization = null;
    }

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
    collection = Area51DataSet.COLLECTION_FISHBED.getServerObject();

    testData.logIn();

    TestDataSet.executeRequestAsUser(TestDataSet.USER_ADMIN, () -> {

      collection = Collection.get(collection.getOid());
      collection.setOwner(GeoprismUser.getCurrentUser());
      collection.apply();

      if (isSetup && synchronization != null)
      {
        site.assignHierarchyParents(synchronization);
      }
    });

    if (!isSetup)
    {
      String url = "https://localhost:8443/georegistry/";

      LabeledPropertyGraphSynchronizationQuery query = new LabeledPropertyGraphSynchronizationQuery(new QueryFactory());
      query.WHERE(query.getUrl().EQ(url));

      if (query.getCount() > 0)
      {
        synchronization = query.getIterator().getAll().get(0);
      }
      else
      {
        TestDataSet.executeRequestAsUser(TestDataSet.USER_ADMIN, () -> {

          RegistryConnectorFactory.setBuilder(new MockRegistryConnectionBuilder());

          try (RegistryConnectorIF connector = RegistryConnectorFactory.getConnector(url))
          {
            synchronization = new LabeledPropertyGraphSynchronization();
            synchronization.setUrl(url);
            synchronization.setRemoteType("TEST");
            synchronization.getDisplayLabel().setValue("Test");
            synchronization.setRemoteEntry("TEST");
            synchronization.setForDate(new Date());
            synchronization.setRemoteVersion("TEST");
            synchronization.setVersionNumber(0);
            synchronization.apply();

            synchronization.execute();
          }
        });
      }
      isSetup = true;
    }

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

    SiteQuery query = new SiteQuery(conditions.toString());

    List<UasComponentIF> results = query.getResults();

    Assert.assertEquals(1, results.size());
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

    System.out.println(conditions);

    SiteQuery query = new SiteQuery(conditions.toString());

    List<UasComponentIF> results = query.getResults();

    Assert.assertEquals(1, results.size());
  }

  @Test
  @Request
  public void testSiteCondition()
  {
    Point geoPoint = site.getGeoPoint();

    Assert.assertNotNull(geoPoint);

    // Create the bounds condition
    JSONObject sw = new JSONObject();
    sw.put("lng", "0");
    sw.put("lat", "0");

    JSONObject ne = new JSONObject();
    ne.put("lng", "170");
    ne.put("lat", "80");

    JSONObject bbox = new JSONObject();
    bbox.put("_sw", sw);
    bbox.put("_ne", ne);

    JSONObject bounds = new JSONObject();
    bounds.put("field", "bounds");
    bounds.put("value", bbox);

    // Create the bureau condition
    JSONObject bureau = new JSONObject();
    bureau.put("field", "bureau");
    bureau.put("value", site.getBureauOid());

    // Create the bureau condition
    JSONObject name = new JSONObject();
    name.put("field", "name");
    name.put("value", site.getName());

    JSONArray array = new JSONArray();
    array.put(bounds);
    array.put(bureau);
    array.put(name);

    JSONObject conditions = new JSONObject();
    conditions.put("array", array);

    SiteQuery query = new SiteQuery(conditions.toString());

    List<UasComponentIF> results = query.getResults();

    Assert.assertEquals(1, results.size());
  }

  @Test
  @Request
  public void testCollectionConditionWithHierarchy()
  {
    // Create the bureau condition
    JSONObject bureau = new JSONObject();
    bureau.put("field", "bureau");
    bureau.put("value", site.getBureauOid());

    // Create the bureau condition
    JSONObject collectionDate = new JSONObject();
    collectionDate.put("field", Collection.COLLECTIONDATE);
    collectionDate.put("value", Util.formatIso8601(collection.getCollectionDate(), false));

    JSONArray array = new JSONArray();
    // array.put(bounds);
    array.put(bureau);
    array.put(collectionDate);

    JSONObject hierarchy = new JSONObject();
    hierarchy.put("oid", synchronization.getOid());
    hierarchy.put("uid", "a228787b-bfc2-484a-9b79-4ceabbd33431");

    JSONObject conditions = new JSONObject();
    conditions.put("array", array);
    conditions.put("hierarchy", hierarchy);

    SiteQuery query = new SiteQuery(conditions.toString());

    List<UasComponentIF> results = query.getResults();

    Assert.assertEquals(1, results.size());
  }

  @Test
  @Request
  public void testProjectConditionWithHierarchy()
  {
    // Create the bureau condition
    JSONObject bureau = new JSONObject();
    bureau.put("field", Project.PROJECTTYPE);
    bureau.put("value", project.getProjectType());

    JSONArray array = new JSONArray();
    array.put(bureau);

    JSONObject hierarchy = new JSONObject();
    hierarchy.put("oid", synchronization.getOid());
    hierarchy.put("uid", "a228787b-bfc2-484a-9b79-4ceabbd33431");

    JSONObject conditions = new JSONObject();
    conditions.put("array", array);
    conditions.put("hierarchy", hierarchy);

    SiteQuery query = new SiteQuery(conditions.toString());

    List<UasComponentIF> results = query.getResults();

    Assert.assertEquals(1, results.size());
  }

  @Test
  @Request
  public void testSiteConditionWithHierarchy()
  {
    Point geoPoint = site.getGeoPoint();

    Assert.assertNotNull(geoPoint);

    // Create the bounds condition
    JSONObject sw = new JSONObject();
    sw.put("lng", "0");
    sw.put("lat", "0");

    JSONObject ne = new JSONObject();
    ne.put("lng", "170");
    ne.put("lat", "80");

    JSONObject bbox = new JSONObject();
    bbox.put("_sw", sw);
    bbox.put("_ne", ne);

    JSONObject bounds = new JSONObject();
    bounds.put("field", "bounds");
    bounds.put("value", bbox);

    // Create the bureau condition
    JSONObject bureau = new JSONObject();
    bureau.put("field", "bureau");
    bureau.put("value", site.getBureauOid());

    // Create the bureau condition
    JSONObject name = new JSONObject();
    name.put("field", "name");
    name.put("value", site.getName());

    JSONArray array = new JSONArray();
    array.put(bounds);
    array.put(bureau);
    array.put(name);

    JSONObject hierarchy = new JSONObject();
    hierarchy.put("oid", synchronization.getOid());
    hierarchy.put("uid", "a228787b-bfc2-484a-9b79-4ceabbd33431");

    JSONObject conditions = new JSONObject();
    conditions.put("array", array);
    conditions.put("hierarchy", hierarchy);

    SiteQuery query = new SiteQuery(conditions.toString());

    List<UasComponentIF> results = query.getResults();

    Assert.assertEquals(1, results.size());
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

    System.out.println(conditions);

    SiteQuery query = new SiteQuery(conditions.toString());

    List<UasComponentIF> results = query.getResults();

    Assert.assertEquals(1, results.size());
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

    System.out.println(conditions);

    SiteQuery query = new SiteQuery(conditions.toString());

    List<UasComponentIF> results = query.getResults();

    Assert.assertEquals(1, results.size());
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

    System.out.println(conditions);

    SiteQuery query = new SiteQuery(conditions.toString());

    List<UasComponentIF> results = query.getResults();

    Assert.assertEquals(1, results.size());
  }

  @Test
  @Request
  public void testOwnerCondition()
  {
    // Create the bureau condition
    JSONObject bureau = new JSONObject();
    bureau.put("field", UasComponent.OWNER);
    bureau.put("value", collection.getOwnerOid());

    JSONArray array = new JSONArray();
    array.put(bureau);

    JSONObject conditions = new JSONObject();
    conditions.put("array", array);

    System.out.println(conditions);

    SiteQuery query = new SiteQuery(conditions.toString());

    List<UasComponentIF> results = query.getResults();

    Assert.assertEquals(1, results.size());
  }

}
