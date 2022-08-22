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

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.model.Page;
import junit.framework.Assert;

public class PlatformTypeTest
{

  @Test
  @Request
  public void testGetAll()
  {
    JSONArray platformTypes = PlatformType.getAll();

    Assert.assertEquals(6, platformTypes.length());
  }

  @Test
  @Request
  public void testGetName()
  {
    PlatformType result = PlatformType.getByName("Helicopter");

    Assert.assertNotNull(result);
    Assert.assertEquals("Helicopter", result.getName());
  }

  @Test
  @Request
  public void testGetBadName()
  {
    PlatformType result = PlatformType.getByName("Helicopterz");

    Assert.assertNull(result);
  }

  @Test
  @Request
  public void testGetCount()
  {
    Assert.assertEquals(new Long(6L), PlatformType.getCount());
  }

  @Test
  @Request
  public void testDefaultGetPage()
  {
    Page<Classification> page = PlatformType.getPage(new JSONObject());

    Assert.assertNotNull(page);
    Assert.assertEquals(new Long(6L), page.getCount());
    Assert.assertEquals(new Integer(1), page.getPageNumber());
    Assert.assertEquals(new Integer(10), page.getPageSize());
    Assert.assertEquals(6, page.getResults().size());
  }

  @Test
  @Request
  public void testGetPageWithSizeLimit()
  {
    JSONObject criteria = new JSONObject();
    criteria.put("rows", 2);
    criteria.put("first", 2);

    Page<Classification> page = PlatformType.getPage(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(new Long(6L), page.getCount());
    Assert.assertEquals(new Integer(2), page.getPageNumber());
    Assert.assertEquals(new Integer(2), page.getPageSize());
    Assert.assertEquals(2, page.getResults().size());
  }

  @Test
  @Request
  public void testGetPageWithSort()
  {
    JSONObject criteria = new JSONObject();
    criteria.put("sortField", PlatformType.NAME);
    criteria.put("sortOrder", 0);

    Page<Classification> page = PlatformType.getPage(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(new Long(6L), page.getCount());
    Assert.assertEquals(new Integer(1), page.getPageNumber());
    Assert.assertEquals(new Integer(10), page.getPageSize());
    Assert.assertEquals(6, page.getResults().size());
    Assert.assertEquals("VTOL Fixed-wing", page.getResults().get(0).getName());
  }

  @Test
  @Request
  public void testGetPageWithMultiSort()
  {
    JSONObject sort1 = new JSONObject();
    sort1.put("field", PlatformType.NAME);
    sort1.put("order", 0);

    JSONObject sort2 = new JSONObject();
    sort2.put("field", PlatformType.SEQ);
    sort2.put("order", 0);

    JSONArray multiSortMeta = new JSONArray();
    multiSortMeta.put(sort1);
    multiSortMeta.put(sort2);

    JSONObject criteria = new JSONObject();
    criteria.put("multiSortMeta", multiSortMeta);

    Page<Classification> page = PlatformType.getPage(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(new Long(6L), page.getCount());
    Assert.assertEquals(new Integer(1), page.getPageNumber());
    Assert.assertEquals(new Integer(10), page.getPageSize());
    Assert.assertEquals(6, page.getResults().size());
    Assert.assertEquals("VTOL Fixed-wing", page.getResults().get(0).getName());
  }

  @Test
  @Request
  public void testGetPageWithContainsFilter()
  {
    JSONObject filter = new JSONObject();
    filter.put("value", "Li");
    filter.put("matchMode", "contains");

    JSONObject filters = new JSONObject();
    filters.put(PlatformType.NAME, filter);

    JSONObject criteria = new JSONObject();
    criteria.put("filters", filters);

    Page<Classification> page = PlatformType.getPage(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(new Long(1L), page.getCount());
    Assert.assertEquals(new Integer(1), page.getPageNumber());
    Assert.assertEquals(new Integer(10), page.getPageSize());
    Assert.assertEquals(1, page.getResults().size());
    Assert.assertEquals("Helicopter", page.getResults().get(0).getName());
  }

  @Test
  @Request
  public void testGetPageWithEqualsFilter()
  {
    JSONObject filter = new JSONObject();
    filter.put("value", "Helicopter");
    filter.put("matchMode", "equals");

    JSONObject filters = new JSONObject();
    filters.put(PlatformType.NAME, filter);

    JSONObject criteria = new JSONObject();
    criteria.put("filters", filters);

    Page<Classification> page = PlatformType.getPage(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(new Long(1L), page.getCount());
    Assert.assertEquals(new Integer(1), page.getPageNumber());
    Assert.assertEquals(new Integer(10), page.getPageSize());
    Assert.assertEquals(1, page.getResults().size());
    Assert.assertEquals("Helicopter", page.getResults().get(0).getName());
  }

  @Test
  @Request
  public void testFromJSON()
  {
    String name = "TEST_PLATFORM_TYPE";
    long seq = 10;

    JSONObject json = new JSONObject();
    json.put(PlatformType.NAME, name);
    json.put(PlatformType.SEQ, seq);

    PlatformType platformType = PlatformType.fromJSON(json);

    Assert.assertEquals(name, platformType.getName());
    Assert.assertEquals(new Long(seq), platformType.getSeq());
  }

}
