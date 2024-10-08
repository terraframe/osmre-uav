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
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.SpringInstanceTestClassRunner;
import gov.geoplatform.uasdm.TestConfig;
import gov.geoplatform.uasdm.model.Page;
import org.junit.Assert;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class PlatformManufacturerTest
{

  @Test
  @Request
  public void testGetAll()
  {
    JSONArray manufacturers = PlatformManufacturer.getAll();

    Assert.assertEquals(3, manufacturers.length());
  }

  @Test
  @Request
  public void testGetName()
  {
    PlatformManufacturer result = PlatformManufacturer.getByName("Parrot Anafi USA");

    Assert.assertNotNull(result);
    Assert.assertEquals("Parrot Anafi USA", result.getName());
  }

  @Test
  @Request
  public void testGetBadName()
  {
    PlatformManufacturer result = PlatformManufacturer.getByName("Parrot Anafi USAz");

    Assert.assertNull(result);
  }

  @Test
  @Request
  public void testGetCount()
  {
    Assert.assertEquals(Long.valueOf(3L), PlatformManufacturer.getCount());
  }

  @Test
  @Request
  public void testDefaultGetPage()
  {
    Page<Classification> page = PlatformManufacturer.getPage(new JSONObject());

    Assert.assertNotNull(page);
    Assert.assertEquals(Long.valueOf(3L), page.getCount());
    Assert.assertEquals(Integer.valueOf(1), page.getPageNumber());
    Assert.assertEquals(Integer.valueOf(10), page.getPageSize());
    Assert.assertEquals(3, page.getResults().size());
  }

  @Test
  @Request
  public void testGetPageWithSizeLimit()
  {
    JSONObject criteria = new JSONObject();
    criteria.put("rows", 2);
    criteria.put("first", 2);

    Page<Classification> page = PlatformManufacturer.getPage(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(Long.valueOf(3L), page.getCount());
    Assert.assertEquals(Integer.valueOf(2), page.getPageNumber());
    Assert.assertEquals(Integer.valueOf(2), page.getPageSize());
    Assert.assertEquals(1, page.getResults().size());
  }

  @Test
  @Request
  public void testGetPageWithSort()
  {
    JSONObject criteria = new JSONObject();
    criteria.put("sortField", PlatformManufacturer.NAME);
    criteria.put("sortOrder", 0);

    Page<Classification> page = PlatformManufacturer.getPage(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(Long.valueOf(3L), page.getCount());
    Assert.assertEquals(Integer.valueOf(1), page.getPageNumber());
    Assert.assertEquals(Integer.valueOf(10), page.getPageSize());
    Assert.assertEquals(3, page.getResults().size());
    Assert.assertEquals("Trimble", page.getResults().get(0).getName());
  }

  @Test
  @Request
  public void testGetPageWithMultiSort()
  {
    JSONObject sort1 = new JSONObject();
    sort1.put("field", PlatformManufacturer.NAME);
    sort1.put("order", 0);

    JSONObject sort2 = new JSONObject();
    sort2.put("field", PlatformManufacturer.SEQ);
    sort2.put("order", 0);

    JSONArray multiSortMeta = new JSONArray();
    multiSortMeta.put(sort1);
    multiSortMeta.put(sort2);

    JSONObject criteria = new JSONObject();
    criteria.put("multiSortMeta", multiSortMeta);

    Page<Classification> page = PlatformManufacturer.getPage(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(Long.valueOf(3L), page.getCount());
    Assert.assertEquals(Integer.valueOf(1), page.getPageNumber());
    Assert.assertEquals(Integer.valueOf(10), page.getPageSize());
    Assert.assertEquals(3, page.getResults().size());
    Assert.assertEquals("Trimble", page.getResults().get(0).getName());
  }

  @Test
  @Request
  public void testGetPageWithContainsFilter()
  {
    JSONObject filter = new JSONObject();
    filter.put("value", "Par");
    filter.put("matchMode", "contains");

    JSONObject filters = new JSONObject();
    filters.put(PlatformManufacturer.NAME, filter);

    JSONObject criteria = new JSONObject();
    criteria.put("filters", filters);

    Page<Classification> page = PlatformManufacturer.getPage(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(Long.valueOf(1L), page.getCount());
    Assert.assertEquals(Integer.valueOf(1), page.getPageNumber());
    Assert.assertEquals(Integer.valueOf(10), page.getPageSize());
    Assert.assertEquals(1, page.getResults().size());
    Assert.assertEquals("Parrot Anafi USA", page.getResults().get(0).getName());
  }

  @Test
  @Request
  public void testGetPageWithEqualsFilter()
  {
    JSONObject filter = new JSONObject();
    filter.put("value", "Parrot Anafi USA");
    filter.put("matchMode", "equals");

    JSONObject filters = new JSONObject();
    filters.put(PlatformManufacturer.NAME, filter);

    JSONObject criteria = new JSONObject();
    criteria.put("filters", filters);

    Page<Classification> page = PlatformManufacturer.getPage(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(Long.valueOf(1L), page.getCount());
    Assert.assertEquals(Integer.valueOf(1), page.getPageNumber());
    Assert.assertEquals(Integer.valueOf(10), page.getPageSize());
    Assert.assertEquals(1, page.getResults().size());
    Assert.assertEquals("Parrot Anafi USA", page.getResults().get(0).getName());
  }

  @Test
  @Request
  public void testFromJSON()
  {
    String name = "TEST_PLATFORM_MANUFACTURER";
    long seq = 10;

    JSONObject json = new JSONObject();
    json.put(PlatformManufacturer.NAME, name);
    json.put(PlatformManufacturer.SEQ, seq);

    PlatformManufacturer manufacturer = PlatformManufacturer.fromJSON(json);

    Assert.assertEquals(name, manufacturer.getName());
    Assert.assertEquals(Long.valueOf(seq), manufacturer.getSeq());
  }

}
