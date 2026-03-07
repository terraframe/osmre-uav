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
package gov.geoplatform.uasdm.processing;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.uasdm.Area51DataTest;
import gov.geoplatform.uasdm.SpringInstanceTestClassRunner;
import gov.geoplatform.uasdm.TestConfig;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.test.Area51DataSet;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class ImageryProcessingJobTest extends Area51DataTest
{
  private static Collection collection;

  @Before
  @Request
  public void setUp()
  {
    testData.setUpInstanceData();

    testData.logIn();

    collection = Area51DataSet.COLLECTION_FISHBED.getServerObject();
  }

  @After
  public void tearDown()
  {
    testData.logOut();

    testData.tearDownInstanceData();
  }

  @Test
  public void testProcessFiles() throws Exception
  {
//    String sessionId = testData.clientRequest.getSessionId();
//
//    MockRequestParser parser = new MockRequestParser(collection.getOid());
//    parser.getCustomParams().put(ProcessConfiguration.TYPE, ProcessType.ODM.name());
//
//    JSONObject job = new WorkflowService().createUploadTask(sessionId, parser);
//    JSONObject task = job.getJSONObject("currentTask");
//
//    Assert.assertEquals(parser.getUuid(), task.getString("uploadId"));
//    Assert.assertEquals(Area51DataSet.SENSOR.getName(), task.getString("sensorName"));
//    Assert.assertNotNull(task.getString("lastUpdateDate"));
//    Assert.assertNotNull(task.getString("oid"));
//    Assert.assertEquals("UAV data upload for collection [Fishbed_E]", task.getString("label"));
//    Assert.assertEquals(collection.getOid(), task.getString("collection"));
//    Assert.assertEquals("100% complete", task.getString("message"));
//    Assert.assertEquals("Fishbed_E", task.getString("collectionLabel"));
//    Assert.assertEquals(parser.getUploadTarget(), task.getString("uploadTarget"));
//    Assert.assertEquals(3, task.getJSONArray("ancestors").length());
//    Assert.assertEquals(0, task.getJSONArray("actions").length());
//    Assert.assertNotNull(task.getString("createDate"));
//    Assert.assertEquals("Processing", task.getString("status"));
//
//    process(sessionId, parser);
  }

  @Request(RequestType.SESSION)
  private void process(String sessionId) throws Exception
  {
//    String admin = GeoprismUser.getByUsername(TestDataSet.ADMIN_USER_NAME).getOid();
//
//    AbstractUploadTask task = ImageryWorkflowTask.getTaskByUploadId(parser.getUuid());
//    File file = FileTestUtils.createZip(this.getClass().getResource("/raw").toURI());
//    File archive = File.createTempFile("archive", ".zip");
//    try
//    {
//      FileUtils.copyFile(file, archive);
//
//      JobHistory history = ImageryProcessingJob.processFiles(admin, parser, archive);
//
//      Assert.assertNotNull(history);
//
//      SchedulerTestUtils.waitUntilStatus(history.getOid(), AllJobStatus.SUCCESS);
//
//      // Validate the job
//      ImageryProcessingJob job = (ImageryProcessingJob) history.getAllJob().getAll().get(0);
//
//      Assert.assertEquals(admin, job.getRunAsUserId());
//      Assert.assertEquals(task.getOid(), job.getWorkflowTaskOid());
//      Assert.assertTrue(job.getImageryFile().length() > 0);
//      Assert.assertEquals(parser.getUploadTarget(), job.getUploadTarget());
//      Assert.assertEquals("", job.getOutFileNamePrefix());
//      Assert.assertEquals(Boolean.FALSE, job.getProcessUpload());
//
//      history = JobHistory.get(history.getOid());
//
//      // Validate a collection event was created
//      CollectionUploadEvent event = CollectionUploadEvent.getByUploadId(parser.getUuid());
//
//      Assert.assertNotNull(event);
//      Assert.assertEquals(admin, event.getGeoprismUserOid());
//      Assert.assertEquals(parser.getUuid(), event.getUploadId());
//      Assert.assertEquals(Area51DataSet.COLLECTION_FISHBED.getServerObject().getOid(), event.getComponent());
//
//      // Validate the files were uploaded
//      MockRemoteFileService service = (MockRemoteFileService) RemoteFileFacade.getService();
//
//      java.util.Collection<RemoteFileAction> actions = service.getActions();
//
//      Assert.assertTrue(actions.size() > 5);
//    }
//    finally
//    {
//      FileUtils.deleteQuietly(archive);
//    }
  }

}
