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

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ecs.AmazonECS;
import com.amazonaws.services.ecs.AmazonECSClientBuilder;
import com.amazonaws.services.ecs.model.AwsVpcConfiguration;
import com.amazonaws.services.ecs.model.ContainerOverride;
import com.amazonaws.services.ecs.model.Failure;
import com.amazonaws.services.ecs.model.KeyValuePair;
import com.amazonaws.services.ecs.model.LaunchType;
import com.amazonaws.services.ecs.model.NetworkConfiguration;
import com.amazonaws.services.ecs.model.RunTaskRequest;
import com.amazonaws.services.ecs.model.RunTaskResult;
import com.amazonaws.services.ecs.model.TaskOverride;
import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.graph.Collection;

public class ErosService
{
  @Request(RequestType.SESSION)
  public JsonObject push(String sessionId, String collectionId)
  {
    Collection collection = Collection.get(collectionId);
    
    final Regions region = Regions.valueOf(AppProperties.getBucketRegion());

    BasicAWSCredentials awsCreds = new BasicAWSCredentials(AppProperties.getErosECSAccessKey(), AppProperties.getErosECSSecretKey());
    AmazonECS client = AmazonECSClientBuilder.standard().withRegion(region).withCredentials(new AWSStaticCredentialsProvider(awsCreds)).build();
    
    RunTaskRequest request = new RunTaskRequest().withTaskDefinition(AppProperties.getErosTask());
    request.withCluster(AppProperties.getErosCluster()).withLaunchType(LaunchType.FARGATE).withCount(1);
    
    TaskOverride taskOverrides = new TaskOverride();
    ContainerOverride containerOverrides = new ContainerOverride();
    containerOverrides.withName(AppProperties.getErosContainerName());
    containerOverrides.withEnvironment(new KeyValuePair().withName("EROSSYNC_FTP_TARGET_PATH").withValue(AppProperties.getErosFtpTargetPath()));
    containerOverrides.withEnvironment(new KeyValuePair().withName("EROSSYNC_FTP_SERVER").withValue(AppProperties.getErosFtpServerUrl()));
    containerOverrides.withEnvironment(new KeyValuePair().withName("EROSSYNC_FTP_USERNAME").withValue(AppProperties.getErosFtpUsername()));
    containerOverrides.withEnvironment(new KeyValuePair().withName("EROSSYNC_FTP_PASSWORD").withValue(AppProperties.getErosFtpPassword()));
    containerOverrides.withEnvironment(new KeyValuePair().withName("EROSSYNC_FTP_PORT").withValue(AppProperties.getErosFtpPort()));
    containerOverrides.withEnvironment(new KeyValuePair().withName("EROSSYNC_FTP_PASSIVE").withValue(AppProperties.getErosFtpPassive()));
    containerOverrides.withEnvironment(new KeyValuePair().withName("EROSSYNC_S3_BUCKET").withValue(AppProperties.getBucketName()));
    containerOverrides.withEnvironment(new KeyValuePair().withName("EROSSYNC_S3_SOURCE_PATH").withValue(collection.getS3location()));
    containerOverrides.withEnvironment(new KeyValuePair().withName("AWS_REGION").withValue(AppProperties.getBucketRegion()));
    taskOverrides.withContainerOverrides(containerOverrides);
    request.withOverrides(taskOverrides);
    
    NetworkConfiguration networkConfiguration = new NetworkConfiguration();
    AwsVpcConfiguration awsvpcConfiguration = new AwsVpcConfiguration();
    awsvpcConfiguration.withSubnets(AppProperties.getErosSubnets());
    networkConfiguration.withAwsvpcConfiguration(awsvpcConfiguration);
    request.withNetworkConfiguration(networkConfiguration);
    
    
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
