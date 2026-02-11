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
package gov.geoplatform.uasdm.remote.s3;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.runwaysdk.RunwayException;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.session.Session;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.GenericException;
import gov.geoplatform.uasdm.cog.TiTillerProxy;
import gov.geoplatform.uasdm.cog.TiTillerProxy.BBoxView;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.StacItem;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.processing.StatusMonitorIF;
import gov.geoplatform.uasdm.remote.RemoteFileMetadata;
import gov.geoplatform.uasdm.remote.RemoteFileObject;
import gov.geoplatform.uasdm.remote.RemoteFileService;
import gov.geoplatform.uasdm.view.SiteObject;
import gov.geoplatform.uasdm.view.SiteObjectsResultSet;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Uri;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest.Builder;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectVersionsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.paginators.ListObjectVersionsIterable;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.CompletedDirectoryUpload;
import software.amazon.awssdk.transfer.s3.model.CompletedFileDownload;
import software.amazon.awssdk.transfer.s3.model.CompletedFileUpload;
import software.amazon.awssdk.transfer.s3.model.DirectoryUpload;
import software.amazon.awssdk.transfer.s3.model.DownloadFileRequest;
import software.amazon.awssdk.transfer.s3.model.FileDownload;
import software.amazon.awssdk.transfer.s3.model.FileUpload;
import software.amazon.awssdk.transfer.s3.model.UploadDirectoryRequest;
import software.amazon.awssdk.transfer.s3.model.UploadFileRequest;

public class S3RemoteFileService implements RemoteFileService
{
  public static final String STAC_BUCKET = "-stac-";

  private Logger             logger      = LoggerFactory.getLogger(S3RemoteFileService.class);

  private S3AsyncClient      asyncClient;

  private S3Client           client;

  private AwsBasicCredentials credentials()
  {
    return AwsBasicCredentials.create(AppProperties.getS3AccessKey(), AppProperties.getS3SecretKey());
  }

  private Region region()
  {
    return Region.of(AppProperties.getBucketRegion());
  }

  public synchronized S3AsyncClient getAsyncClient()
  {
    if (asyncClient == null)
    {
      this.asyncClient = S3AsyncClient.builder() //
          .region(region()) //
          .credentialsProvider(StaticCredentialsProvider.create(credentials())) //
          .build();
    }
    return this.asyncClient;

  }

  public synchronized S3Client getClient()
  {
    if (client == null)
    {
      this.client = S3Client.builder() //
          .region(region()) //
          .credentialsProvider(StaticCredentialsProvider.create(credentials())) //
          .build();
    }

    return this.client;
  }

  public S3TransferManager createTransferManager()
  {
    return S3TransferManager.builder() //
        .s3Client(getAsyncClient()) //
        .build();
  }

  public void destroy()
  {
    if (this.client != null)
    {
      this.client.close();
    }

    if (this.asyncClient != null)
    {
      this.asyncClient.close();
    }

    this.client = null;
    this.asyncClient = null;
  }

  @Override
  public Long download(String key, File destination)
  {
    try
    {
      S3TransferManager transferManager = createTransferManager();

      String bucketName = AppProperties.getBucketName();

      DownloadFileRequest downloadFileRequest = DownloadFileRequest.builder()//
          .getObjectRequest(b -> b.bucket(bucketName).key(key)) //
          .destination(destination).build();

      FileDownload downloadFile = transferManager.downloadFile(downloadFileRequest);

      CompletedFileDownload downloadResult = downloadFile.completionFuture().join();

      logger.info("Content length [{}]", downloadResult.response().contentLength());

      return downloadResult.response().contentLength();
    }
    catch (SdkClientException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  @Override
  public RemoteFileObject proxy(String url)
  {
    try
    {
      S3Client client = getClient();

      S3Utilities s3Utilities = client.utilities();

      S3Uri s3Uri = s3Utilities.parseUri(URI.create(url));

      String bucket = s3Uri.bucket().orElseThrow(() -> {
        GenericException ex = new GenericException();
        ex.setUserMessage("Unable to find bucket from url [" + url + "]");
        return ex;
      });

      String key = s3Uri.key().orElseThrow(() -> {
        GenericException ex = new GenericException();
        ex.setUserMessage("Unable to find key from url [" + url + "]");
        return ex;
      });

      GetObjectRequest request = GetObjectRequest.builder().bucket(bucket).key(key).build();

      ResponseInputStream<GetObjectResponse> stream = client.getObject(request);

      return new S3ObjectWrapper(stream, key);
    }
    catch (SdkClientException e)
    {
      throw new ProgrammingErrorException(e);
    }

  }

  @Override
  public String presignUrl(String key, Duration duration)
  {
    String bucketName = AppProperties.getBucketName();
    AwsBasicCredentials awsCreds = AwsBasicCredentials.create(AppProperties.getS3AccessKey(), AppProperties.getS3SecretKey());

    S3Presigner.Builder builder = S3Presigner.builder() //
        .region(region()) //
        .credentialsProvider(StaticCredentialsProvider.create(awsCreds));

    try (S3Presigner presigner = builder.build())
    {
      GetObjectRequest objectRequest = GetObjectRequest.builder() //
          .bucket(bucketName) //
          .key(key) //
          .build();

      GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder() //
          .signatureDuration(Duration.ofHours(24)) //
          .getObjectRequest(objectRequest) //
          .build();

      PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
      logger.trace("Presigned URL: [{}]", presignedRequest.url().toString());
      logger.trace("HTTP method: [{}]", presignedRequest.httpRequest().method());

      return presignedRequest.url().toExternalForm();
    }
    catch (SdkClientException e)
    {
      throw new ProgrammingErrorException(e);
    }

  }

  @Override
  public RemoteFileObject download(String key)
  {
    String bucket = AppProperties.getBucketName();

    return getFile(key, bucket);
  }

  private RemoteFileObject getFile(String key, String bucket)
  {
    try
    {

      S3Client client = getClient();
      GetObjectRequest request = GetObjectRequest.builder().bucket(bucket).key(key).build();

      ResponseInputStream<GetObjectResponse> stream = client.getObject(request);

      return new S3ObjectWrapper(stream, key);
    }
    catch (SdkClientException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  @Override
  public RemoteFileObject download(String key, String range)
  {
    try
    {
      String bucket = AppProperties.getBucketName();

      S3Client client = getClient();
      Builder builder = GetObjectRequest.builder().bucket(bucket).key(key);

      if (!StringUtils.isBlank(range))
      {
        builder.range(range);
      }

      GetObjectRequest request = builder.build();

      ResponseInputStream<GetObjectResponse> stream = client.getObject(request);

      return new S3ObjectWrapper(stream, key);
    }
    catch (SdkClientException e)
    {
      throw new ProgrammingErrorException(e);
    }

  }

  @Override
  public void createFolder(String key)
  {
    try
    {
      S3Client client = getClient();

      PutObjectRequest request = PutObjectRequest.builder() //
          .bucket(AppProperties.getBucketName()) //
          .key(key) //
          .contentLength(0L) //
          .build();

      client.putObject(request, RequestBody.fromBytes(new byte[0]));
    }
    catch (SdkClientException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  @Override
  public void copyFolder(String sourceKey, String sourceBucket, String destKey, String destBucket)
  {
    try
    {

      final int maxKeys = 500;

      S3Client client = getClient();
      String bucketName = AppProperties.getBucketName();

      ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder() //
          .bucket(bucketName) //
          .prefix(sourceKey) //
          .maxKeys(maxKeys) //
          .build();

      ListObjectsV2Iterable listRes = client.listObjectsV2Paginator(listObjectsRequest);
      listRes.stream().flatMap(r -> r.contents().stream()).forEach(content -> {
        String summaryKey = content.key();
        String targetKey = destKey + ( summaryKey.replaceFirst(sourceKey, "") );

        this.copyObject(summaryKey, sourceBucket, targetKey, destBucket);
      });
    }
    catch (SdkClientException e)
    {
      throw new ProgrammingErrorException("Unabled to copy folder [" + sourceBucket + "/" + sourceKey + "] to [" + destBucket + "/" + destKey + "]", e);
    }
  }

  @Override
  public void copyObject(String sourceKey, String sourceBucket, String destKey, String destBucket)
  {
    try
    {
      CopyObjectRequest copyReq = CopyObjectRequest.builder() //
          .sourceBucket(sourceBucket) //
          .sourceKey(sourceKey) //
          .destinationBucket(destBucket) //
          .destinationKey(destKey) //
          .build();

      S3Client client = getClient();
      client.copyObject(copyReq);
    }
    catch (SdkClientException e)
    {
      throw new ProgrammingErrorException("Unabled to copy [" + sourceBucket + "/" + sourceKey + "] to [" + destBucket + "/" + destKey + "]", e);
    }
  }

  @Override
  public void deleteObject(String key)
  {
    this.deleteObject(key, AppProperties.getBucketName());
  }

  @Override
  public void deleteObject(String key, String bucket)
  {
    try
    {
      S3Client client = getClient();

      DeleteObjectRequest request = DeleteObjectRequest.builder() //
          .bucket(bucket) //
          .key(key) //
          .build();
      client.deleteObject(request);
    }
    catch (SdkClientException e)
    {
      throw new ProgrammingErrorException("Unabled to delete object [" + key + "/" + bucket + "]", e);
    }
  }

  @Override
  public void deleteObjects(String key)
  {
    this.deleteObjects(key, AppProperties.getBucketName());
  }

  @Override
  public void deleteObjects(String key, String bucket)
  {
    try
    {

      final int maxKeys = 500;

      S3Client client = getClient();

      String bucketName = AppProperties.getBucketName();

      ListObjectVersionsRequest listObjectsRequest = ListObjectVersionsRequest.builder() //
          .bucket(bucketName) //
          .prefix(key) //
          .maxKeys(maxKeys) //
          .build();

      ListObjectVersionsIterable listRes = client.listObjectVersionsPaginator(listObjectsRequest);

      List<ObjectIdentifier> keys = listRes.stream() //
          .flatMap(r -> r.versions().stream()) //
          .map(version -> {
            return ObjectIdentifier.builder() //
                .versionId(version.versionId()) //
                .key(version.key()) //
                .build();
          }).collect(Collectors.toList());

      // Delete multiple objects in one request.

      if (keys.size() > 0)
      {
        Delete del = Delete.builder().objects(keys).build();

        DeleteObjectsRequest multiObjectDeleteRequest = DeleteObjectsRequest.builder() //
            .bucket(bucketName) //
            .delete(del) //
            .build();

        client.deleteObjects(multiObjectDeleteRequest);
      }
    }
    catch (SdkClientException e)
    {
      throw new ProgrammingErrorException("Unabled to delete objects for [" + key + "/" + bucket + "]", e);
    }
  }

  public boolean objectExists(String key)
  {
    try
    {
      S3Client client = getClient();
      String bucketName = AppProperties.getBucketName();

      try
      {
        final HeadObjectRequest headObjectRequest = HeadObjectRequest.builder() //
            .bucket(bucketName) //
            .key(key) //
            .build();
        client.headObject(headObjectRequest);

        return true;
      }
      catch (NoSuchKeyException noSuchKeyException)
      {
        return false;
      }
    }
    catch (SdkClientException e)
    {
      throw new ProgrammingErrorException(e);
    }

  }

  @Override
  public SiteObjectsResultSet getSiteObjects(UasComponentIF component, String folder, List<SiteObject> objects, Long pageNumber, Long pageSize)
  {
    try
    {
      final int maxKeys = 500;
      String key = component.getS3location() + folder;

      S3Client client = getClient();

      String bucketName = AppProperties.getBucketName();

      ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder() //
          .bucket(bucketName) //
          .prefix(key) //
          .maxKeys(maxKeys) //
          .build();

      AtomicLong curIndex = new AtomicLong(0);

      final long skip = pageNumber != null && pageSize != null ? ( pageNumber - 1 ) * pageSize : 0;
      final long stop = pageNumber != null && pageSize != null ? pageNumber * pageSize : 50;

      ListObjectsV2Iterable iterable = client.listObjectsV2Paginator(listObjectsRequest);
      iterable.stream().flatMap(r -> r.contents().stream()).forEach(content -> {
        long index = curIndex.getAndIncrement();

        if ( ( pageSize == null || ( index >= skip && index < stop ) ))
        {
          objects.add(SiteObject.create(component, key, content));
        }

      });

      return new SiteObjectsResultSet(curIndex.longValue(), pageNumber, pageSize, objects, folder);
    }
    catch (SdkClientException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  @Override
  public Long calculateSize(UasComponentIF component)
  {
    try
    {
      String key = component.getS3location();

      S3Client client = getClient();
      String bucketName = AppProperties.getBucketName();

      ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder() //
          .bucket(bucketName) //
          .prefix(key) //
          .maxKeys(500) //
          .build();

      ListObjectsV2Iterable iterable = client.listObjectsV2Paginator(listObjectsRequest);
      return iterable.stream() //
          .flatMap(r -> r.contents().stream()) //
          .map(content -> content.size()) //
          .reduce(Long.valueOf(0), (a, b) -> Long.valueOf((long) a + b));
    }
    catch (SdkClientException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  @Override
  public void putFile(String key, RemoteFileMetadata metadata, InputStream stream)
  {
    try
    {
      S3Client client = getClient();
      String bucketName = AppProperties.getBucketName();

      PutObjectRequest putOb = PutObjectRequest.builder() //
          .bucket(bucketName) //
          .key(key) //
          .contentLength(metadata.getContentLength()) //
          .contentType(metadata.getContentType()) //
          .build();

      client.putObject(putOb, RequestBody.fromInputStream(stream, metadata.getContentLength()));
    }
    catch (SdkClientException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  @Override
  public String uploadFile(File file, String key, StatusMonitorIF monitor)
  {
    try
    {
      S3TransferManager transferManager = createTransferManager();

      UploadFileRequest uploadFileRequest = UploadFileRequest.builder() //
          .putObjectRequest(b -> b.bucket(AppProperties.getBucketName()).key(key)) //
          .source(file) //
          .build();

      FileUpload fileUpload = transferManager.uploadFile(uploadFileRequest);

      CompletedFileUpload uploadResult = fileUpload.completionFuture().join();

      return uploadResult.response().eTag();
    }
    catch (Exception e)
    {
      if (monitor != null)
      {
        monitor.addError(RunwayException.localizeThrowable(e, Session.getCurrentLocale()));
      }

      logger.error("Exception occured while uploading [" + key + "].", e);

      throw new ProgrammingErrorException(e);
    }
  }

  @Override
  public void uploadDirectory(File directory, String key, StatusMonitorIF monitor, boolean includeSubDirectories)
  {
    this.uploadDirectory(directory, key, AppProperties.getBucketName(), monitor, includeSubDirectories);
  }

  @Override
  public void uploadDirectory(File directory, String key, String bucket, StatusMonitorIF monitor, boolean includeSubDirectories)
  {
    try
    {
      S3TransferManager transferManager = createTransferManager();

      UploadDirectoryRequest uploadRequest = UploadDirectoryRequest.builder() //
          .s3Prefix(key) //
          .bucket(AppProperties.getBucketName()) //
          .source(directory.toPath()) //
          .build();

      DirectoryUpload upload = transferManager.uploadDirectory(uploadRequest);

      CompletedDirectoryUpload result = upload.completionFuture().join();

      result.failedTransfers().forEach(fail -> {
        logger.error("Exception occured while uploading [" + key + "].", fail.toString());

        logger.warn("Object [{}] failed to transfer", fail.toString());
      });

      if (result.failedTransfers().size() > 0)
      {
        GenericException ex = new GenericException();
        ex.setUserMessage("Failed to transfer all files");
        throw ex;
      }
    }
    catch (SdkClientException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  @Override
  public void putStacItem(StacItem item)
  {
    try
    {
      S3Client client = getClient();
      String bucket = item.isPublished() ? AppProperties.getPublicBucketName() : AppProperties.getBucketName();
      String key = STAC_BUCKET + "/" + item.getId() + ".json";

      ObjectMapper mapper = new ObjectMapper();
      mapper.enable(SerializationFeature.INDENT_OUTPUT);
      byte[] bytes = mapper.writeValueAsBytes(item);

      PutObjectRequest request = PutObjectRequest.builder() //
          .bucket(bucket) //
          .key(key) //
          .contentLength((long) bytes.length) //
          .contentType("application/geo+json") //
          .build();

      client.putObject(request, RequestBody.fromBytes(bytes));
    }
    catch (SdkClientException | JsonProcessingException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  @Override
  public void removeStacItem(ProductIF product)
  {
    String bucket = product.isPublished() ? AppProperties.getPublicBucketName() : AppProperties.getBucketName();
    String key = STAC_BUCKET + "/" + product.getOid() + ".json";

    this.deleteObject(key, bucket);
  }

  @Override
  public RemoteFileObject getStacItem(ProductIF product)
  {
    String bucket = product.isPublished() ? AppProperties.getPublicBucketName() : AppProperties.getBucketName();
    String key = STAC_BUCKET + "/" + product.getOid() + ".json";

    return this.getFile(key, bucket);
  }

  @Override
  public String getUrl(String bucket, String key)
  {
    S3Client client = getClient();

    S3Utilities utilities = client.utilities();
    GetUrlRequest request = GetUrlRequest.builder().bucket(bucket).key(key).build();
    URL url = utilities.getUrl(request);

    return url.toString();
  }

  @Override
  public BBoxView getBoundingBox(Product product, DocumentIF mappable)
  {
    return new TiTillerProxy().getBoundingBox(product, mappable);
  }
}
