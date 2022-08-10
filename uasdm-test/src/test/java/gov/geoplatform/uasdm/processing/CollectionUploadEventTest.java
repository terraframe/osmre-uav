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
import gov.geoplatform.uasdm.mock.MockRemoteFileService;
import gov.geoplatform.uasdm.mock.MockRemoteFileService.RemoteFileAction;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.odm.ODMProcessingTask;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.test.Area51DataSet;
import junit.framework.Assert;
import net.geoprism.GeoprismUser;

public class CollectionUploadEventTest
{
  private static Area51DataSet testData;

  private Collection collection;

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

  private Pair<WorkflowTask, CollectionUploadEvent> createEvent(String uploadTarget)
  {
    GeoprismUser user = GeoprismUser.getByUsername(Area51DataSet.ADMIN_USER_NAME);

    String uploadId = UUID.randomUUID().toString();

    WorkflowTask task = (WorkflowTask) collection.createWorkflowTask(uploadId, uploadTarget);
    task.setGeoprismUser(user);
    task.setProcessDem(true);
    task.setProcessOrtho(true);
    task.setProcessPtcloud(true);
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
    File file = new File(this.getClass().getResource("/small-fix-with-video.zip").toURI());

    final FileResource resource = new FileResource(file);

    String uploadTarget = ImageryComponent.RAW;

    Pair<WorkflowTask, CollectionUploadEvent> pair = this.createEvent(uploadTarget);

    try
    {
      CollectionUploadEvent event = pair.getSecond();

      event.handleUploadFinish(pair.getFirst(), uploadTarget, resource, "test", false);

      // Validate the files were uploaded
      MockRemoteFileService service = (MockRemoteFileService) RemoteFileFacade.getService();

      java.util.Collection<RemoteFileAction> actions = service.getActions();

      Assert.assertEquals(6, actions.size());

      List<DocumentIF> documents = collection.getDocuments();

      Assert.assertEquals(6, documents.size());
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
    collection.createDocumentIfNotExist(collection.getS3location() + Collection.ORTHO + "/test.tif", "test.tif", "", "");

    Assert.assertEquals(1, collection.getDocuments().size());

    File file = new File(this.getClass().getResource("/odm_orthophoto_test.tif").toURI());

    final FileResource resource = new FileResource(file);

    String uploadTarget = ImageryComponent.ORTHO;

    Pair<WorkflowTask, CollectionUploadEvent> pair = this.createEvent(uploadTarget);

    try
    {
      CollectionUploadEvent event = pair.getSecond();

      event.handleUploadFinish(pair.getFirst(), uploadTarget, resource, "test", false);

      // Validate the files were removed and upldated
      MockRemoteFileService service = (MockRemoteFileService) RemoteFileFacade.getService();

      java.util.Collection<RemoteFileAction> actions = service.getActions();

      Assert.assertEquals(2, actions.size());

      List<DocumentIF> documents = collection.getDocuments();

      Assert.assertEquals(1, documents.size());

      DocumentIF document = documents.get(0);

      Assert.assertEquals("odm_orthophoto_test.tif", document.getName());
    }
    finally
    {
      pair.getSecond().delete();
    }

  }

  @Test
  @Request
  public void testHandleUploadFinishOrthoWithProcessing() throws Exception
  {
    File file = new File(this.getClass().getResource("/odm_orthophoto_test.tif").toURI());

    final FileResource resource = new FileResource(file);

    String uploadTarget = ImageryComponent.ORTHO;

    Pair<WorkflowTask, CollectionUploadEvent> pair = this.createEvent(uploadTarget);

    try
    {
      CollectionUploadEvent event = pair.getSecond();

      event.handleUploadFinish(pair.getFirst(), uploadTarget, resource, "test", true);

      List<DocumentIF> documents = collection.getDocuments();

      Assert.assertEquals(2, documents.size());

      List<String> names = documents.stream().map(doc -> doc.getName()).collect(Collectors.toList());

      Assert.assertTrue(names.contains("odm_orthophoto_test.tif"));
      Assert.assertTrue(names.contains("odm_orthophoto_test.png"));

      OrthoProcessingTask task = OrthoProcessingTask.getByUploadId(event.getUploadId());

      Assert.assertNotNull(task);
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
    File file = new File(this.getClass().getResource("/small-fix-with-video.zip").toURI());

    final FileResource resource = new FileResource(file);

    String uploadTarget = ImageryComponent.RAW;

    Pair<WorkflowTask, CollectionUploadEvent> pair = this.createEvent(uploadTarget);

    try
    {
      CollectionUploadEvent event = pair.getSecond();

      event.handleUploadFinish(pair.getFirst(), uploadTarget, resource, "test", true);

      ODMProcessingTask task = ODMProcessingTask.getByUploadId(event.getUploadId());

      Assert.assertNotNull(task);

      List<DocumentIF> documents = collection.getDocuments();

      Assert.assertEquals(6, documents.size());

      List<String> names = documents.stream().map(doc -> doc.getName()).collect(Collectors.toList());
      
      Assert.assertTrue(names.contains("DJI_0583.jpeg"));
      Assert.assertTrue(names.contains("DJI_0593.jpeg"));
      Assert.assertTrue(names.contains("DJI_0603.jpeg"));
      Assert.assertTrue(names.contains("DJI_0605.jpeg"));
      Assert.assertTrue(names.contains("sewer_pull_from_city_tap.mp4"));
      Assert.assertTrue(names.contains("sewer_push_to_city_tap.mp4"));
    }
    finally
    {
      pair.getSecond().delete();
    }
  }

}
