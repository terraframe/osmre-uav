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
public class WaveLengthTest
{

  @Test
  @Request
  public void testGetAll()
  {
    JSONArray waveLengths = WaveLength.getAll();

    Assert.assertEquals(5, waveLengths.length());
  }

  @Test
  @Request
  public void testGetName()
  {
    WaveLength result = WaveLength.getByName("LiDAR");

    Assert.assertNotNull(result);
    Assert.assertEquals("LiDAR", result.getName());
  }

  @Test
  @Request
  public void testGetBadName()
  {
    WaveLength result = WaveLength.getByName("LiDARz");

    Assert.assertNull(result);
  }

  @Test
  @Request
  public void testGetCount()
  {
    Assert.assertEquals(Long.valueOf(5L), WaveLength.getCount());
  }

  @Test
  @Request
  public void testDefaultGetPage()
  {
    Page<Classification> page = WaveLength.getPage(new JSONObject());

    Assert.assertNotNull(page);
    Assert.assertEquals(Long.valueOf(5L), page.getCount());
    Assert.assertEquals(Integer.valueOf(1), page.getPageNumber());
    Assert.assertEquals(Integer.valueOf(10), page.getPageSize());
    Assert.assertEquals(5, page.getResults().size());
  }

  @Test
  @Request
  public void testGetPageWithSizeLimit()
  {
    JSONObject criteria = new JSONObject();
    criteria.put("rows", 2);
    criteria.put("first", 2);

    Page<Classification> page = WaveLength.getPage(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(Long.valueOf(5L), page.getCount());
    Assert.assertEquals(Integer.valueOf(2), page.getPageNumber());
    Assert.assertEquals(Integer.valueOf(2), page.getPageSize());
    Assert.assertEquals(2, page.getResults().size());
  }

  @Test
  @Request
  public void testGetPageWithSort()
  {
    JSONObject criteria = new JSONObject();
    criteria.put("sortField", WaveLength.NAME);
    criteria.put("sortOrder", 0);

    Page<Classification> page = WaveLength.getPage(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(Long.valueOf(5L), page.getCount());
    Assert.assertEquals(Integer.valueOf(1), page.getPageNumber());
    Assert.assertEquals(Integer.valueOf(10), page.getPageSize());
    Assert.assertEquals(5, page.getResults().size());
    Assert.assertEquals("Thermal", page.getResults().get(0).getName());
  }

  @Test
  @Request
  public void testGetPageWithMultiSort()
  {
    JSONObject sort1 = new JSONObject();
    sort1.put("field", WaveLength.NAME);
    sort1.put("order", 0);

    JSONObject sort2 = new JSONObject();
    sort2.put("field", WaveLength.SEQ);
    sort2.put("order", 0);

    JSONArray multiSortMeta = new JSONArray();
    multiSortMeta.put(sort1);
    multiSortMeta.put(sort2);

    JSONObject criteria = new JSONObject();
    criteria.put("multiSortMeta", multiSortMeta);

    Page<Classification> page = WaveLength.getPage(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(Long.valueOf(5L), page.getCount());
    Assert.assertEquals(Integer.valueOf(1), page.getPageNumber());
    Assert.assertEquals(Integer.valueOf(10), page.getPageSize());
    Assert.assertEquals(5, page.getResults().size());
    Assert.assertEquals("Thermal", page.getResults().get(0).getName());
  }

  @Test
  @Request
  public void testGetPageWithContainsFilter()
  {
    JSONObject filter = new JSONObject();
    filter.put("value", "Li");
    filter.put("matchMode", "contains");

    JSONObject filters = new JSONObject();
    filters.put(WaveLength.NAME, filter);

    JSONObject criteria = new JSONObject();
    criteria.put("filters", filters);

    Page<Classification> page = WaveLength.getPage(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(Long.valueOf(1L), page.getCount());
    Assert.assertEquals(Integer.valueOf(1), page.getPageNumber());
    Assert.assertEquals(Integer.valueOf(10), page.getPageSize());
    Assert.assertEquals(1, page.getResults().size());
    Assert.assertEquals("LiDAR", page.getResults().get(0).getName());
  }

  @Test
  @Request
  public void testGetPageWithEqualsFilter()
  {
    JSONObject filter = new JSONObject();
    filter.put("value", "LiDAR");
    filter.put("matchMode", "equals");

    JSONObject filters = new JSONObject();
    filters.put(WaveLength.NAME, filter);

    JSONObject criteria = new JSONObject();
    criteria.put("filters", filters);

    Page<Classification> page = WaveLength.getPage(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(Long.valueOf(1L), page.getCount());
    Assert.assertEquals(Integer.valueOf(1), page.getPageNumber());
    Assert.assertEquals(Integer.valueOf(10), page.getPageSize());
    Assert.assertEquals(1, page.getResults().size());
    Assert.assertEquals("LiDAR", page.getResults().get(0).getName());
  }

  @Test
  @Request
  public void testFromJSON()
  {
    String name = "TEST_WAVELENGTH";
    long seq = 10;

    JSONObject json = new JSONObject();
    json.put(WaveLength.NAME, name);
    json.put(WaveLength.SEQ, seq);

    WaveLength waveLength = WaveLength.fromJSON(json);

    Assert.assertEquals(name, waveLength.getName());
    Assert.assertEquals(Long.valueOf(seq), waveLength.getSeq());
  }

}
