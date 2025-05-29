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

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.Area51DataTest;
import gov.geoplatform.uasdm.InstanceTestClassListener;
import gov.geoplatform.uasdm.SpringInstanceTestClassRunner;
import gov.geoplatform.uasdm.TestConfig;
import gov.geoplatform.uasdm.bus.WorkflowAction;
import gov.geoplatform.uasdm.bus.WorkflowTask;
import gov.geoplatform.uasdm.test.Area51DataSet;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class WorkflowTaskTest extends Area51DataTest implements InstanceTestClassListener
{
  @Test
  @Request
  public void testToJSON()
  {
    Collection collection = Area51DataSet.COLLECTION_FISHBED.getServerObject();

    WorkflowTask task = new WorkflowTask();
    task.setGeoprismUser(Area51DataSet.USER_ADMIN.getServerObject());
    task.setComponent(collection.getOid());
    task.setUploadId("testID");
    task.setStatus("Test Status");
    task.setTaskLabel("Test label");
    task.apply();

    WorkflowAction action = new WorkflowAction();
    action.setActionType("TEST");
    action.setDescription("TEST");
    action.setWorkflowTask(task);
    action.apply();

    JSONObject json = task.toJSON();

    Assert.assertTrue(json.has("actions"));
    Assert.assertEquals(task.getTaskLabel(), json.getString("label"));

    JSONArray actions = json.getJSONArray("actions");

    Assert.assertEquals(1, actions.length());
  }

}
