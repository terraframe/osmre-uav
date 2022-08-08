package gov.geoplatform.uasdm.graph;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.model.Page;
import junit.framework.Assert;

public class SensorTypeTest
{

  @Test
  @Request
  public void testGetAll()
  {
    JSONArray sensorTypes = SensorType.getAll();

    Assert.assertEquals(3, sensorTypes.length());
  }

  @Test
  @Request
  public void testGetName()
  {
    SensorType result = SensorType.getByName("CMOS");

    Assert.assertNotNull(result);
    Assert.assertEquals("CMOS", result.getName());
  }

  @Test
  @Request
  public void testGetBadName()
  {
    SensorType result = SensorType.getByName("CMOSz");

    Assert.assertNull(result);
  }

  @Test
  @Request
  public void testGetCount()
  {
    Assert.assertEquals(new Long(3L), SensorType.getCount());
  }

  @Test
  @Request
  public void testDefaultGetPage()
  {
    Page<Classification> page = SensorType.getPage(new JSONObject());

    Assert.assertNotNull(page);
    Assert.assertEquals(new Long(3L), page.getCount());
    Assert.assertEquals(new Integer(1), page.getPageNumber());
    Assert.assertEquals(new Integer(10), page.getPageSize());
    Assert.assertEquals(3, page.getResults().size());
  }

  @Test
  @Request
  public void testGetPageWithSizeLimit()
  {
    JSONObject criteria = new JSONObject();
    criteria.put("rows", 2);
    criteria.put("first", 2);

    Page<Classification> page = SensorType.getPage(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(new Long(3L), page.getCount());
    Assert.assertEquals(new Integer(2), page.getPageNumber());
    Assert.assertEquals(new Integer(2), page.getPageSize());
    Assert.assertEquals(1, page.getResults().size());
  }

  @Test
  @Request
  public void testGetPageWithSort()
  {
    JSONObject criteria = new JSONObject();
    criteria.put("sortField", SensorType.NAME);
    criteria.put("sortOrder", 0);

    Page<Classification> page = SensorType.getPage(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(new Long(3L), page.getCount());
    Assert.assertEquals(new Integer(1), page.getPageNumber());
    Assert.assertEquals(new Integer(10), page.getPageSize());
    Assert.assertEquals(3, page.getResults().size());
    Assert.assertEquals("Multispectral", page.getResults().get(0).getName());
  }

  @Test
  @Request
  public void testGetPageWithMultiSort()
  {
    JSONObject sort1 = new JSONObject();
    sort1.put("field", SensorType.NAME);
    sort1.put("order", 0);

    JSONObject sort2 = new JSONObject();
    sort2.put("field", SensorType.SEQ);
    sort2.put("order", 0);

    JSONArray multiSortMeta = new JSONArray();
    multiSortMeta.put(sort1);
    multiSortMeta.put(sort2);

    JSONObject criteria = new JSONObject();
    criteria.put("multiSortMeta", multiSortMeta);

    Page<Classification> page = SensorType.getPage(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(new Long(3L), page.getCount());
    Assert.assertEquals(new Integer(1), page.getPageNumber());
    Assert.assertEquals(new Integer(10), page.getPageSize());
    Assert.assertEquals(3, page.getResults().size());
    Assert.assertEquals("Multispectral", page.getResults().get(0).getName());
  }

  @Test
  @Request
  public void testGetPageWithContainsFilter()
  {
    JSONObject filter = new JSONObject();
    filter.put("value", "cm");
    filter.put("matchMode", "contains");

    JSONObject filters = new JSONObject();
    filters.put(SensorType.NAME, filter);

    JSONObject criteria = new JSONObject();
    criteria.put("filters", filters);

    Page<Classification> page = SensorType.getPage(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(new Long(1L), page.getCount());
    Assert.assertEquals(new Integer(1), page.getPageNumber());
    Assert.assertEquals(new Integer(10), page.getPageSize());
    Assert.assertEquals(1, page.getResults().size());
    Assert.assertEquals("CMOS", page.getResults().get(0).getName());
  }

  @Test
  @Request
  public void testGetPageWithEqualsFilter()
  {
    JSONObject filter = new JSONObject();
    filter.put("value", "CMOS");
    filter.put("matchMode", "equals");

    JSONObject filters = new JSONObject();
    filters.put(SensorType.NAME, filter);

    JSONObject criteria = new JSONObject();
    criteria.put("filters", filters);

    Page<Classification> page = SensorType.getPage(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(new Long(1L), page.getCount());
    Assert.assertEquals(new Integer(1), page.getPageNumber());
    Assert.assertEquals(new Integer(10), page.getPageSize());
    Assert.assertEquals(1, page.getResults().size());
    Assert.assertEquals("CMOS", page.getResults().get(0).getName());
  }

  @Test
  @Request
  public void testFromJSON()
  {
    String name = "TEST_SENSOR_TYPE";
    long seq = 10;

    JSONObject json = new JSONObject();
    json.put(SensorType.NAME, name);
    json.put(SensorType.SEQ, seq);

    SensorType sensorType = SensorType.fromJSON(json);

    Assert.assertEquals(name, sensorType.getName());
    Assert.assertEquals(new Long(seq), sensorType.getSeq());
  }

}
