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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.S3VersionSummary;
import com.amazonaws.services.s3.model.VersionListing;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.runwaysdk.RunwayException;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.session.Session;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.cog.TiTillerProxy;
import gov.geoplatform.uasdm.cog.TiTillerProxy.BBoxView;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.Range;
import gov.geoplatform.uasdm.model.StacItem;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.processing.StatusMonitorIF;
import gov.geoplatform.uasdm.remote.RemoteFileMetadata;
import gov.geoplatform.uasdm.remote.RemoteFileObject;
import gov.geoplatform.uasdm.remote.RemoteFileService;
import gov.geoplatform.uasdm.view.SiteObject;
import gov.geoplatform.uasdm.view.SiteObjectsResultSet;

public class S3RemoteFileService implements RemoteFileService
{
  public static final String STAC_BUCKET = "-stac-";

  private Logger logger = LoggerFactory.getLogger(S3RemoteFileService.class);

  @Override
  public void download(String key, File destination) throws IOException, FileNotFoundException
  {
    try
    {

      AmazonS3 client = S3ClientFactory.createClient();

      String bucketName = AppProperties.getBucketName();

      GetObjectRequest request = new GetObjectRequest(bucketName, key);

      S3Object s3Obj = client.getObject(request);

      try (final S3ObjectInputStream istream = s3Obj.getObjectContent())
      {
        try (OutputStream fos = new FileOutputStream(destination))
        {
          IOUtils.copy(istream, fos);
        }
      }
    }
    catch (AmazonS3Exception e)
    {
      this.logger.error("Unable to find s3 object [" + key + "]", e);

      throw e;
    }
  }

  @Override
  public RemoteFileObject proxy(String url)
  {
    AmazonS3 client = S3ClientFactory.createClient();
    AmazonS3URI uri = new AmazonS3URI(url);

    GetObjectRequest request = new GetObjectRequest(uri.getBucket(), uri.getKey());

    S3Object object = client.getObject(request);

    return new S3ObjectWrapper(object);
  }

  @Override
  public RemoteFileObject download(String key)
  {
    AmazonS3 client = S3ClientFactory.createClient();
    String bucketName = AppProperties.getBucketName();

    GetObjectRequest request = new GetObjectRequest(bucketName, key);

    S3Object object = client.getObject(request);

    return new S3ObjectWrapper(object);
  }

  @Override
  public RemoteFileObject download(String key, List<Range> ranges)
  {
    try
    {

      AmazonS3 client = S3ClientFactory.createClient();
      String bucketName = AppProperties.getBucketName();

      GetObjectRequest request = new GetObjectRequest(bucketName, key);

      if (ranges.size() > 0)
      {
        final Range range = ranges.get(0);

        if (range.getEnd() != null)
        {
          request.setRange(range.getStart(), range.getEnd());
        }
        else
        {
          request.setRange(range.getStart());
        }
      }

      return new S3ObjectWrapper(client.getObject(request));
    }
    catch (AmazonS3Exception e)
    {
      this.logger.error("Unable to find s3 object [" + key + "]", e);

      throw e;
    }
  }

  @Override
  public void createFolder(String key)
  {
    AmazonS3 client = S3ClientFactory.createClient();

    // create meta-data for your folder and set content-length to 0
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength(0);

    // create empty content
    InputStream emptyContent = new ByteArrayInputStream(new byte[0]);

    PutObjectRequest putObjectRequest = new PutObjectRequest(AppProperties.getBucketName(), key, emptyContent, metadata);

    // send request to S3 to create folder
    client.putObject(putObjectRequest);
  }

  @Override
  public void copyObject(String sourceKey, String sourceBucket, String destKey, String destBucket)
  {
    AmazonS3 client = S3ClientFactory.createClient();

    CopyObjectRequest request = new CopyObjectRequest(sourceBucket, sourceKey, destBucket, destKey);

    client.copyObject(request);
  }

  @Override
  public void deleteObject(String key)
  {
    this.deleteObject(key, AppProperties.getBucketName());
  }

  @Override
  public void deleteObject(String key, String bucket)
  {
    AmazonS3 client = S3ClientFactory.createClient();

    DeleteObjectRequest request = new DeleteObjectRequest(bucket, key);

    client.deleteObject(request);
  }

  @Override
  public void deleteObjects(String key)
  {
    this.deleteObjects(key, AppProperties.getBucketName());
  }

  @Override
  public void deleteObjects(String key, String bucket)
  {
    AmazonS3 client = S3ClientFactory.createClient();

    ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucket).withPrefix(key);

    ObjectListing objectListing = client.listObjects(listObjectsRequest);

    while (true)
    {
      Iterator<S3ObjectSummary> objIter = objectListing.getObjectSummaries().iterator();

      while (objIter.hasNext())
      {
        String objectKey = objIter.next().getKey();

        client.deleteObject(bucket, objectKey);
      }

      // If the bucket contains many objects, the listObjects() call
      // might not return all of the objects in the first listing. Check to
      // see whether the listing was truncated. If so, retrieve the next page of
      // objects and delete them.
      if (objectListing.isTruncated())
      {
        objectListing = client.listNextBatchOfObjects(objectListing);
      }
      else
      {
        break;
      }
    }

    // Delete all object versions (required for versioned buckets).
    VersionListing versionList = client.listVersions(new ListVersionsRequest().withBucketName(bucket).withPrefix(key));
    while (true)
    {
      Iterator<S3VersionSummary> versionIter = versionList.getVersionSummaries().iterator();
      while (versionIter.hasNext())
      {
        S3VersionSummary vs = versionIter.next();
        client.deleteVersion(bucket, vs.getKey(), vs.getVersionId());
      }

      if (versionList.isTruncated())
      {
        versionList = client.listNextBatchOfVersions(versionList);
      }
      else
      {
        break;
      }
    }

    DeleteObjectsRequest multiObjectDeleteRequest = new DeleteObjectsRequest(bucket).withKeys(key).withQuiet(false);

    client.deleteObjects(multiObjectDeleteRequest);
  }

  public boolean objectExists(String key)
  {
    AmazonS3 client = S3ClientFactory.createClient();

    String bucketName = AppProperties.getBucketName();

    return client.doesObjectExist(bucketName, key);
  }

  @Override
  public int getItemCount(String key)
  {
    int count = 0;

    AmazonS3 client = S3ClientFactory.createClient();

    String bucketName = AppProperties.getBucketName();

    ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName).withPrefix(key);

    ObjectListing objectListing = client.listObjects(listObjectsRequest);

    while (true)
    {
      Iterator<S3ObjectSummary> objIter = objectListing.getObjectSummaries().iterator();

      while (objIter.hasNext())
      {
        S3ObjectSummary summary = objIter.next();

        String summaryKey = summary.getKey();

        if (!summaryKey.endsWith("/") && !summaryKey.contains("thumbnails/"))
        {
          count++;
        }
      }

      // If the bucket contains many objects, the listObjects() call
      // might not return all of the objects in the first listing. Check to
      // see whether the listing was truncated.
      if (objectListing.isTruncated())
      {
        objectListing = client.listNextBatchOfObjects(objectListing);
      }
      else
      {
        break;
      }
    }

    return count;
  }

  @Override
  public SiteObjectsResultSet getSiteObjects(UasComponentIF component, String folder, List<SiteObject> objects, Long pageNumber, Long pageSize)
  {
    final int maxKeys = 500;
    String key = component.getS3location() + folder;

    try
    {
      AmazonS3 client = S3ClientFactory.createClient();

      String bucketName = AppProperties.getBucketName();

      ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName).withPrefix(key);
      listObjectsRequest.setMaxKeys(maxKeys);

      int curIndex = 0;

      long pageIndexStart = 0;
      long pageIndexStop = 0;

      if (pageNumber != null && pageSize != null)
      {
        pageIndexStart = ( pageNumber - 1 ) * pageSize;
        pageIndexStop = pageNumber * pageSize;
      }

      ObjectListing objectListing = client.listObjects(listObjectsRequest);

      while (true)
      {
        List<S3ObjectSummary> list = objectListing.getObjectSummaries();
        Iterator<S3ObjectSummary> objIter = list.iterator();

        while (objIter.hasNext())
        {
          S3ObjectSummary summary = objIter.next();

          String summaryKey = summary.getKey();

          if (!summaryKey.endsWith("/") && !summaryKey.contains("thumbnails/"))
          {
            if ( ( pageSize == null || ( curIndex >= pageIndexStart && curIndex < pageIndexStop ) ))
            {
              objects.add(SiteObject.create(component, key, summary));
            }

            curIndex++;
          }
        }

        // If the bucket contains many objects, the listObjects() call
        // might not return all of the objects in the first listing. Check to
        // see whether the listing was truncated.
        if (objectListing.isTruncated())
        {
          objectListing = client.listNextBatchOfObjects(objectListing);
        }
        else
        {
          break;
        }
      }

      return new SiteObjectsResultSet(new Long(curIndex), pageNumber, pageSize, objects, folder);
    }
    catch (AmazonS3Exception e)
    {
      this.logger.error("Unable to find s3 object [" + key + "]", e);

      throw e;
    }
  }

  @Override
  public Long calculateSize(UasComponentIF component)
  {
    String key = component.getS3location();

    try
    {
      AmazonS3 client = S3ClientFactory.createClient();

      String bucketName = AppProperties.getBucketName();

      ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName).withPrefix(key);

      long size = 0;

      ObjectListing objectListing = client.listObjects(listObjectsRequest);

      while (true)
      {
        List<S3ObjectSummary> list = objectListing.getObjectSummaries();
        Iterator<S3ObjectSummary> objIter = list.iterator();

        while (objIter.hasNext())
        {
          S3ObjectSummary summary = objIter.next();

          size += summary.getSize();
        }

        // If the bucket contains many objects, the listObjects() call
        // might not return all of the objects in the first listing. Check to
        // see whether the listing was truncated.
        if (objectListing.isTruncated())
        {
          objectListing = client.listNextBatchOfObjects(objectListing);
        }
        else
        {
          break;
        }
      }

      return size;
    }
    catch (AmazonS3Exception e)
    {
      this.logger.error("Unable to find s3 object [" + key + "]", e);

      throw e;
    }
  }

  @Override
  public void putFile(String key, RemoteFileMetadata metadata, InputStream stream)
  {
    try
    {
      String bucketName = AppProperties.getBucketName();

      ObjectMetadata oMetadata = new ObjectMetadata();
      oMetadata.setContentType(metadata.getContentType());
      oMetadata.setContentLength(metadata.getContentLength());

      PutObjectRequest request = new PutObjectRequest(bucketName, key, stream, oMetadata);

      AmazonS3 client = S3ClientFactory.createClient();
      client.putObject(request);
    }
    catch (AmazonServiceException e)
    {
      throw new ProgrammingErrorException(e);
    }
    catch (SdkClientException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  @Override
  public void uploadFile(File file, String key, StatusMonitorIF monitor)
  {
    try
    {
      TransferManager tx = S3ClientFactory.createTransferManager();

      try
      {
        Upload myUpload = tx.upload(AppProperties.getBucketName(), key, file);

        if (myUpload.isDone() == false)
        {
          logger.info("Source: " + file.getAbsolutePath());
          logger.info("Destination: " + myUpload.getDescription());

          if (monitor != null)
          {
            monitor.setMessage(myUpload.getDescription());
          }
        }

        myUpload.addProgressListener(new ProgressListener()
        {
          int count = 0;

          @Override
          public void progressChanged(ProgressEvent progressEvent)
          {
            if (count % 2000 == 0)
            {
              long total = myUpload.getProgress().getTotalBytesToTransfer();
              long current = myUpload.getProgress().getBytesTransferred();

              logger.info(current + "/" + total + "-" + ( (int) ( (double) current / total * 100 ) ) + "%");

              count = 0;
            }

            count++;
          }
        });

        myUpload.waitForCompletion();
      }
      finally
      {
        tx.shutdownNow();
      }
    }
    catch (Exception e)
    {
      if (monitor != null)
      {
        monitor.addError(RunwayException.localizeThrowable(e, Session.getCurrentLocale()));
      }

      logger.error("Exception occured while uploading [" + key + "].", e);
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
      TransferManager tx = S3ClientFactory.createTransferManager();

      try
      {
        MultipleFileUpload myUpload = tx.uploadDirectory(bucket, key, directory, includeSubDirectories);

        if (myUpload.isDone() == false)
        {
          logger.info("Source: " + directory.getAbsolutePath());
          logger.info("Destination: " + myUpload.getDescription());

          if (monitor != null)
          {
            monitor.setMessage(myUpload.getDescription());
          }
        }

        myUpload.addProgressListener(new ProgressListener()
        {
          int count = 0;

          @Override
          public void progressChanged(ProgressEvent progressEvent)
          {
            if (count % 2000 == 0)
            {
              long total = myUpload.getProgress().getTotalBytesToTransfer();
              long current = myUpload.getProgress().getBytesTransferred();

              logger.info(current + "/" + total + "-" + ( (int) ( (double) current / total * 100 ) ) + "%");

              count = 0;
            }

            count++;
          }
        });

        myUpload.waitForCompletion();
      }
      finally
      {
        tx.shutdownNow();
      }
    }
    catch (Exception e)
    {
      if (monitor != null)
      {
        monitor.addError(RunwayException.localizeThrowable(e, Session.getCurrentLocale()));
      }

      logger.error("Exception occured while uploading [" + key + "].", e);
    }
  }

  @Override
  public void putStacItem(StacItem item)
  {
    try
    {
      String bucket = item.isPublished() ? AppProperties.getPublicBucketName() : AppProperties.getBucketName();
      String key = STAC_BUCKET + "/" + item.getId() + ".json";

      ObjectMapper mapper = new ObjectMapper();
      mapper.enable(SerializationFeature.INDENT_OUTPUT);
      byte[] bytes = mapper.writeValueAsBytes(item);

      ObjectMetadata oMetadata = new ObjectMetadata();
      oMetadata.setContentType("application/geo+json");
      oMetadata.setContentLength(bytes.length);

      try (ByteArrayInputStream istream = new ByteArrayInputStream(bytes))
      {
        PutObjectRequest request = new PutObjectRequest(bucket, key, istream, oMetadata);
        AmazonS3 client = S3ClientFactory.createClient();
        client.putObject(request);
      }
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
    catch (AmazonServiceException e)
    {
      throw new ProgrammingErrorException(e);
    }
    catch (SdkClientException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  @Override
  public void removeStacItem(ProductIF product)
  {
    String bucket = product.isPublished() ? AppProperties.getPublicBucketName() : AppProperties.getBucketName();
    String key = STAC_BUCKET + "/" + product.getOid() + ".json";

    AmazonS3 client = S3ClientFactory.createClient();

    DeleteObjectRequest request = new DeleteObjectRequest(bucket, key);

    client.deleteObject(request);
  }

  @Override
  public RemoteFileObject getStacItem(ProductIF product)
  {
    String bucket = product.isPublished() ? AppProperties.getPublicBucketName() : AppProperties.getBucketName();
    String key = STAC_BUCKET + "/" + product.getOid() + ".json";

    try
    {
      AmazonS3 client = S3ClientFactory.createClient();

      GetObjectRequest request = new GetObjectRequest(bucket, key);

      return new S3ObjectWrapper(client.getObject(request));
    }
    catch (AmazonS3Exception e)
    {
      this.logger.error("Unable to find s3 object [" + key + "]", e);

      throw e;
    }
  }

  @Override
  public BBoxView getBoundingBox(Product product, DocumentIF mappable)
  {
    return new TiTillerProxy().getBoundingBox(product, mappable);
  }
}
