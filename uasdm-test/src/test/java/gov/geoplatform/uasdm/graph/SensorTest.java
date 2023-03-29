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

import java.math.BigDecimal;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.GenericException;
import gov.geoplatform.uasdm.model.Page;
import org.junit.Assert;

public class SensorTest
{
  private static SensorType type;

  private static Sensor sensor;

  @BeforeClass
  @Request
  public static void classSetup()
  {
    WaveLength waveLength = (WaveLength) WaveLength.getPage(new JSONObject()).getResults().get(0);

    type = new SensorType();
    type.setIsMultispectral(false);
    type.setName("TEST");
    type.apply();

    sensor = new Sensor();
    sensor.setName("TEST");
    sensor.setDescription("DESC");
    sensor.setSensorType(type);
    sensor.setRealPixelSizeHeight(new BigDecimal(10));
    sensor.setRealPixelSizeWidth(new BigDecimal(15));
    sensor.setRealSensorHeight(new BigDecimal(1));
    sensor.setRealSensorWidth(new BigDecimal(5));
    sensor.apply();

    sensor.addSensorHasWaveLengthChild(waveLength).apply();
  }

  @AfterClass
  @Request
  public static void classTeardown()
  {
    if (sensor != null)
    {
      sensor.delete();
    }

    if (type != null)
    {
      type.delete();
    }
  }

  @Test
  @Request
  public void testGetAll()
  {
    List<Sensor> sensors = Sensor.getAll();

    Assert.assertEquals(1, sensors.size());
  }

  @Test
  @Request
  public void testGetCount()
  {
    Assert.assertEquals(Long.valueOf(1L), Sensor.getCount());
  }

  @Test
  @Request
  public void testDefaultGetPage()
  {
    Page<SensorPageView> page = Sensor.getPage(new JSONObject());

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

    Page<SensorPageView> page = Sensor.getPage(criteria);

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
    criteria.put("sortField", Sensor.NAME);
    criteria.put("sortOrder", 0);

    Page<SensorPageView> page = Sensor.getPage(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(Long.valueOf(1L), page.getCount());
    Assert.assertEquals(Integer.valueOf(1), page.getPageNumber());
    Assert.assertEquals(Integer.valueOf(10), page.getPageSize());
    Assert.assertEquals(1, page.getResults().size());
    Assert.assertEquals("TEST", page.getResults().get(0).getSensor().getName());
  }

  @Test
  @Request
  public void testGetPageWithMultiSort()
  {
    JSONObject sort1 = new JSONObject();
    sort1.put("field", Sensor.NAME);
    sort1.put("order", 0);

    JSONObject sort2 = new JSONObject();
    sort2.put("field", Sensor.SEQ);
    sort2.put("order", 0);

    JSONArray multiSortMeta = new JSONArray();
    multiSortMeta.put(sort1);
    multiSortMeta.put(sort2);

    JSONObject criteria = new JSONObject();
    criteria.put("multiSortMeta", multiSortMeta);

    Page<SensorPageView> page = Sensor.getPage(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(Long.valueOf(1L), page.getCount());
    Assert.assertEquals(Integer.valueOf(1), page.getPageNumber());
    Assert.assertEquals(Integer.valueOf(10), page.getPageSize());
    Assert.assertEquals(1, page.getResults().size());
    Assert.assertEquals("TEST", page.getResults().get(0).getSensor().getName());
  }

  @Test
  @Request
  public void testGetPageWithContainsFilter()
  {
    JSONObject filter = new JSONObject();
    filter.put("value", "te");
    filter.put("matchMode", "contains");

    JSONObject filters = new JSONObject();
    filters.put(Sensor.NAME, filter);

    JSONObject criteria = new JSONObject();
    criteria.put("filters", filters);

    Page<SensorPageView> page = Sensor.getPage(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(Long.valueOf(1L), page.getCount());
    Assert.assertEquals(Integer.valueOf(1), page.getPageNumber());
    Assert.assertEquals(Integer.valueOf(10), page.getPageSize());
    Assert.assertEquals(1, page.getResults().size());
    Assert.assertEquals("TEST", page.getResults().get(0).getSensor().getName());
  }

  @Test
  @Request
  public void testGetPageWithEqualsFilter()
  {
    JSONObject filter = new JSONObject();
    filter.put("value", "TEST");
    filter.put("matchMode", "equals");

    JSONObject filters = new JSONObject();
    filters.put(Sensor.NAME, filter);

    JSONObject criteria = new JSONObject();
    criteria.put("filters", filters);

    Page<SensorPageView> page = Sensor.getPage(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(Long.valueOf(1L), page.getCount());
    Assert.assertEquals(Integer.valueOf(1), page.getPageNumber());
    Assert.assertEquals(Integer.valueOf(10), page.getPageSize());
    Assert.assertEquals(1, page.getResults().size());
    Assert.assertEquals("TEST", page.getResults().get(0).getSensor().getName());
  }

  @Test
  @Request
  public void testToJSON()
  {
    JSONObject object = sensor.toJSON();

    Assert.assertNotNull(object);
    Assert.assertEquals(sensor.getOid(), object.getString(Sensor.OID));
    Assert.assertEquals(sensor.getName(), object.getString(Sensor.NAME));
    Assert.assertEquals(sensor.getDescription(), object.getString(Sensor.DESCRIPTION));
    Assert.assertNotNull(object.getString(Sensor.DATECREATED));
    Assert.assertNotNull(object.getString(Sensor.DATEUPDATED));
    Assert.assertEquals(type.getOid(), object.getString(Sensor.SENSOR_TYPE_OID));
    Assert.assertNotNull(object.getJSONObject(Sensor.SENSORTYPE));
    Assert.assertEquals(sensor.getSeq(), Long.valueOf(object.getLong(Sensor.SEQ)));

    JSONArray wavelengths = object.getJSONArray("wavelengths");

    Assert.assertEquals(1, wavelengths.length());

    JSONArray platforms = object.getJSONArray("platforms");

    Assert.assertEquals(0, platforms.length());
  }

  @Test
  @Request
  public void testIsSensorTypeReferenced()
  {
    Assert.assertTrue(Sensor.isSensorTypeReferenced(type));
  }

  @Test
  @Request
  public void testGetSensorType()
  {
    Assert.assertEquals(type.getOid(), sensor.getSensorType().getOid());
  }

  @Test(expected = GenericException.class)
  @Request
  public void testDeleteSensorTypeInUse()
  {
    SensorType.get(type.getOid()).delete();
  }

  @Test
  @Request
  public void testApply()
  {
    JSONObject object = sensor.toJSON();
    object.remove(Sensor.OID);
    object.put(Sensor.NAME, "UPDATE");

    Sensor result = Sensor.apply(object);

    Assert.assertNotNull(result);

    result.delete();
  }

  @Test(expected = GenericException.class)
  @Request
  public void testApplyNoWaveLength()
  {
    JSONObject object = sensor.toJSON();
    object.remove(Sensor.OID);
    object.put("wavelengths", new JSONArray());

    Sensor.apply(object);
  }

}
