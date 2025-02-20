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
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import com.runwaysdk.resource.ArchiveFileResource;
import com.runwaysdk.resource.FileResource;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.Area51DataTest;
import gov.geoplatform.uasdm.InstanceTestClassListener;
import gov.geoplatform.uasdm.SpringInstanceTestClassRunner;
import gov.geoplatform.uasdm.TestConfig;
import gov.geoplatform.uasdm.bus.WorkflowAction;
import gov.geoplatform.uasdm.bus.WorkflowTask;
import gov.geoplatform.uasdm.test.Area51DataSet;
import gov.geoplatform.uasdm.util.FileTestUtils;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class UploadArchiveTest extends Area51DataTest implements InstanceTestClassListener
{
  @Test
  @Request
  public void testZipArchive() throws URISyntaxException, IOException
  {
    Collection collection = Area51DataSet.COLLECTION_FISHBED.getServerObject();

    WorkflowTask task = new WorkflowTask();
    task.setGeoprismUser(Area51DataSet.USER_ADMIN.getServerObject());
    task.setComponent(collection.getOid());
    task.setUploadId("testID2");
    task.setStatus("Test Status");
    task.apply();

    File file = FileTestUtils.createZip(this.getClass().getResource("/raw").toURI());

    final ArchiveFileResource resource = new ArchiveFileResource(new FileResource(file));

    List<String> results = collection.uploadArchive(task, resource, "raw", Area51DataSet.PRODUCT.getServerObject());

    Assert.assertEquals(5, results.size());

    List<WorkflowAction> actions = task.getActions();

    Assert.assertEquals(0, actions.size());
  }

  @Test(expected = UnsupportedOperationException.class)
  @Request
  public void testUploadUnknownUploadTarget() throws URISyntaxException, IOException
  {
    Collection collection = Area51DataSet.COLLECTION_FISHBED.getServerObject();

    WorkflowTask task = new WorkflowTask();
    task.setGeoprismUser(Area51DataSet.USER_ADMIN.getServerObject());
    task.setComponent(collection.getOid());
    task.setUploadId("testID2");
    task.setStatus("Test Status");
    task.apply();

    File file = FileTestUtils.createZip(this.getClass().getResource("/raw").toURI());

    final FileResource resource = new FileResource(file);

    collection.uploadArchive(task, resource, "RAW", Area51DataSet.PRODUCT.getServerObject());
  }

  @Test
  @Request
  public void testTarGzArchive() throws URISyntaxException, IOException
  {
    Collection collection = Area51DataSet.COLLECTION_FISHBED.getServerObject();

    WorkflowTask task = new WorkflowTask();
    task.setGeoprismUser(Area51DataSet.USER_ADMIN.getServerObject());
    task.setComponent(collection.getOid());
    task.setUploadId("testID");
    task.setStatus("Test Status");
    task.apply();

    File file = FileTestUtils.createZip(this.getClass().getResource("/raw").toURI());

    final ArchiveFileResource resource = new ArchiveFileResource(new FileResource(file));

    List<String> results = collection.uploadArchive(task, resource, "raw", Area51DataSet.PRODUCT.getServerObject());

    Assert.assertEquals(5, results.size());

    List<WorkflowAction> actions = task.getActions();

    Assert.assertEquals(0, actions.size());
  }

  @Test
  @Request
  public void testUploadOrtho() throws URISyntaxException
  {
    Collection collection = Area51DataSet.COLLECTION_FISHBED.getServerObject();

    WorkflowTask task = new WorkflowTask();
    task.setGeoprismUser(Area51DataSet.USER_ADMIN.getServerObject());
    task.setComponent(collection.getOid());
    task.setUploadId("testID2");
    task.setStatus("Test Status");
    task.apply();

    File file = new File(this.getClass().getResource("/odm_orthophoto_test.tif").toURI());

    final FileResource resource = new FileResource(file);

    List<String> results = collection.uploadArchive(task, resource, "ortho", Area51DataSet.PRODUCT.getServerObject());

    Assert.assertEquals(1, results.size());

    List<WorkflowAction> actions = task.getActions();

    Assert.assertEquals(0, actions.size());
  }

  @Test
  @Request
  public void testUploadDem() throws URISyntaxException
  {
    Collection collection = Area51DataSet.COLLECTION_FISHBED.getServerObject();

    WorkflowTask task = new WorkflowTask();
    task.setGeoprismUser(Area51DataSet.USER_ADMIN.getServerObject());
    task.setComponent(collection.getOid());
    task.setUploadId("testID2");
    task.setStatus("Test Status");
    task.apply();

    File file = new File(this.getClass().getResource("/dsm.tif").toURI());

    final FileResource resource = new FileResource(file);

    List<String> results = collection.uploadArchive(task, resource, "dem", Area51DataSet.PRODUCT.getServerObject());

    Assert.assertEquals(1, results.size());

    List<WorkflowAction> actions = task.getActions();

    Assert.assertEquals(0, actions.size());
  }

  @Test
  @Request
  public void testUploadPtcloud() throws URISyntaxException
  {
    Collection collection = Area51DataSet.COLLECTION_FISHBED.getServerObject();

    WorkflowTask task = new WorkflowTask();
    task.setGeoprismUser(Area51DataSet.USER_ADMIN.getServerObject());
    task.setComponent(collection.getOid());
    task.setUploadId("testID2");
    task.setStatus("Test Status");
    task.apply();

    File file = new File(this.getClass().getResource("/odm_georeferenced_model.laz").toURI());

    final FileResource resource = new FileResource(file);

    List<String> results = collection.uploadArchive(task, resource, "ptcloud", Area51DataSet.PRODUCT.getServerObject());

    Assert.assertEquals(1, results.size());

    List<WorkflowAction> actions = task.getActions();

    Assert.assertEquals(0, actions.size());
  }
}
