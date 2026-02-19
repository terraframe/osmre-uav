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

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.SpringInstanceTestClassRunner;
import gov.geoplatform.uasdm.TestConfig;
import gov.geoplatform.uasdm.model.StacItem;
import gov.geoplatform.uasdm.model.StacItem.Asset;
import gov.geoplatform.uasdm.model.StacItem.Properties;
import gov.geoplatform.uasdm.model.StacLink;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class SiteItemTest
{
  // private static Area51DataSet testData;
  //
  // private Product product;
  //
  // private Collection collection;
  //
  // private Document target;
  //
  // private Document source;
  //
  // private Document image;
  //
  // @BeforeClass
  // public static void setUpClass()
  // {
  // testData = new Area51DataSet();
  // testData.setUpSuiteData();
  // }
  //
  // @AfterClass
  // public static void cleanUpClass()
  // {
  // if (testData != null)
  // {
  // testData.tearDownMetadata();
  // }
  // }
  //
  // @Before
  // @Request
  // public void setUp()
  // {
  // testData.setUpInstanceData();
  //
  // collection = Area51DataSet.COLLECTION_FISHBED.getServerObject();
  // product = Product.createIfNotExist(collection);
  // target = Document.createIfNotExist(collection, collection.getS3location() +
  // ImageryComponent.ORTHO + "/test.cog.tif", "test.cog.tif", "", "");
  // image = Document.createIfNotExist(collection, collection.getS3location() +
  // ImageryComponent.ORTHO + "/test.png", "test.png", "", "");
  // source = Document.createIfNotExist(collection, collection.getS3location() +
  // ImageryComponent.RAW + "/test.jpg", "test.jpg", "", "");
  //
  // product.addDocumentGeneratedProductParent(source).apply();
  // product.addDocuments(Arrays.asList(target, image));
  // }
  //
  // @After
  // @Request
  // public void tearDown()
  // {
  // if (product != null)
  // {
  // product.delete();
  // }
  //
  // testData.tearDownInstanceData();
  // }

  @Test
  @Request
  public void testDeserialization() throws Exception
  {
    File file = new File(this.getClass().getResource("/stac_item.json").toURI());

    ObjectMapper mapper = new ObjectMapper();
    mapper.enable(SerializationFeature.INDENT_OUTPUT);

    StacItem item = mapper.readValue(file, StacItem.class);

    Assert.assertNotNull(item);

    Assert.assertEquals("c65434ee-5f2f-44cf-97c6-10d4dca08cf5", item.getId());
    Assert.assertNotNull(item.getBbox());
    Assert.assertNotNull(item.getGeometry());

    Properties properties = item.getProperties();
    Assert.assertNotNull(properties.getDatetime());
    Assert.assertEquals("collection 2", properties.getTitle());
    Assert.assertEquals("collection 2", properties.getCollection());
//    Assert.assertNotNull(properties.getUpdated());
    Assert.assertEquals("Sensor 1", properties.getSensor());
    Assert.assertEquals("Platform 1", properties.getPlatform());
    Assert.assertEquals("RGB", properties.getFaaNumber());
    Assert.assertEquals("RGB", properties.getSerialNumber());
    Assert.assertEquals("Marble", properties.getSite());
    Assert.assertEquals("Testing", properties.getProject());
    Assert.assertEquals("Test Miission", properties.getMission());

    Map<String, Asset> assets = item.getAssets();

    Assert.assertEquals(8, assets.size());
    
    Asset asset = assets.get("thumbnail");
        
    Assert.assertEquals("s3://osmre-uas-dev-deploy/marble/testing/testmiission/collection2/ortho/thumbnails/odm_orthophoto.png", asset.getHref());
    Assert.assertEquals("image/png", asset.getType());
    Assert.assertEquals("Thumbnail", asset.getTitle());
    Assert.assertEquals(1, asset.getRoles().size());

    List<StacLink> links = item.getLinks();

    Assert.assertEquals(1, links.size());
    
    StacLink link = links.get(0);
    Assert.assertEquals("s3://osmre-uas-dev-deploy/_stac_/c65434ee-5f2f-44cf-97c6-10d4dca08cf5.json", link.getHref());
    Assert.assertEquals("application/json", link.getType());
    Assert.assertEquals("self", link.getRel());
  }

  @Test
  @Request
  public void testSerialization() throws Exception
  {
    File file = new File(this.getClass().getResource("/stac_item.json").toURI());

    ObjectMapper mapper = new ObjectMapper();
    mapper.enable(SerializationFeature.INDENT_OUTPUT);

    StacItem item = mapper.readValue(file, StacItem.class);

    String target = mapper.writeValueAsString(item);

    JsonNode sourceNode = mapper.readTree(file);
    JsonNode targetNode = mapper.readTree(target);

    // Checking if both json objects are same
    Assert.assertTrue(sourceNode.equals(targetNode));
  }
}
