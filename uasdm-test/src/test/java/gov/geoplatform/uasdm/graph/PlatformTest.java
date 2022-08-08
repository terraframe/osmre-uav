package gov.geoplatform.uasdm.graph;

import java.math.BigDecimal;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.GenericException;
import gov.geoplatform.uasdm.model.Page;
import junit.framework.Assert;

public class PlatformTest
{
  private static PlatformManufacturer manufacturer;

  private static PlatformType platformType;

  private static Platform platform;

  private static SensorType sensorType;

  private static Sensor sensor;

  @BeforeClass
  @Request
  public static void classSetup()
  {
    WaveLength waveLength = (WaveLength) WaveLength.getPage(new JSONObject()).getResults().get(0);

    sensorType = new SensorType();
    sensorType.setIsMultispectral(false);
    sensorType.setName("TEST");
    sensorType.apply();

    sensor = new Sensor();
    sensor.setName("TEST");
    sensor.setDescription("DESC");
    sensor.setSensorType(sensorType);
    sensor.setRealPixelSizeHeight(new BigDecimal(10));
    sensor.setRealPixelSizeWidth(new BigDecimal(15));
    sensor.setRealSensorHeight(new BigDecimal(1));
    sensor.setRealSensorWidth(new BigDecimal(5));
    sensor.apply();

    sensor.addSensorHasWaveLengthChild(waveLength).apply();

    manufacturer = new PlatformManufacturer();
    manufacturer.setName("TEST");
    manufacturer.apply();

    platformType = new PlatformType();
    platformType.setName("TEST");
    platformType.apply();

    platform = new Platform();
    platform.setName("TEST");
    platform.setDescription("DESC");
    platform.setManufacturer(manufacturer);
    platform.setPlatformType(platformType);
    platform.apply();

    platform.addPlatformHasSensorChild(sensor).apply();
  }

  @AfterClass
  @Request
  public static void classTeardown()
  {
    if (platform != null)
    {
      platform.delete();
    }

    if (platformType != null)
    {
      platformType.delete();
    }

    if (manufacturer != null)
    {
      manufacturer.delete();
    }

    if (sensor != null)
    {
      sensor.delete();
    }

    if (sensorType != null)
    {
      sensorType.delete();
    }
  }

  @Test
  @Request
  public void testGetAll()
  {
    JSONArray platforms = Platform.getAll();

    Assert.assertEquals(1, platforms.length());
  }

  @Test
  @Request
  public void testGetCount()
  {
    Assert.assertEquals(new Long(1L), Platform.getCount());
  }

  @Test
  @Request
  public void testDefaultGetPage()
  {
    Page<Platform> page = Platform.getPage(new JSONObject());

    Assert.assertNotNull(page);
    Assert.assertEquals(new Long(1L), page.getCount());
    Assert.assertEquals(new Integer(1), page.getPageNumber());
    Assert.assertEquals(new Integer(10), page.getPageSize());
    Assert.assertEquals(1, page.getResults().size());
  }

  @Test
  @Request
  public void testGetPageWithSizeLimit()
  {
    JSONObject criteria = new JSONObject();
    criteria.put("rows", 2);
    criteria.put("first", 2);

    Page<Platform> page = Platform.getPage(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(new Long(1L), page.getCount());
    Assert.assertEquals(new Integer(2), page.getPageNumber());
    Assert.assertEquals(new Integer(2), page.getPageSize());
    Assert.assertEquals(0, page.getResults().size());
  }

  @Test
  @Request
  public void testGetPageWithSort()
  {
    JSONObject criteria = new JSONObject();
    criteria.put("sortField", Platform.NAME);
    criteria.put("sortOrder", 0);

    Page<Platform> page = Platform.getPage(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(new Long(1L), page.getCount());
    Assert.assertEquals(new Integer(1), page.getPageNumber());
    Assert.assertEquals(new Integer(10), page.getPageSize());
    Assert.assertEquals(1, page.getResults().size());
    Assert.assertEquals("TEST", page.getResults().get(0).getName());
  }

  @Test
  @Request
  public void testGetPageWithMultiSort()
  {
    JSONObject sort1 = new JSONObject();
    sort1.put("field", Platform.NAME);
    sort1.put("order", 0);

    JSONObject sort2 = new JSONObject();
    sort2.put("field", Platform.SEQ);
    sort2.put("order", 0);

    JSONArray multiSortMeta = new JSONArray();
    multiSortMeta.put(sort1);
    multiSortMeta.put(sort2);

    JSONObject criteria = new JSONObject();
    criteria.put("multiSortMeta", multiSortMeta);

    Page<Platform> page = Platform.getPage(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(new Long(1L), page.getCount());
    Assert.assertEquals(new Integer(1), page.getPageNumber());
    Assert.assertEquals(new Integer(10), page.getPageSize());
    Assert.assertEquals(1, page.getResults().size());
    Assert.assertEquals("TEST", page.getResults().get(0).getName());
  }

  @Test
  @Request
  public void testGetPageWithContainsFilter()
  {
    JSONObject filter = new JSONObject();
    filter.put("value", "te");
    filter.put("matchMode", "contains");

    JSONObject filters = new JSONObject();
    filters.put(Platform.NAME, filter);

    JSONObject criteria = new JSONObject();
    criteria.put("filters", filters);

    Page<Platform> page = Platform.getPage(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(new Long(1L), page.getCount());
    Assert.assertEquals(new Integer(1), page.getPageNumber());
    Assert.assertEquals(new Integer(10), page.getPageSize());
    Assert.assertEquals(1, page.getResults().size());
    Assert.assertEquals("TEST", page.getResults().get(0).getName());
  }

  @Test
  @Request
  public void testGetPageWithEqualsFilter()
  {
    JSONObject filter = new JSONObject();
    filter.put("value", "TEST");
    filter.put("matchMode", "equals");

    JSONObject filters = new JSONObject();
    filters.put(Platform.NAME, filter);

    JSONObject criteria = new JSONObject();
    criteria.put("filters", filters);

    Page<Platform> page = Platform.getPage(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(new Long(1L), page.getCount());
    Assert.assertEquals(new Integer(1), page.getPageNumber());
    Assert.assertEquals(new Integer(10), page.getPageSize());
    Assert.assertEquals(1, page.getResults().size());
    Assert.assertEquals("TEST", page.getResults().get(0).getName());
  }

  @Test
  @Request
  public void testToJSON()
  {
    JSONObject object = platform.toJSON();

    Assert.assertNotNull(object);
    Assert.assertEquals(platform.getOid(), object.getString(Platform.OID));
    Assert.assertEquals(platform.getName(), object.getString(Platform.NAME));
    Assert.assertEquals(platform.getDescription(), object.getString(Platform.DESCRIPTION));
    Assert.assertNotNull(object.getString(Platform.DATECREATED));
    Assert.assertNotNull(object.getString(Platform.DATEUPDATED));
    Assert.assertEquals(platformType.getOid(), object.getString(Platform.PLATFORM_TYPE_OID));
    Assert.assertNotNull(object.getJSONObject(Platform.PLATFORMTYPE));
    Assert.assertEquals(manufacturer.getOid(), object.getString(Platform.MANUFACTURER));
    Assert.assertEquals(platform.getSeq(), new Long(object.getLong(Platform.SEQ)));

    // List<Sensor> sensors = this.getPlatformHasSensorChildSensors();
    //
    // JSONArray array = sensors.stream().map(w ->
    // w.getOid()).collect(Collector.of(JSONArray::new, JSONArray::put,
    // JSONArray::put));
    //
    // object.put("sensors", array);
    //
    // return object;

  }

  @Test
  @Request
  public void testIsPlatformManufacturerReferenced()
  {
    Assert.assertTrue(Platform.isPlatformManufacturerReferenced(manufacturer));
  }

  @Test
  @Request
  public void testIsPlatformTypeReferenced()
  {
    Assert.assertTrue(Platform.isPlatformTypeReferenced(platformType));
  }

  @Test
  @Request
  public void testGetPlatformManufacturer()
  {
    Assert.assertEquals(manufacturer.getOid(), platform.getManufacturer().getOid());
  }

  @Test
  @Request
  public void testGetPlatformType()
  {
    Assert.assertEquals(platformType.getOid(), platform.getPlatformType().getOid());
  }

  @Test
  @Request
  public void testApply()
  {
    JSONObject object = platform.toJSON();
    object.remove(Platform.OID);
    object.put(Platform.NAME, "UPDATE");

    Platform result = Platform.apply(object);

    Assert.assertNotNull(result);

    result.delete();
  }

  @Test(expected = GenericException.class)
  @Request
  public void testApplyNoSesnor()
  {
    JSONObject object = platform.toJSON();
    object.remove(Platform.OID);
    object.put("sensors", new JSONArray());

    Platform.apply(object);
  }

  @Test(expected = GenericException.class)
  @Request
  public void testDeletePlatformTypeInUse()
  {
    PlatformType.get(platformType.getOid()).delete();
  }

  @Test(expected = GenericException.class)
  @Request
  public void testDeletePlatformManufacturerInUse()
  {
    PlatformManufacturer.get(manufacturer.getOid()).delete();
  }

  @Test
  @Request
  public void testSensorReference()
  {
    JSONObject object = sensor.toJSON();
    JSONArray platforms = object.getJSONArray("platforms");

    Assert.assertEquals(1, platforms.length());

    JSONObject result = platforms.getJSONObject(0);

    Assert.assertEquals(platform.getName(), result.getString(Platform.NAME));
  }

}
