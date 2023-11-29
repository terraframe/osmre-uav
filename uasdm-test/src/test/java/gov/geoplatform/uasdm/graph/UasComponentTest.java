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
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import org.locationtech.jts.geom.Point;
import org.springframework.test.context.ContextConfiguration;

import gov.geoplatform.uasdm.Area51DataTest;
import gov.geoplatform.uasdm.SpringInstanceTestClassRunner;
import gov.geoplatform.uasdm.TestConfig;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.test.Area51DataSet;
import gov.geoplatform.uasdm.view.SiteObject;
import gov.geoplatform.uasdm.view.SiteObjectsResultSet;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class UasComponentTest extends Area51DataTest
{
  private Product              product;

  private Site                 site;

  private Project              project;

  private Mission              mission;

  private Collection           collection;

  private Document             target;

  private Document             source;

  private Document             image;

  @Before
  @Request
  public void setUp()
  {
    testData.setUpInstanceData();

    site = (Site) Area51DataSet.SITE_AREA_51.getServerObject();
    project = (Project) Area51DataSet.PROJECT_DREAMLAND.getServerObject();
    mission = (Mission) Area51DataSet.MISSION_HAVE_DOUGHNUT.getServerObject();
    collection = Area51DataSet.COLLECTION_FISHBED.getServerObject();
    product = collection.getProducts().get(0);
    target = Area51DataSet.ORTHO_DOCUMENT.getServerObject();
    image = Area51DataSet.IMAGE_DOCUMENT.getServerObject();
    source = Area51DataSet.RAW_DOCUMENT.getServerObject();

    testData.logIn();
  }

  @After
  @Request
  public void tearDown()
  {
    testData.logOut();

    if (product != null)
    {
      product.delete();
    }

    testData.tearDownInstanceData();
  }

  @Test
  @Request
  public void testGenerateFolderName()
  {
    String folderName = collection.generateFolderName(mission);

    Assert.assertEquals("fishbed_e", folderName);
  }

  @Test
  @Request
  public void testGetArtifacts()
  {
    JSONObject artifacts = collection.getArtifacts();

    Assert.assertTrue(artifacts.has(ImageryComponent.DEM));
    Assert.assertTrue(artifacts.has(ImageryComponent.ORTHO));
    Assert.assertTrue(artifacts.has(ImageryComponent.PTCLOUD));

    Assert.assertEquals(0, artifacts.getJSONObject(ImageryComponent.DEM).getJSONArray("items").length());
    Assert.assertEquals(0, artifacts.getJSONObject(ImageryComponent.PTCLOUD).getJSONArray("items").length());

    JSONArray result = artifacts.getJSONObject(ImageryComponent.ORTHO).getJSONArray("items");
    Assert.assertEquals(1, result.length());

    JSONObject artifact = result.getJSONObject(0);

    Assert.assertEquals(target.getOid(), artifact.getString("id"));
  }

  @Test
  @Request
  public void testRemoveArtifacts()
  {
    collection.removeArtifacts(ImageryComponent.ORTHO, true);

    Assert.assertNull(Document.get(target.getOid()));
    Assert.assertNull(Document.get(image.getOid()));
    Assert.assertNull(Product.get(product.getOid()));

    product = null;
  }

  @Test
  @Request
  public void testGetSiteObjects_Raw()
  {
    SiteObjectsResultSet results = collection.getSiteObjects(ImageryComponent.RAW, 1L, 20L);

    Assert.assertEquals(ImageryComponent.RAW, results.getFolder());
    Assert.assertEquals(Long.valueOf(1L), results.getPageNumber());
    Assert.assertEquals(Long.valueOf(20L), results.getPageSize());
    Assert.assertEquals(Long.valueOf(1L), results.getTotalObjects());

    List<SiteObject> objects = results.getObjects();

    Assert.assertEquals(1, objects.size());

    SiteObject object = objects.get(0);

    Assert.assertEquals(object.getKey(), source.getS3location());
  }

  @Test
  @Request
  public void testGetSiteObjects_Collection()
  {
    SiteObjectsResultSet results = collection.getSiteObjects(null, 1L, 20L);

    Assert.assertNull(results.getFolder());
    Assert.assertEquals(Long.valueOf(1L), results.getPageNumber());
    Assert.assertEquals(Long.valueOf(20L), results.getPageSize());
    Assert.assertEquals(Long.valueOf(5L), results.getTotalObjects());

    List<SiteObject> objects = results.getObjects();

    Assert.assertEquals(5, objects.size());

    SiteObject object = objects.get(0);

    Assert.assertEquals(object.getKey(), collection.buildRawKey());
  }

  @Test(expected = ProgrammingErrorException.class)
  @Request
  public void testGetSiteObjects_Ortho()
  {
    collection.getSiteObjects(ImageryComponent.ORTHO, 1L, 20L);
  }

  @Test
  @Request
  public void testGetProperties()
  {
    JSONObject properties = collection.getProperties();

    Assert.assertEquals(collection.getName(), properties.getString("name"));
    Assert.assertEquals(collection.getOid(), properties.getString("oid"));
  }

  @Test
  @Request
  public void testWriteFeature() throws IOException
  {
    StringWriter writer = new StringWriter();

    collection.writeFeature(new JSONWriter(writer));

    String result = writer.toString();

    Assert.assertNotNull(result);
  }

  @Test
  @Request
  public void testFeature() throws IOException
  {
    Point geoPoint = site.getGeoPoint();

    Assert.assertNotNull(geoPoint);

    // Create the bounds condition
    JSONObject sw = new JSONObject();
    sw.put("lng", "0");
    sw.put("lat", "0");

    JSONObject ne = new JSONObject();
    ne.put("lng", "170");
    ne.put("lat", "80");

    JSONObject bbox = new JSONObject();
    bbox.put("_sw", sw);
    bbox.put("_ne", ne);

    JSONObject bounds = new JSONObject();
    bounds.put("field", "bounds");
    bounds.put("value", bbox);

    // Create the bureau condition
    JSONObject bureau = new JSONObject();
    bureau.put("field", "bureau");
    bureau.put("value", site.getBureauOid());

    // Create the bureau condition
    JSONObject name = new JSONObject();
    name.put("field", "name");
    name.put("value", site.getName());

    JSONArray array = new JSONArray();
    array.put(bounds);
    array.put(bureau);
    array.put(name);

    JSONObject conditions = new JSONObject();
    conditions.put("array", array);

    JSONObject results = UasComponent.features(conditions.toString());

    Assert.assertNotNull(results);

    JSONArray features = results.getJSONArray("features");

    Assert.assertEquals(1, features.length());

    JSONObject feature = features.getJSONObject(0);

    Assert.assertTrue(feature.has("geometry"));
    Assert.assertEquals("Feature", feature.getString("type"));
    Assert.assertEquals(site.getOid(), feature.getString("id"));

    JSONObject properties = feature.getJSONObject("properties");
    Assert.assertEquals(site.getName(), properties.getString("name"));
    Assert.assertEquals(site.getOid(), properties.getString("oid"));
  }

  @Test
  @Request
  public void testBbox() throws IOException
  {
    JSONArray result = UasComponent.bbox();

    Assert.assertNotNull(result);
  }

  @Test
  @Request
  public void testGetDerivedProducts_Mission() throws IOException
  {
    List<ProductIF> results = mission.getDerivedProducts(null, null);

    Assert.assertEquals(1, results.size());

    ProductIF result = results.get(0);

    Assert.assertEquals(product.getOid(), result.getOid());
  }

  @Test
  @Request
  public void testGetDerivedProducts_Project() throws IOException
  {
    List<ProductIF> results = project.getDerivedProducts(null, null);

    Assert.assertEquals(1, results.size());

    ProductIF result = results.get(0);

    Assert.assertEquals(product.getOid(), result.getOid());
  }

  @Test
  @Request
  public void testGetDerivedProducts_Site() throws IOException
  {
    List<ProductIF> results = site.getDerivedProducts(null, null);

    Assert.assertEquals(1, results.size());

    ProductIF result = results.get(0);

    Assert.assertEquals(product.getOid(), result.getOid());
  }

  @Test
  @Request
  public void testGetChild() throws IOException
  {
    UasComponentIF child = site.getChild(project.getName());

    Assert.assertEquals(project.getOid(), child.getOid());
  }

  @Test
  @Request
  public void testIsDuplicateFolderName_Root() throws IOException
  {
    Assert.assertFalse(site.isDuplicateFolderName(null, site.getFolderName()));
  }

  @Test
  @Request
  public void testIsDuplicateFolderName_Child() throws IOException
  {
    Assert.assertFalse(project.isDuplicateFolderName(site, project.getFolderName()));
  }

  @Test
  @Request
  public void testGetExcludes() throws IOException
  {
    Assert.assertEquals(0, collection.getExcludes().size());

    image.setExclude(true);
    image.apply();

    Assert.assertEquals(1, collection.getExcludes().size());
  }

  @Test
  @Request
  public void testToMetadataMessage() throws IOException
  {
    JSONArray messages = Collection.toMetadataMessage(Arrays.asList(collection));

    Assert.assertEquals(1, messages.length());

    JSONObject message = messages.getJSONObject(0);

    Assert.assertEquals("Metadata missing for collection [Fishbed_E]", message.getString("message"));
    Assert.assertEquals(collection.getOid(), message.getString("collectionId"));
    Assert.assertEquals(collection.getName(), message.getString("collectionName"));
    Assert.assertEquals(3, message.getJSONArray("ancestors").length());
  }

  @Test
  public void testGetMissingMetadata()
  {
    getMissingMetadata(testData.clientRequest.getSessionId());
  }

  @Request(RequestType.SESSION)
  public void getMissingMetadata(String sessionId)
  {
    Assert.assertEquals(0, Collection.getMissingMetadata(1, 10).size());
    Assert.assertEquals(0, Collection.getMissingMetadataCount());
  }
}
