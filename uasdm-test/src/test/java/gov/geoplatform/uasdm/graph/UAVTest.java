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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.ProblemException;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.GenericException;
import gov.geoplatform.uasdm.model.Page;
import gov.geoplatform.uasdm.test.Area51DataSet;
import org.junit.Assert;

public class UAVTest
{
  private static Area51DataSet testData;

  @BeforeClass
  public static void setUpClass()
  {
    testData = new Area51DataSet();
    testData.setUpSuiteData();
  }

  @AfterClass
  public static void cleanUpClass()
  {
    if (testData != null)
    {
      testData.tearDownMetadata();
    }
  }

  @Before
  public void setUp()
  {
    testData.setUpInstanceData();
  }

  @After
  public void tearDown()
  {
    testData.tearDownInstanceData();
  }

  @Test
  @Request
  public void testGetCount()
  {
    Assert.assertEquals(Long.valueOf(1L), UAV.getCount());
  }

  @Test
  @Request
  public void testDefaultGetPage()
  {
    Page<UAVPageView> page = UAV.getPage(new JSONObject());

    Assert.assertNotNull(page);
    Assert.assertEquals(Long.valueOf(1L), page.getCount());
    Assert.assertEquals(Integer.valueOf(1), page.getPageNumber());
    Assert.assertEquals(Integer.valueOf(10), page.getPageSize());
    Assert.assertEquals(1, page.getResults().size());
  }

  @Test
  @Request
  public void testGetPageWithSizeLimit()
  {
    JSONObject criteria = new JSONObject();
    criteria.put("rows", 2);
    criteria.put("first", 2);

    Page<UAVPageView> page = UAV.getPage(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(Long.valueOf(1L), page.getCount());
    Assert.assertEquals(Integer.valueOf(2), page.getPageNumber());
    Assert.assertEquals(Integer.valueOf(2), page.getPageSize());
    Assert.assertEquals(0, page.getResults().size());
  }

  @Test
  @Request
  public void testGetPageWithSort()
  {
    JSONObject criteria = new JSONObject();
    criteria.put("sortField", UAV.FAANUMBER);
    criteria.put("sortOrder", 0);

    Page<UAVPageView> page = UAV.getPage(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(Long.valueOf(1L), page.getCount());
    Assert.assertEquals(Integer.valueOf(1), page.getPageNumber());
    Assert.assertEquals(Integer.valueOf(10), page.getPageSize());
    Assert.assertEquals(1, page.getResults().size());
    Assert.assertEquals(Area51DataSet.UAV.getFaaNumber(), page.getResults().get(0).getUav().getFaaNumber());
  }

  @Test
  @Request
  public void testGetPageWithMultiSort()
  {
    JSONObject sort1 = new JSONObject();
    sort1.put("field", UAV.FAANUMBER);
    sort1.put("order", 0);

    JSONObject sort2 = new JSONObject();
    sort2.put("field", UAV.SEQ);
    sort2.put("order", 0);

    JSONArray multiSortMeta = new JSONArray();
    multiSortMeta.put(sort1);
    multiSortMeta.put(sort2);

    JSONObject criteria = new JSONObject();
    criteria.put("multiSortMeta", multiSortMeta);

    Page<UAVPageView> page = UAV.getPage(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(Long.valueOf(1L), page.getCount());
    Assert.assertEquals(Integer.valueOf(1), page.getPageNumber());
    Assert.assertEquals(Integer.valueOf(10), page.getPageSize());
    Assert.assertEquals(1, page.getResults().size());
    Assert.assertEquals(Area51DataSet.UAV.getFaaNumber(), page.getResults().get(0).getUav().getFaaNumber());
  }

  @Test
  @Request
  public void testGetPageWithContainsFilter()
  {
    JSONObject filter = new JSONObject();
    filter.put("value", Area51DataSet.UAV.getFaaNumber());
    filter.put("matchMode", "contains");

    JSONObject filters = new JSONObject();
    filters.put(UAV.FAANUMBER, filter);

    JSONObject criteria = new JSONObject();
    criteria.put("filters", filters);

    Page<UAVPageView> page = UAV.getPage(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(Long.valueOf(1L), page.getCount());
    Assert.assertEquals(Integer.valueOf(1), page.getPageNumber());
    Assert.assertEquals(Integer.valueOf(10), page.getPageSize());
    Assert.assertEquals(1, page.getResults().size());
    Assert.assertEquals(Area51DataSet.UAV.getFaaNumber(), page.getResults().get(0).getUav().getFaaNumber());
  }

  @Test
  @Request
  public void testGetPageWithEqualsFilter()
  {
    JSONObject filter = new JSONObject();
    filter.put("value", Area51DataSet.UAV.getFaaNumber());
    filter.put("matchMode", "equals");

    JSONObject filters = new JSONObject();
    filters.put(UAV.FAANUMBER, filter);

    JSONObject criteria = new JSONObject();
    criteria.put("filters", filters);

    Page<UAVPageView> page = UAV.getPage(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(Long.valueOf(1L), page.getCount());
    Assert.assertEquals(Integer.valueOf(1), page.getPageNumber());
    Assert.assertEquals(Integer.valueOf(10), page.getPageSize());
    Assert.assertEquals(1, page.getResults().size());
    Assert.assertEquals(Area51DataSet.UAV.getFaaNumber(), page.getResults().get(0).getUav().getFaaNumber());
  }

  @Test
  @Request
  public void testToJSON()
  {
    UAV uav = Area51DataSet.UAV.getServerObject();
    JSONObject object = uav.toJSON();

    Assert.assertNotNull(object);
    Assert.assertEquals(uav.getOid(), object.getString(UAV.OID));
    Assert.assertEquals(uav.getFaaNumber(), object.getString(UAV.FAANUMBER));
    Assert.assertEquals(uav.getSerialNumber(), object.getString(UAV.SERIALNUMBER));
    Assert.assertEquals(uav.getDescription(), object.getString(UAV.DESCRIPTION));
    Assert.assertEquals(uav.getPlatform().getOid(), object.getString(UAV.PLATFORM));
    Assert.assertEquals(uav.getBureau().getOid(), object.getString(UAV.BUREAU));
    Assert.assertEquals(uav.getSeq(), Long.valueOf(object.getLong(UAV.SEQ)));
  }

  @Test
  @Request
  public void testToView()
  {
    UAV uav = Area51DataSet.UAV.getServerObject();
    Platform platform = Area51DataSet.PLATFORM.getServerObject();
    PlatformType platformType = platform.getPlatformType();
    PlatformManufacturer manufacturer = platform.getManufacturer();

    JSONObject object = uav.toView();

    Assert.assertNotNull(object);
    Assert.assertEquals(uav.getOid(), object.getString(UAV.OID));
    Assert.assertEquals(uav.getFaaNumber(), object.getString(UAV.FAANUMBER));
    Assert.assertEquals(uav.getSerialNumber(), object.getString(UAV.SERIALNUMBER));
    Assert.assertEquals(platform.getName(), object.getString(Platform.NAME));
    Assert.assertEquals(platformType.getName(), object.getString(Platform.PLATFORMTYPE));
    Assert.assertEquals(manufacturer.getName(), object.getString(Platform.MANUFACTURER));
    Assert.assertEquals(uav.getBureau().getName(), object.getString(UAV.BUREAU));
  }

  @Test
  @Request
  public void testSearchFaaNumber()
  {
    List<UAV> results = UAV.search(Area51DataSet.UAV.getFaaNumber(), UAV.FAANUMBER);

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(Area51DataSet.UAV.getServerObject().getOid(), results.get(0).getOid());
  }

  @Test
  @Request
  public void testSearchFaaNumber_Bad()
  {
    List<UAV> results = UAV.search(Area51DataSet.UAV.getFaaNumber() + "BAD", UAV.FAANUMBER);

    Assert.assertEquals(0, results.size());
  }

  @Test
  @Request
  public void testSearchSerialNumber()
  {
    List<UAV> results = UAV.search(Area51DataSet.UAV.getSerialNumber(), UAV.SERIALNUMBER);

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(Area51DataSet.UAV.getServerObject().getOid(), results.get(0).getOid());
  }

  @Test(expected = GenericException.class)
  @Request
  public void testSearchUnknownField()
  {
    UAV.search(Area51DataSet.UAV.getSerialNumber(), "blarg");
  }

  @Test
  @Request
  public void testGetMetadataOptions()
  {
    UAV uav = Area51DataSet.UAV.getServerObject();
    Platform platform = Area51DataSet.PLATFORM.getServerObject();
    PlatformType platformType = platform.getPlatformType();
    Sensor sensor = Area51DataSet.SENSOR.getServerObject();

    JSONObject object = uav.getMetadataOptions();

    Assert.assertNotNull(object);
    Assert.assertEquals(uav.getOid(), object.getString(UAV.OID));
    Assert.assertEquals(uav.getFaaNumber(), object.getString(UAV.FAANUMBER));
    Assert.assertEquals(uav.getSerialNumber(), object.getString(UAV.SERIALNUMBER));
    Assert.assertEquals(platform.getName(), object.getString(UAV.PLATFORM));
    Assert.assertEquals(platformType.getName(), object.getString(Platform.PLATFORMTYPE));
    Assert.assertEquals(uav.getBureau().getDisplayLabel(), object.getString(UAV.BUREAU));

    JSONArray sensors = object.getJSONArray("sensors");

    Assert.assertEquals(1, sensors.length());

    JSONObject sensorObject = sensors.getJSONObject(0);

    Assert.assertEquals(sensor.getOid(), sensorObject.getString(Sensor.OID));
    Assert.assertEquals(sensor.getName(), sensorObject.getString(Sensor.NAME));
  }

  @Test
  @Request
  public void testIsPlatformReferenced()
  {
    Assert.assertTrue(UAV.isPlatformReferenced(Area51DataSet.PLATFORM.getServerObject()));
  }

  @Test
  @Request
  public void testGetPlatform()
  {
    Assert.assertEquals(Area51DataSet.PLATFORM.getServerObject().getOid(), Area51DataSet.UAV.getServerObject().getPlatform().getOid());
  }

  @Test
  @Request
  public void testApply()
  {
    JSONObject object = Area51DataSet.UAV.getServerObject().toJSON();
    object.remove(UAV.OID);

    UAV result = UAV.apply(object);

    Assert.assertNotNull(result);

    result.delete();
  }

  @Test(expected = ProblemException.class)
  @Request
  public void testApplyNoPlatform()
  {
    JSONObject object = Area51DataSet.UAV.getServerObject().toJSON();
    object.remove(UAV.OID);
    object.remove(UAV.PLATFORM);

    UAV.apply(object);
  }

  @Test(expected = GenericException.class)
  @Request
  public void testDeletePlatformInUse()
  {
    Area51DataSet.PLATFORM.getServerObject().delete();
  }

  @Test(expected = GenericException.class)
  @Request
  public void testDeleteUAVInUse()
  {
    Area51DataSet.UAV.getServerObject().delete();
  }
}
