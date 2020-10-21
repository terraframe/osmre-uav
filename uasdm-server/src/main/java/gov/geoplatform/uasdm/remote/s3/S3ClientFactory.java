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
package gov.geoplatform.uasdm.remote.s3;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ecs.AmazonECS;
import com.amazonaws.services.ecs.AmazonECSClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;

import gov.geoplatform.uasdm.AppProperties;

public class S3ClientFactory
{
  public static AmazonS3 createClient()
  {
    final Regions region = Regions.valueOf(AppProperties.getBucketRegion());

    BasicAWSCredentials awsCreds = new BasicAWSCredentials(AppProperties.getS3AccessKey(), AppProperties.getS3SecretKey());
    AmazonS3 client = AmazonS3ClientBuilder.standard().withRegion(region).withCredentials(new AWSStaticCredentialsProvider(awsCreds)).build();

    return client;
  }

  public static TransferManager createTransferManager()
  {
    final AmazonS3 client = createClient();

    return TransferManagerBuilder.standard().withS3Client(client).build();
  }
  
  public static AmazonECS createECSClient()
  {
    final Regions region = Regions.valueOf(AppProperties.getBucketRegion());

    BasicAWSCredentials awsCreds = new BasicAWSCredentials(AppProperties.getECSAccessKey(), AppProperties.getECSSecretKey());
    AmazonECS client = AmazonECSClientBuilder.standard().withRegion(region).withCredentials(new AWSStaticCredentialsProvider(awsCreds)).build();

    return client;
  }
}
