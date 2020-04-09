package gov.geoplatform.uasdm;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;

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
}
