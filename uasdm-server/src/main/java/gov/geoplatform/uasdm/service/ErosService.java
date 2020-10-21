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
package gov.geoplatform.uasdm.service;

import com.amazonaws.services.ecs.AmazonECS;
import com.amazonaws.services.ecs.model.ContainerOverride;
import com.amazonaws.services.ecs.model.Failure;
import com.amazonaws.services.ecs.model.LaunchType;
import com.amazonaws.services.ecs.model.RunTaskRequest;
import com.amazonaws.services.ecs.model.RunTaskResult;
import com.amazonaws.services.ecs.model.TaskOverride;
import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.uasdm.remote.s3.S3ClientFactory;

public class ErosService
{
  @Request(RequestType.SESSION)
  public JsonObject push(String sessionId, String collectionId)
  {
    AmazonECS client = S3ClientFactory.createECSClient();
    
    RunTaskRequest request = new RunTaskRequest().withTaskDefinition("idm-fargate-erossync");
    request.withCluster("").withLaunchType(LaunchType.FARGATE).withCount(1);
    
    TaskOverride taskOverrides = new TaskOverride();
    ContainerOverride containerOverrides = new ContainerOverride();
    containerOverrides.withCommand(new String[] {"one", "two"});
    taskOverrides.withContainerOverrides(containerOverrides);
    request.withOverrides(taskOverrides);
    
    
    RunTaskResult response = client.runTask(request);
    
    JsonObject resp = new JsonObject();
    
    if (response.getFailures().size() > 0)
    {
      resp.addProperty("status", "failure");
      
      Failure failure = response.getFailures().get(0);
      resp.addProperty("message", failure.getArn() + " : " + failure.getReason() + ". " + failure.getDetail());
    }
    else
    {
      resp.addProperty("status", "success");
    }
    
    return resp;
  }
}
