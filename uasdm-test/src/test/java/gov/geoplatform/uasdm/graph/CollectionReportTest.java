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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.bus.CollectionReport;
import gov.geoplatform.uasdm.model.Page;
import gov.geoplatform.uasdm.test.Area51DataSet;
import junit.framework.Assert;

public class CollectionReportTest
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
  public void testDefaultGetPage()
  {
    Page<CollectionReport> page = CollectionReport.page(new JSONObject());

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

    Page<CollectionReport> page = CollectionReport.page(criteria);

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
    criteria.put("sortField", CollectionReport.COLLECTIONNAME);
    criteria.put("sortOrder", 0);

    Page<CollectionReport> page = CollectionReport.page(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(Long.valueOf(1L), page.getCount());
    Assert.assertEquals(Integer.valueOf(1), page.getPageNumber());
    Assert.assertEquals(Integer.valueOf(10), page.getPageSize());
    Assert.assertEquals(1, page.getResults().size());
    Assert.assertEquals(Area51DataSet.COLLECTION_FISHBED.getName(), page.getResults().get(0).getCollectionName());
  }

  @Test
  @Request
  public void testGetPageWithMultiSort()
  {
    JSONObject sort1 = new JSONObject();
    sort1.put("field", CollectionReport.COLLECTIONNAME);
    sort1.put("order", 0);

    JSONObject sort2 = new JSONObject();
    sort2.put("field", CollectionReport.SEQ);
    sort2.put("order", 0);

    JSONArray multiSortMeta = new JSONArray();
    multiSortMeta.put(sort1);
    multiSortMeta.put(sort2);

    JSONObject criteria = new JSONObject();
    criteria.put("multiSortMeta", multiSortMeta);

    Page<CollectionReport> page = CollectionReport.page(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(Long.valueOf(1L), page.getCount());
    Assert.assertEquals(Integer.valueOf(1), page.getPageNumber());
    Assert.assertEquals(Integer.valueOf(10), page.getPageSize());
    Assert.assertEquals(1, page.getResults().size());
    Assert.assertEquals(Area51DataSet.COLLECTION_FISHBED.getName(), page.getResults().get(0).getCollectionName());
  }

  @Test
  @Request
  public void testGetPageWithContainsFilter()
  {
    JSONObject filter = new JSONObject();
    filter.put("value", Area51DataSet.COLLECTION_FISHBED.getName());
    filter.put("matchMode", "contains");

    JSONObject filters = new JSONObject();
    filters.put(CollectionReport.COLLECTIONNAME, filter);

    JSONObject criteria = new JSONObject();
    criteria.put("filters", filters);

    Page<CollectionReport> page = CollectionReport.page(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(Long.valueOf(1L), page.getCount());
    Assert.assertEquals(Integer.valueOf(1), page.getPageNumber());
    Assert.assertEquals(Integer.valueOf(10), page.getPageSize());
    Assert.assertEquals(1, page.getResults().size());
    Assert.assertEquals(Area51DataSet.COLLECTION_FISHBED.getName(), page.getResults().get(0).getCollectionName());
  }

  @Test
  @Request
  public void testGetPageWithEqualsFilter()
  {
    JSONObject filter = new JSONObject();
    filter.put("value", Area51DataSet.COLLECTION_FISHBED.getName());
    filter.put("matchMode", "equals");

    JSONObject filters = new JSONObject();
    filters.put(CollectionReport.COLLECTIONNAME, filter);

    JSONObject criteria = new JSONObject();
    criteria.put("filters", filters);

    Page<CollectionReport> page = CollectionReport.page(criteria);

    Assert.assertNotNull(page);
    Assert.assertEquals(Long.valueOf(1L), page.getCount());
    Assert.assertEquals(Integer.valueOf(1), page.getPageNumber());
    Assert.assertEquals(Integer.valueOf(10), page.getPageSize());
    Assert.assertEquals(1, page.getResults().size());
    Assert.assertEquals(Area51DataSet.COLLECTION_FISHBED.getName(), page.getResults().get(0).getCollectionName());
  }

  @Test
  @Request
  public void testExportCSVWithSort() throws IOException, CsvException
  {
    CollectionReport report = this.get();

    JSONObject criteria = new JSONObject();
    criteria.put("sortField", CollectionReport.COLLECTIONNAME);
    criteria.put("sortOrder", 0);

    try (InputStream istream = CollectionReport.exportCSV(criteria))
    {
      Assert.assertNotNull(istream);

      CSVReader reader = new CSVReader(new InputStreamReader(istream));

      List<String[]> lines = reader.readAll();

      Assert.assertEquals(2, lines.size());

      String[] line = lines.get(1);
      
      int col = 0;
      
      Assert.assertEquals(report.getCollectionName(), line[col++]);
      Assert.assertEquals(report.getUserName(), line[col++]);
      Assert.assertNotNull(line[col++]);
      Assert.assertEquals(report.getMissionName(), line[col++]);
      Assert.assertEquals(report.getProjectName(), line[col++]);
      Assert.assertEquals(report.getSiteName(), line[col++]);
      Assert.assertNotNull(line[col++]);
      Assert.assertNotNull(line[col++]);
      Assert.assertEquals(report.getBureauName(), line[col++]);
      Assert.assertEquals(report.getPlatformName(), line[col++]);
      Assert.assertEquals(report.getSensorName(), line[col++]);
      Assert.assertEquals(report.getFaaIdNumber(), line[col++]);
      Assert.assertEquals(report.getSerialNumber(), line[col++]);
      Assert.assertEquals(report.getOdmProcessing(), line[col++]);
      Assert.assertEquals(report.getRawImagesCount().toString(), line[col++]);
      Assert.assertEquals(report.getErosMetadataComplete().toString(), line[col++]);
      Assert.assertEquals(report.getVideo().toString(), line[col++]);
      Assert.assertEquals(report.getOrthomosaic().toString(), line[col++]);
      Assert.assertEquals(report.getPointCloud().toString(), line[col++]);
      Assert.assertEquals(report.getHillshade().toString(), line[col++]);
      Assert.assertEquals(report.getProductsShared().toString(), line[col++]);
      Assert.assertEquals(report.getAllStorageSize().toString(), line[col++]);
      Assert.assertEquals(report.getDownloadCounts().toString(), line[col++]);      
      Assert.assertNotNull(line[col++]);
      Assert.assertNotNull(line[col++]);
    }

  }

  @Test
  @Request
  public void testExportCSVWithMultiSort() throws IOException, CsvException
  {
    JSONObject sort1 = new JSONObject();
    sort1.put("field", CollectionReport.COLLECTIONNAME);
    sort1.put("order", 0);

    JSONObject sort2 = new JSONObject();
    sort2.put("field", CollectionReport.SEQ);
    sort2.put("order", 0);

    JSONArray multiSortMeta = new JSONArray();
    multiSortMeta.put(sort1);
    multiSortMeta.put(sort2);

    JSONObject criteria = new JSONObject();
    criteria.put("multiSortMeta", multiSortMeta);

    try (InputStream istream = CollectionReport.exportCSV(criteria))
    {
      Assert.assertNotNull(istream);

      CSVReader reader = new CSVReader(new InputStreamReader(istream));

      List<String[]> lines = reader.readAll();

      Assert.assertEquals(2, lines.size());
    }
  }

  @Test
  @Request
  public void testExportCSVWithContainsFilter() throws IOException, CsvException
  {
    JSONObject filter = new JSONObject();
    filter.put("value", Area51DataSet.COLLECTION_FISHBED.getName());
    filter.put("matchMode", "contains");

    JSONObject filters = new JSONObject();
    filters.put(CollectionReport.COLLECTIONNAME, filter);

    JSONObject criteria = new JSONObject();
    criteria.put("filters", filters);

    try (InputStream istream = CollectionReport.exportCSV(criteria))
    {
      Assert.assertNotNull(istream);

      CSVReader reader = new CSVReader(new InputStreamReader(istream));

      List<String[]> lines = reader.readAll();

      Assert.assertEquals(2, lines.size());
    }
  }

  @Test
  @Request
  public void testExportCSVWithEqualsFilter() throws IOException, CsvException
  {
    JSONObject filter = new JSONObject();
    filter.put("value", Area51DataSet.COLLECTION_FISHBED.getName());
    filter.put("matchMode", "equals");

    JSONObject filters = new JSONObject();
    filters.put(CollectionReport.COLLECTIONNAME, filter);

    JSONObject criteria = new JSONObject();
    criteria.put("filters", filters);

    try (InputStream istream = CollectionReport.exportCSV(criteria))
    {
      Assert.assertNotNull(istream);

      CSVReader reader = new CSVReader(new InputStreamReader(istream));

      List<String[]> lines = reader.readAll();

      Assert.assertEquals(2, lines.size());
    }
  }

  @Test
  @Request
  public void testToJSON()
  {
    CollectionReport report = this.get();
    JSONObject object = report.toJSON();

    Assert.assertNotNull(object);
    Assert.assertEquals(report.getUserName(), object.getString(CollectionReport.USERNAME));
    Assert.assertEquals(report.getBureauName(), object.getString(CollectionReport.BUREAUNAME));
    Assert.assertEquals(report.getSiteName(), object.getString(CollectionReport.SITENAME));
    Assert.assertEquals(report.getProjectName(), object.getString(CollectionReport.PROJECTNAME));
    Assert.assertEquals(report.getMissionName(), object.getString(CollectionReport.MISSIONNAME));
    Assert.assertEquals(report.getValue(CollectionReport.COLLECTION), object.getString(CollectionReport.COLLECTION));
    Assert.assertEquals(report.getCollectionName(), object.getString(CollectionReport.COLLECTIONNAME));
    Assert.assertNotNull(object.getString(CollectionReport.COLLECTIONDATE));
    Assert.assertEquals(report.getSerialNumber(), object.getString(CollectionReport.SERIALNUMBER));
    Assert.assertEquals(report.getPlatformName(), object.getString(CollectionReport.PLATFORMNAME));
    Assert.assertEquals(report.getSensorName(), object.getString(CollectionReport.SENSORNAME));
    Assert.assertEquals(report.getFaaIdNumber(), object.getString(CollectionReport.FAAIDNUMBER));
    Assert.assertEquals(report.getSerialNumber(), object.getString(CollectionReport.SERIALNUMBER));
    Assert.assertEquals(report.getOdmProcessing(), object.getString(CollectionReport.ODMPROCESSING));
    Assert.assertEquals(report.getErosMetadataComplete(), Boolean.valueOf(object.getBoolean(CollectionReport.EROSMETADATACOMPLETE)));
    Assert.assertEquals(report.getRawImagesCount(), Integer.valueOf(object.getInt(CollectionReport.RAWIMAGESCOUNT)));
    Assert.assertEquals(report.getVideo(), Boolean.valueOf(object.getBoolean(CollectionReport.VIDEO)));
    Assert.assertEquals(report.getOrthomosaic(), Boolean.valueOf(object.getBoolean(CollectionReport.ORTHOMOSAIC)));
    Assert.assertEquals(report.getPointCloud(), Boolean.valueOf(object.getBoolean(CollectionReport.POINTCLOUD)));
    Assert.assertEquals(report.getHillshade(), Boolean.valueOf(object.getBoolean(CollectionReport.HILLSHADE)));
    Assert.assertEquals(report.getProductsShared(), Boolean.valueOf(object.getBoolean(CollectionReport.PRODUCTSSHARED)));
    Assert.assertEquals(report.getValue(CollectionReport.PRODUCT), object.getString(CollectionReport.PRODUCT));
    Assert.assertEquals(report.getExists(), Boolean.valueOf(object.getBoolean(CollectionReport.EXISTS)));
    Assert.assertEquals(report.getDownloadCounts(), Long.valueOf(object.getLong(CollectionReport.DOWNLOADCOUNTS)));
    Assert.assertFalse(object.has(CollectionReport.DELETEDATE));
    Assert.assertNotNull(object.getString(CollectionReport.CREATEDATE));
  }

  private CollectionReport get()
  {
    return CollectionReport.getForCollection(Area51DataSet.COLLECTION_FISHBED.getServerObject()).get(0);
  }
}
