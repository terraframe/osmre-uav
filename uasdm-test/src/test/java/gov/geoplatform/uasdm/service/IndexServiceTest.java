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
package gov.geoplatform.uasdm.service;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.graph.Document;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.mock.MockIndex;
import gov.geoplatform.uasdm.mock.MockIndex.IndexAction;
import gov.geoplatform.uasdm.mock.MockIndex.IndexActionType;
import gov.geoplatform.uasdm.test.Area51DataSet;
import junit.framework.Assert;

public class IndexServiceTest
{
  private static Area51DataSet testData;

  private Product product;

  private Collection collection;

  private Document target;

  private MockIndex index;

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
  @Request
  public void setUp()
  {
    testData.setUpInstanceData();

    collection = Area51DataSet.COLLECTION_FISHBED.getServerObject();
    product = collection.getProducts().get(0);
    target = Area51DataSet.ORTHO_DOCUMENT.getServerObject();

    index = (MockIndex) IndexService.getIndex();
  }

  @After
  @Request
  public void tearDown()
  {
    if (product != null)
    {
      product.delete();
    }

    testData.tearDownInstanceData();
  }

  @Test
  @Request
  public void testGetContentXML() throws Exception
  {
    File file = new File(this.getClass().getResource("/metadata.xml").toURI());
    String content = IndexService.getContent(file);
    String expected = " \n" + "   \n" + "   \n" + "   \n" + "   \n" + "   \n" + "   \n" + "   \n" + "   \n" + "\n";

    Assert.assertEquals(expected, content);
  }

  @Test
  @Request
  public void testGetContentJSON() throws Exception
  {
    File file = new File(this.getClass().getResource("/stac_item.json").toURI());
    String content = IndexService.getContent(file);
    String expected = FileUtils.readFileToString(file, "UTF-8") + "\n";

    Assert.assertEquals(expected, content);
  }

  @Test
  @Request
  public void testGetContentPDF() throws Exception
  {
    File file = new File(this.getClass().getResource("/sample.pdf").toURI());
    String content = IndexService.getContent(file);
    String expected = "\n" + "TEST 1 2\n" + "\n" + "\n" + "";

    Assert.assertEquals(expected, content);
  }

  @Test
  @Request
  public void testGetContentDOC() throws Exception
  {
    File file = new File(this.getClass().getResource("/sample.docx").toURI());
    String content = IndexService.getContent(file);
    String expected = "TEST 1 2\n" + "";

    Assert.assertEquals(expected, content);
  }

  @Test
  @Request
  public void testGetContentXLSX() throws Exception
  {
    File file = new File(this.getClass().getResource("/sample.xlsx").toURI());
    String content = IndexService.getContent(file);

    Assert.assertTrue(content.contains("TEST"));
  }

  @Test
  @Request
  public void testDeleteDocuments()
  {
    IndexService.deleteDocuments(collection.getSolrIdField(), collection.getOid());

    List<IndexAction> actions = index.getActions();

    Assert.assertEquals(1, actions.size());

    IndexAction action = actions.get(0);

    Assert.assertEquals(IndexActionType.DELETE_DOCUMENTS, action.getType());

    Object[] objects = action.getObjects();

    Assert.assertEquals(2, objects.length);
    Assert.assertEquals(collection.getSolrIdField(), objects[0]);
    Assert.assertEquals(collection.getOid(), objects[1]);
  }

  @Test
  @Request
  public void testDeleteDocument()
  {
    IndexService.deleteDocument(collection, target.getS3location());

    List<IndexAction> actions = index.getActions();

    Assert.assertEquals(1, actions.size());

    IndexAction action = actions.get(0);

    Assert.assertEquals(IndexActionType.DELETE_DOCUMENT, action.getType());

    Object[] objects = action.getObjects();

    Assert.assertEquals(2, objects.length);
    Assert.assertEquals(collection.getOid(), objects[0]);
    Assert.assertEquals(target.getS3location(), objects[1]);
  }

  @Test
  @Request
  public void testUpdateOrCreateDocument()
  {
    IndexService.updateOrCreateDocument(Arrays.asList(collection), collection, collection.getS3location(), collection.getName());

    List<IndexAction> actions = index.getActions();

    Assert.assertEquals(1, actions.size());

    IndexAction action = actions.get(0);

    Assert.assertEquals(IndexActionType.UPDATE_DOCUMENT, action.getType());

    Object[] objects = action.getObjects();

    Assert.assertEquals(4, objects.length);
  }

}
