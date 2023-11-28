/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.geoplatform.uasdm.graph;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.geom.Envelope;
import org.springframework.test.context.ContextConfiguration;

import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.Area51DataTest;
import gov.geoplatform.uasdm.SpringInstanceTestClassRunner;
import gov.geoplatform.uasdm.TestConfig;
import gov.geoplatform.uasdm.bus.CollectionReport;
import gov.geoplatform.uasdm.cog.TiTillerProxy.BBoxView;
import gov.geoplatform.uasdm.mock.MockRemoteFileService;
import gov.geoplatform.uasdm.mock.MockRemoteFileService.RemoteFileAction;
import gov.geoplatform.uasdm.mock.MockRemoteFileService.RemoteFileActionType;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.Page;
import gov.geoplatform.uasdm.model.StacItem;
import gov.geoplatform.uasdm.model.StacItem.Asset;
import gov.geoplatform.uasdm.model.StacItem.Properties;
import gov.geoplatform.uasdm.model.StacLink;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.remote.RemoteFileObject;
import gov.geoplatform.uasdm.test.Area51DataSet;
import gov.geoplatform.uasdm.view.SiteObject;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class ProductTest extends Area51DataTest
{
  private Collection collection;

  private Product    product;

  private Document   target;

  private Document   source;

  private Document   image;

  @Before
  @Request
  public void setUp()
  {
    testData.setUpInstanceData();

    collection = Area51DataSet.COLLECTION_FISHBED.getServerObject();
    product = collection.getProducts().get(0);
    target = Area51DataSet.ORTHO_DOCUMENT.getServerObject();
    image = Area51DataSet.IMAGE_DOCUMENT.getServerObject();
    source = Area51DataSet.RAW_DOCUMENT.getServerObject();
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
  public void testFind()
  {
    Product result = Product.find(collection);

    Assert.assertNotNull(result);
    Assert.assertEquals(product.getOid(), result.getOid());
  }

  @Test
  @Request
  public void testGetCountGeneratedFromDocuments()
  {
    Assert.assertEquals(Integer.valueOf(1), product.getCountGeneratedFromDocuments());
  }

  @Test
  @Request
  public void testGetPageGeneratedFromDocuments()
  {
    Page<DocumentIF> page = product.getGeneratedFromDocuments(1, 10);

    Assert.assertNotNull(page);
    Assert.assertEquals(Long.valueOf(1), page.getCount());
    Assert.assertEquals(Integer.valueOf(1), page.getPageNumber());
    Assert.assertEquals(Integer.valueOf(10), page.getPageSize());

    List<DocumentIF> results = page.getResults();

    Assert.assertNotNull(results);
    Assert.assertEquals(1, results.size());
    Assert.assertEquals(source.getOid(), results.get(0).getOid());
  }

  @Test
  @Request
  public void testGetGeneratedFromDocuments()
  {
    List<DocumentIF> results = product.getGeneratedFromDocuments();

    Assert.assertNotNull(results);
    Assert.assertEquals(1, results.size());
    Assert.assertEquals(source.getOid(), results.get(0).getOid());
  }

  @Test
  @Request
  public void testGetDocuments()
  {
    List<DocumentIF> results = product.getDocuments();

    Assert.assertNotNull(results);
    Assert.assertEquals(2, results.size());
    Assert.assertEquals(target.getOid(), results.get(0).getOid());
  }

  @Test
  @Request
  public void testDelete()
  {
    product.delete();

    Assert.assertNull(Product.get(product.getOid()));
    Assert.assertNull(Document.get(target.getOid()));

    CollectionReport report = CollectionReport.getForCollection(collection).get(0);

    Assert.assertFalse(report.getProductsShared());
    Assert.assertNull(report.getProduct());

    product = null;
  }

  @Test
  @Request
  public void testCollectionReport()
  {
    CollectionReport report = CollectionReport.getForCollection(collection).get(0);

    Assert.assertFalse(report.getProductsShared());
    Assert.assertNotNull(report.getProduct());
  }

  @Test
  @Request
  public void testClear()
  {
    product.clear();

    Assert.assertEquals(0, product.getGeneratedFromDocuments().size());
    Assert.assertEquals(0, product.getDocuments().size());
    Assert.assertNull(Document.get(target.getOid()));
    Assert.assertNotNull(Document.get(source.getOid()));
  }

  @Test
  @Request
  public void testCalculateBoundingBox()
  {
    BBoxView bbox = product.calculateBoundingBox();

    Assert.assertNotNull(bbox);
  }

  @Test
  @Request
  public void testUpdateBoundingBox()
  {
    product.updateBoundingBox();

    Assert.assertNotNull(product.getBoundingBox());
    Assert.assertEquals("[-180,180,-90,90]", product.getBoundingBox());
  }

  @Test
  @Request
  public void testGetEnvelope()
  {
    product.updateBoundingBox();
    Envelope envelope = product.getEnvelope();

    Assert.assertNotNull(envelope);
    Assert.assertEquals(180, envelope.getMaxX(), 0);
    Assert.assertEquals(-180, envelope.getMinX(), 0);
    Assert.assertEquals(90, envelope.getMaxY(), 0);
    Assert.assertEquals(-90, envelope.getMinY(), 0);
  }

  @Test
  @Request
  public void testIsPublished_False()
  {
    Assert.assertFalse(product.isPublished());
  }

  @Test
  @Request
  public void testIsPublished_True()
  {
    product.setPublished(true);

    Assert.assertTrue(product.isPublished());
  }

  @Test
  @Request
  public void testGetAllZip()
  {
    SiteObject result = product.getAllZip();
    ;

    Assert.assertNotNull(result);
  }

  @Test
  @Request
  public void testDownloadAllZip()
  {
    RemoteFileObject result = product.downloadAllZip();

    Assert.assertNotNull(result);
  }

  @Test
  @Request
  public void testTogglePublished()
  {
    product.togglePublished();

    MockRemoteFileService service = (MockRemoteFileService) RemoteFileFacade.getService();
    java.util.Collection<RemoteFileAction> actions = service.getActions();

    Assert.assertEquals(4, actions.size());

    List<RemoteFileActionType> types = actions.stream().map(a -> a.getType()).collect(Collectors.toList());

    // On publish the data should be copied over to the public bucket
    // The stac items needs to be removed from the private bucket and
    // put into the public bucket as well as updating the asset urls
    Assert.assertTrue(types.contains(RemoteFileActionType.COPY));
    Assert.assertTrue(types.contains(RemoteFileActionType.REMOVE_STAC_ITEM));
    Assert.assertTrue(types.contains(RemoteFileActionType.PUT_STAC_ITEM));
  }

  @Test
  @Request
  public void testCalculateKeys()
  {
    Assert.assertNull(product.getImageKey());

    product.calculateKeys(Arrays.asList(collection));

    Assert.assertNotNull(product.getImageKey());
  }

  @Test
  @Request
  public void testGetMappableOrtho()
  {
    Assert.assertTrue(product.getMappableOrtho().isPresent());
  }

  @Test
  @Request
  public void testGetMappableDem()
  {
    Assert.assertFalse(product.getMappableDEM().isPresent());
  }

  @Test
  @Request
  public void testGetOrthoPng()
  {
    Assert.assertTrue(product.getOrthoPng().isPresent());
  }

  @Test
  @Request
  public void testGetMappableDocuments()
  {
    Assert.assertEquals(1, product.getMappableDocuments().size());
  }

  @Test
  @Request
  public void testGetProducts()
  {
    Assert.assertEquals(1, Product.getProducts().size());
  }

  @Test
  @Request
  public void testToStacItem()
  {
    UAV uav = collection.getUav();
    Sensor sensor = collection.getSensor();

    StacItem item = product.toStacItem();

    Assert.assertEquals(product.getOid(), item.getId());
    Assert.assertEquals(product.getPublished(), item.isPublished());
    Assert.assertEquals(product.getEnvelope(), item.getBbox());

    Properties properties = item.getProperties();
    Assert.assertEquals(collection.getCollectionDate(), properties.getDatetime());
    Assert.assertEquals(collection.getName(), properties.getTitle());
    Assert.assertEquals(collection.getName(), properties.getCollection());
    Assert.assertEquals(collection.getDescription(), properties.getDescription());
    Assert.assertEquals(product.getLastUpdateDate(), properties.getUpdated());
    Assert.assertEquals(sensor.getName(), properties.getSensor());
    Assert.assertEquals(uav.getPlatform().getName(), properties.getPlatform());
    Assert.assertEquals(uav.getFaaNumber(), properties.getFaaNumber());
    Assert.assertEquals(uav.getSerialNumber(), properties.getSerialNumber());
    Assert.assertEquals(Area51DataSet.SITE_AREA_51.getName(), properties.getSite());
    Assert.assertEquals(Area51DataSet.PROJECT_DREAMLAND.getName(), properties.getProject());
    Assert.assertEquals(Area51DataSet.MISSION_HAVE_DOUGHNUT.getName(), properties.getMission());

    Map<String, Asset> assets = item.getAssets();

    Assert.assertEquals(3, assets.size());

    List<StacLink> links = item.getLinks();

    Assert.assertEquals(1, links.size());
  }

}
