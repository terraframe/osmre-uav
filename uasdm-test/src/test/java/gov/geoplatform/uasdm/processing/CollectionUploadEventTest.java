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
package gov.geoplatform.uasdm.processing;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.Pair;
import com.runwaysdk.resource.FileResource;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask.WorkflowTaskStatus;
import gov.geoplatform.uasdm.bus.CollectionUploadEvent;
import gov.geoplatform.uasdm.bus.OrthoProcessingTask;
import gov.geoplatform.uasdm.bus.WorkflowTask;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.mock.MockRemoteFileService;
import gov.geoplatform.uasdm.mock.MockRemoteFileService.RemoteFileAction;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.odm.ODMProcessingTask;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.test.Area51DataSet;
import gov.geoplatform.uasdm.util.FileTestUtils;
import junit.framework.Assert;
import net.geoprism.GeoprismUser;

public class CollectionUploadEventTest
{
  private static Area51DataSet testData;

  private Collection           collection;

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

    testData.logIn();

    collection = Area51DataSet.COLLECTION_FISHBED.getServerObject();
  }

  private Pair<WorkflowTask, CollectionUploadEvent> createEvent(String uploadTarget, boolean createProduct)
  {
    GeoprismUser user = GeoprismUser.getByUsername(Area51DataSet.ADMIN_USER_NAME);

    String uploadId = UUID.randomUUID().toString();

    WorkflowTask task = (WorkflowTask) collection.createWorkflowTask(uploadId, uploadTarget);
    task.setGeoprismUser(user);
    task.setProcessDem(createProduct);
    task.setProcessOrtho(createProduct);
    task.setProcessPtcloud(createProduct);
    task.setStatus(WorkflowTaskStatus.PROCESSING.toString());
    task.setMessage("100% complete");
    task.apply();

    CollectionUploadEvent event = new CollectionUploadEvent();
    event.setGeoprismUser(user);
    event.setUploadId(uploadId);
    event.setComponent(collection.getOid());
    event.apply();

    return new Pair<WorkflowTask, CollectionUploadEvent>(task, event);
  }

  @After
  @Request
  public void tearDown()
  {
    testData.logOut();

    testData.tearDownInstanceData();
  }

  @Test
  @Request
  public void testHandleUploadFinishRaw() throws Exception
  {
    File file = FileTestUtils.createZip(this.getClass().getResource("/raw").toURI());

    final FileResource resource = new FileResource(file);

    String uploadTarget = ImageryComponent.RAW;

    Pair<WorkflowTask, CollectionUploadEvent> pair = this.createEvent(uploadTarget, true);

    try
    {
      CollectionUploadEvent event = pair.getSecond();

      event.handleUploadFinish(pair.getFirst(), uploadTarget, resource, "test", false);

      // Validate the files were uploaded
      MockRemoteFileService service = (MockRemoteFileService) RemoteFileFacade.getService();

      java.util.Collection<RemoteFileAction> actions = service.getActions();

      Assert.assertTrue(actions.size() > 5);

      List<DocumentIF> documents = collection.getDocuments();

      Assert.assertEquals(8, documents.size());
    }
    finally
    {
      pair.getSecond().delete();
    }
  }

  @Test
  @Request
  public void testHandleUploadFinishOrtho() throws Exception
  {
    Assert.assertEquals(3, collection.getDocuments().size());

    File file = new File(this.getClass().getResource("/odm_orthophoto_test.tif").toURI());

    final FileResource resource = new FileResource(file);

    String uploadTarget = ImageryComponent.ORTHO;

    Pair<WorkflowTask, CollectionUploadEvent> pair = this.createEvent(uploadTarget, true);

    try
    {
      CollectionUploadEvent event = pair.getSecond();

      event.handleUploadFinish(pair.getFirst(), uploadTarget, resource, "test", false);

      // Validate the files were removed and upldated
      MockRemoteFileService service = (MockRemoteFileService) RemoteFileFacade.getService();

      java.util.Collection<RemoteFileAction> actions = service.getActions();

      Assert.assertTrue(actions.size() > 2);

      List<DocumentIF> documents = collection.getDocuments();

      Assert.assertEquals(2, documents.size());

      DocumentIF document = documents.get(1);

      Assert.assertEquals("odm_orthophoto_test.tif", document.getName());
    }
    finally
    {
      pair.getSecond().delete();
    }

  }

  @Test
  @Request
  public void testHandleUploadFinishOrthoWithPngProcessing() throws Exception
  {
    File file = new File(this.getClass().getResource("/odm_orthophoto_test.tif").toURI());

    final FileResource resource = new FileResource(file);

    String uploadTarget = ImageryComponent.ORTHO;

    Pair<WorkflowTask, CollectionUploadEvent> pair = this.createEvent(uploadTarget, true);

    try
    {
      CollectionUploadEvent event = pair.getSecond();

      event.handleUploadFinish(pair.getFirst(), uploadTarget, resource, "test", true);

      List<DocumentIF> documents = collection.getDocuments();

      Assert.assertEquals(4, documents.size());

      List<String> names = documents.stream().map(doc -> doc.getName()).collect(Collectors.toList());

      Assert.assertTrue(names.contains("odm_orthophoto_test.tif"));
      Assert.assertTrue(names.contains("odm_orthophoto_test.cog.tif"));
      Assert.assertTrue(names.contains("odm_orthophoto_test.png"));

      OrthoProcessingTask task = OrthoProcessingTask.getByUploadId(event.getUploadId());

      Assert.assertNotNull(task);

      List<Product> products = collection.getProducts();

      Assert.assertEquals(1, products.size());
    }
    finally
    {
      pair.getSecond().delete();
    }
  }

  @Test
  @Request
  public void testHandleUploadFinishOrthoWithHillshadeProcessing() throws Exception
  {
    File file = new File(this.getClass().getResource("/odm_orthophoto_test.tif").toURI());

    final FileResource resource = new FileResource(file);

    String uploadTarget = ImageryComponent.DEM;

    Pair<WorkflowTask, CollectionUploadEvent> pair = this.createEvent(uploadTarget, true);

    try
    {
      CollectionUploadEvent event = pair.getSecond();

      event.handleUploadFinish(pair.getFirst(), uploadTarget, resource, "test", true);

      List<DocumentIF> documents = collection.getDocuments();

      Assert.assertEquals(6, documents.size());

      List<String> names = documents.stream().map(doc -> doc.getName()).collect(Collectors.toList());

      Assert.assertTrue(names.contains("odm_orthophoto_test.tif"));
      Assert.assertTrue(names.contains("dsm.cog.tif"));

      OrthoProcessingTask task = OrthoProcessingTask.getByUploadId(event.getUploadId());

      Assert.assertNotNull(task);

      List<Product> products = collection.getProducts();

      Assert.assertEquals(1, products.size());
    }
    finally
    {
      pair.getSecond().delete();
    }
  }

  @Test
  @Request
  public void testHandleUploadFinishRawWithProcessing() throws Exception
  {
    File file = FileTestUtils.createZip(this.getClass().getResource("/raw").toURI());

    final FileResource resource = new FileResource(file);

    String uploadTarget = ImageryComponent.RAW;

    Pair<WorkflowTask, CollectionUploadEvent> pair = this.createEvent(uploadTarget, true);

    try
    {
      CollectionUploadEvent event = pair.getSecond();

      event.handleUploadFinish(pair.getFirst(), uploadTarget, resource, "test", true);

      ODMProcessingTask task = ODMProcessingTask.getByUploadId(event.getUploadId());

      Assert.assertNotNull(task);

      Collection result = Collection.get(collection.getOid());

      Assert.assertNotNull(result.getImageHeight());
      Assert.assertNotNull(result.getImageWidth());

      List<DocumentIF> documents = result.getDocuments();

      Assert.assertEquals(14, documents.size());

      List<String> names = documents.stream().map(doc -> doc.getName()).collect(Collectors.toList());

      Assert.assertTrue(names.contains("DJI_0583.jpeg"));
      Assert.assertTrue(names.contains("DJI_0593.jpeg"));
      Assert.assertTrue(names.contains("DJI_0603.jpeg"));
      Assert.assertTrue(names.contains("DJI_0605.jpeg"));
      Assert.assertTrue(names.contains("SampleVideo_1280x720_5mb.mp4"));

      List<Product> products = result.getProducts();

      Assert.assertEquals(1, products.size());
    }
    finally
    {
      pair.getSecond().delete();
    }
  }

  @Test
  @Request
  public void testHandleUploadFinishRawWithProcessing_NoProduct() throws Exception
  {
    File file = FileTestUtils.createZip(this.getClass().getResource("/raw").toURI());

    final FileResource resource = new FileResource(file);

    String uploadTarget = ImageryComponent.RAW;

    Pair<WorkflowTask, CollectionUploadEvent> pair = this.createEvent(uploadTarget, false);

    try
    {
      CollectionUploadEvent event = pair.getSecond();

      event.handleUploadFinish(pair.getFirst(), uploadTarget, resource, "test", true);

      ODMProcessingTask task = ODMProcessingTask.getByUploadId(event.getUploadId());

      Assert.assertNull(task);
    }
    finally
    {
      pair.getSecond().delete();
    }
  }

}
