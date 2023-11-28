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
package gov.geoplatform.uasdm.remote;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.Area51DataTest;
import gov.geoplatform.uasdm.SpringInstanceTestClassRunner;
import gov.geoplatform.uasdm.TestConfig;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.graph.Document;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.mock.MockRemoteFileMetadata;
import gov.geoplatform.uasdm.mock.MockRemoteFileService;
import gov.geoplatform.uasdm.mock.MockRemoteFileService.RemoteFileAction;
import gov.geoplatform.uasdm.mock.MockRemoteFileService.RemoteFileActionType;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.Range;
import gov.geoplatform.uasdm.model.StacItem;
import gov.geoplatform.uasdm.processing.InMemoryMonitor;
import gov.geoplatform.uasdm.test.Area51DataSet;
import gov.geoplatform.uasdm.view.SiteObject;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class RemoteFileFacadeTest extends Area51DataTest
{
  private Product product;

  private Collection collection;

  private Document target;

  private MockRemoteFileService service;

  @Before
  @Request
  public void setUp()
  {
    testData.setUpInstanceData();

    collection = Area51DataSet.COLLECTION_FISHBED.getServerObject();
    product = collection.getProducts().get(0);
    target = Area51DataSet.ORTHO_DOCUMENT.getServerObject();

    service = (MockRemoteFileService) RemoteFileFacade.getService();
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
  public void testDownload() throws Exception
  {
    File tempFile = File.createTempFile("test", ".JPG");
    tempFile.deleteOnExit();

    RemoteFileFacade.download("test.JPG", tempFile);

    List<RemoteFileAction> actions = service.getActions();

    Assert.assertEquals(1, actions.size());

    RemoteFileAction action = actions.get(0);

    Assert.assertEquals(RemoteFileActionType.DOWNLOAD, action.getType());

    Object[] parameters = action.getParameters();

    Assert.assertEquals(2, parameters.length);
    Assert.assertEquals("test.JPG", parameters[0]);
    Assert.assertEquals(tempFile, parameters[1]);
  }

  @Test
  @Request
  public void testProxy()
  {
    RemoteFileFacade.proxy("test.JPG");

    List<RemoteFileAction> actions = service.getActions();

    Assert.assertEquals(1, actions.size());

    RemoteFileAction action = actions.get(0);

    Assert.assertEquals(RemoteFileActionType.PROXY, action.getType());

    Object[] parameters = action.getParameters();

    Assert.assertEquals(1, parameters.length);
    Assert.assertEquals("test.JPG", parameters[0]);
  }

  @Test
  @Request
  public void testDownloadByKey()
  {
    RemoteFileFacade.download("test.JPG");

    List<RemoteFileAction> actions = service.getActions();

    Assert.assertEquals(1, actions.size());

    RemoteFileAction action = actions.get(0);

    Assert.assertEquals(RemoteFileActionType.DOWNLOAD, action.getType());

    Object[] parameters = action.getParameters();

    Assert.assertEquals(1, parameters.length);
    Assert.assertEquals("test.JPG", parameters[0]);
  }

  @Test
  @Request
  public void testDownloadRange()
  {
    List<Range> ranges = Arrays.asList(new Range(0L, 1000L, 10000L));
    RemoteFileFacade.download("test.JPG", ranges);

    List<RemoteFileAction> actions = service.getActions();

    Assert.assertEquals(1, actions.size());

    RemoteFileAction action = actions.get(0);

    Assert.assertEquals(RemoteFileActionType.DOWNLOAD, action.getType());

    Object[] parameters = action.getParameters();

    Assert.assertEquals(2, parameters.length);
    Assert.assertEquals("test.JPG", parameters[0]);
    Assert.assertEquals(ranges, parameters[1]);
  }

  @Test
  @Request
  public void testCreateFolder()
  {
    RemoteFileFacade.createFolder("site/");

    List<RemoteFileAction> actions = service.getActions();

    Assert.assertEquals(1, actions.size());

    RemoteFileAction action = actions.get(0);

    Assert.assertEquals(RemoteFileActionType.CREATE_FOLDER, action.getType());

    Object[] parameters = action.getParameters();

    Assert.assertEquals(1, parameters.length);
    Assert.assertEquals("site/", parameters[0]);
  }

  @Test
  @Request
  public void testCopyObject()
  {
    String sourceKey = "source/test.JPG";
    String sourceBucket = AppProperties.getBucketName();
    String targetKey = "target/test.JPG";
    String targetBucket = AppProperties.getPublicBucketName();

    RemoteFileFacade.copyObject(sourceKey, sourceBucket, targetKey, targetBucket);

    List<RemoteFileAction> actions = service.getActions();

    Assert.assertEquals(1, actions.size());

    RemoteFileAction action = actions.get(0);

    Assert.assertEquals(RemoteFileActionType.COPY, action.getType());

    Object[] parameters = action.getParameters();

    Assert.assertEquals(4, parameters.length);
    Assert.assertEquals(sourceKey, parameters[0]);
    Assert.assertEquals(sourceBucket, parameters[1]);
    Assert.assertEquals(targetKey, parameters[2]);
    Assert.assertEquals(targetBucket, parameters[3]);
  }

  @Test
  @Request
  public void testDeleteObject()
  {
    String key = "source/test.JPG";

    RemoteFileFacade.deleteObject(key);

    List<RemoteFileAction> actions = service.getActions();

    Assert.assertEquals(1, actions.size());

    RemoteFileAction action = actions.get(0);

    Assert.assertEquals(RemoteFileActionType.DELETE, action.getType());

    Object[] parameters = action.getParameters();

    Assert.assertEquals(1, parameters.length);
    Assert.assertEquals(key, parameters[0]);
  }

  @Test
  @Request
  public void testDeleteObjectWithBucket()
  {
    String key = "source/test.JPG";
    String bucket = AppProperties.getBucketName();

    RemoteFileFacade.deleteObject(key, bucket);

    List<RemoteFileAction> actions = service.getActions();

    Assert.assertEquals(1, actions.size());

    RemoteFileAction action = actions.get(0);

    Assert.assertEquals(RemoteFileActionType.DELETE, action.getType());

    Object[] parameters = action.getParameters();

    Assert.assertEquals(2, parameters.length);
    Assert.assertEquals(key, parameters[0]);
    Assert.assertEquals(bucket, parameters[1]);
  }

  @Test
  @Request
  public void testDeleteObjects()
  {
    String key = "source/test.JPG";

    RemoteFileFacade.deleteObjects(key);

    List<RemoteFileAction> actions = service.getActions();

    Assert.assertEquals(1, actions.size());

    RemoteFileAction action = actions.get(0);

    Assert.assertEquals(RemoteFileActionType.DELETE_FOLDER, action.getType());

    Object[] parameters = action.getParameters();

    Assert.assertEquals(1, parameters.length);
    Assert.assertEquals(key, parameters[0]);
  }

  @Test
  @Request
  public void testDeleteObjectsWithBucket()
  {
    String key = "source/test.JPG";
    String bucket = AppProperties.getBucketName();

    RemoteFileFacade.deleteObjects(key, bucket);

    List<RemoteFileAction> actions = service.getActions();

    Assert.assertEquals(1, actions.size());

    RemoteFileAction action = actions.get(0);

    Assert.assertEquals(RemoteFileActionType.DELETE_FOLDER, action.getType());

    Object[] parameters = action.getParameters();

    Assert.assertEquals(2, parameters.length);
    Assert.assertEquals(key, parameters[0]);
    Assert.assertEquals(bucket, parameters[1]);
  }

  @Test
  @Request
  public void testGetItemCount()
  {
    String key = "source/test.JPG";

    RemoteFileFacade.getItemCount(key);

    List<RemoteFileAction> actions = service.getActions();

    Assert.assertEquals(1, actions.size());

    RemoteFileAction action = actions.get(0);

    Assert.assertEquals(RemoteFileActionType.COUNT, action.getType());

    Object[] parameters = action.getParameters();

    Assert.assertEquals(1, parameters.length);
    Assert.assertEquals(key, parameters[0]);
  }

  @Test
  @Request
  public void testGetSiteObjects()
  {
    LinkedList<SiteObject> items = new LinkedList<SiteObject>();
    RemoteFileFacade.getSiteObjects(collection, ImageryComponent.RAW, items, 1L, 20L);

    List<RemoteFileAction> actions = service.getActions();

    Assert.assertEquals(1, actions.size());

    RemoteFileAction action = actions.get(0);

    Assert.assertEquals(RemoteFileActionType.GET_ITEMS, action.getType());

    Object[] parameters = action.getParameters();

    Assert.assertEquals(5, parameters.length);
    Assert.assertEquals(collection, parameters[0]);
    Assert.assertEquals(ImageryComponent.RAW, parameters[1]);
    Assert.assertEquals(items, parameters[2]);
    Assert.assertEquals(Long.valueOf(1), parameters[3]);
    Assert.assertEquals(Long.valueOf(20), parameters[4]);
  }

  @Test
  @Request
  public void testUploadFile() throws IOException
  {
    File tempFile = File.createTempFile("test", ".JPG");
    tempFile.deleteOnExit();
    String key = ImageryComponent.RAW + "/" + "test.JPG";
    InMemoryMonitor monitor = new InMemoryMonitor();

    RemoteFileFacade.uploadFile(tempFile, key, monitor);

    List<RemoteFileAction> actions = service.getActions();

    Assert.assertEquals(1, actions.size());

    RemoteFileAction action = actions.get(0);

    Assert.assertEquals(RemoteFileActionType.UPLOAD, action.getType());

    Object[] parameters = action.getParameters();

    Assert.assertEquals(3, parameters.length);
    Assert.assertEquals(tempFile, parameters[0]);
    Assert.assertEquals(key, parameters[1]);
    Assert.assertEquals(monitor, parameters[2]);
  }

  @Test
  @Request
  public void testUploadStream() throws IOException
  {
    String key = ImageryComponent.RAW + "/" + "test.JPG";
    MockRemoteFileMetadata metadata = new MockRemoteFileMetadata();

    try (InputStream stream = this.getClass().getResourceAsStream("/raw/DJI_0583.jpeg"))
    {
      RemoteFileFacade.uploadFile(key, metadata, stream);

      List<RemoteFileAction> actions = service.getActions();

      Assert.assertEquals(1, actions.size());

      RemoteFileAction action = actions.get(0);

      Assert.assertEquals(RemoteFileActionType.UPLOAD, action.getType());

      Object[] parameters = action.getParameters();

      Assert.assertEquals(3, parameters.length);
      Assert.assertEquals(key, parameters[0]);
      Assert.assertEquals(metadata, parameters[1]);
      Assert.assertEquals(stream, parameters[2]);
    }
  }

  @Test
  @Request
  public void testCalculateSize()
  {
    RemoteFileFacade.calculateSize(collection);

    List<RemoteFileAction> actions = service.getActions();

    Assert.assertEquals(1, actions.size());

    RemoteFileAction action = actions.get(0);

    Assert.assertEquals(RemoteFileActionType.SIZE, action.getType());

    Object[] parameters = action.getParameters();

    Assert.assertEquals(1, parameters.length);
    Assert.assertEquals(collection, parameters[0]);
  }

  @Test
  @Request
  public void testUploadDirectory() throws Exception
  {
    String key = ImageryComponent.RAW;
    InMemoryMonitor monitor = new InMemoryMonitor();

    File directory = new File(this.getClass().getResource("/raw").toURI());

    RemoteFileFacade.uploadDirectory(directory, key, monitor, false);

    List<RemoteFileAction> actions = service.getActions();

    Assert.assertEquals(1, actions.size());

    RemoteFileAction action = actions.get(0);

    Assert.assertEquals(RemoteFileActionType.UPLOAD_FOLDER, action.getType());

    Object[] parameters = action.getParameters();

    Assert.assertEquals(4, parameters.length);
    Assert.assertEquals(directory, parameters[0]);
    Assert.assertEquals(key, parameters[1]);
    Assert.assertEquals(monitor, parameters[2]);
    Assert.assertEquals(false, parameters[3]);
  }

  @Test
  @Request
  public void testUploadDirectoryWithBucket() throws Exception
  {
    String key = ImageryComponent.RAW;
    String bucket = AppProperties.getBucketName();
    InMemoryMonitor monitor = new InMemoryMonitor();

    File directory = new File(this.getClass().getResource("/raw").toURI());

    RemoteFileFacade.uploadDirectory(directory, key, bucket, monitor, false);

    List<RemoteFileAction> actions = service.getActions();

    Assert.assertEquals(1, actions.size());

    RemoteFileAction action = actions.get(0);

    Assert.assertEquals(RemoteFileActionType.UPLOAD_FOLDER, action.getType());

    Object[] parameters = action.getParameters();

    Assert.assertEquals(5, parameters.length);
    Assert.assertEquals(directory, parameters[0]);
    Assert.assertEquals(key, parameters[1]);
    Assert.assertEquals(bucket, parameters[2]);
    Assert.assertEquals(monitor, parameters[3]);
    Assert.assertEquals(false, parameters[4]);
  }

  @Test
  @Request
  public void testObjectExists()
  {
    String key = ImageryComponent.RAW + "/test.JPG";

    RemoteFileFacade.objectExists(key);

    List<RemoteFileAction> actions = service.getActions();

    Assert.assertEquals(1, actions.size());

    RemoteFileAction action = actions.get(0);

    Assert.assertEquals(RemoteFileActionType.EXISTS, action.getType());

    Object[] parameters = action.getParameters();

    Assert.assertEquals(1, parameters.length);
    Assert.assertEquals(key, parameters[0]);
  }

  @Test
  @Request
  public void testPutStacItem()
  {
    StacItem item = product.toStacItem();

    RemoteFileFacade.putStacItem(item);

    List<RemoteFileAction> actions = service.getActions();

    Assert.assertEquals(1, actions.size());

    RemoteFileAction action = actions.get(0);

    Assert.assertEquals(RemoteFileActionType.PUT_STAC_ITEM, action.getType());

    Object[] parameters = action.getParameters();

    Assert.assertEquals(1, parameters.length);
    Assert.assertEquals(item, parameters[0]);
  }

  @Test
  @Request
  public void testRemoveStacItem()
  {
    RemoteFileFacade.removeStacItem(product);

    List<RemoteFileAction> actions = service.getActions();

    Assert.assertEquals(1, actions.size());

    RemoteFileAction action = actions.get(0);

    Assert.assertEquals(RemoteFileActionType.REMOVE_STAC_ITEM, action.getType());

    Object[] parameters = action.getParameters();

    Assert.assertEquals(1, parameters.length);
    Assert.assertEquals(product, parameters[0]);
  }

  @Test
  @Request
  public void testGetStacItem()
  {
    RemoteFileFacade.getStacItem(product);

    List<RemoteFileAction> actions = service.getActions();

    Assert.assertEquals(1, actions.size());

    RemoteFileAction action = actions.get(0);

    Assert.assertEquals(RemoteFileActionType.GET_STAC_ITEM, action.getType());

    Object[] parameters = action.getParameters();

    Assert.assertEquals(1, parameters.length);
    Assert.assertEquals(product, parameters[0]);
  }

  @Test
  @Request
  public void testGetBoundingBox()
  {
    Assert.assertNotNull(service.getBoundingBox(product, target));
  }

}
