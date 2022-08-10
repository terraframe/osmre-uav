package gov.geoplatform.uasdm.processing;

import java.util.UUID;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.Pair;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask.WorkflowTaskStatus;
import gov.geoplatform.uasdm.bus.CollectionUploadEvent;
import gov.geoplatform.uasdm.bus.WorkflowTask;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.test.Area51DataSet;
import net.geoprism.GeoprismUser;

public class OrthoProcessingTaskTest
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
  }
}
