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

import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3;
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
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.model.AbstractWorkflowTaskIF;
import gov.geoplatform.uasdm.model.Range;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.remote.RemoteFileObject;
import gov.geoplatform.uasdm.remote.RemoteFileService;
import gov.geoplatform.uasdm.view.SiteObject;
import gov.geoplatform.uasdm.view.SiteObjectsResultSet;

public class S3RemoteFileService implements RemoteFileService
{
  private Logger logger = LoggerFactory.getLogger(S3RemoteFileService.class);

  @Override
  public void download(String key, File destination) throws IOException, FileNotFoundException
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

  @Override
  public RemoteFileObject download(String key)
  {
    AmazonS3 client = S3ClientFactory.createClient();
    String bucketName = AppProperties.getBucketName();

    GetObjectRequest request = new GetObjectRequest(bucketName, key);

    return new S3ObjectWrapper(client.getObject(request));
  }

  @Override
  public RemoteFileObject download(String key, List<Range> ranges)
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
  public void deleteObject(String key)
  {
    AmazonS3 client = S3ClientFactory.createClient();
    String bucketName = AppProperties.getBucketName();

    DeleteObjectRequest request = new DeleteObjectRequest(bucketName, key);

    client.deleteObject(request);
  }

  @Override
  public void deleteObjects(String key)
  {
    AmazonS3 client = S3ClientFactory.createClient();

    String bucketName = AppProperties.getBucketName();

    ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName).withPrefix(key);

    ObjectListing objectListing = client.listObjects(listObjectsRequest);

    while (true)
    {
      Iterator<S3ObjectSummary> objIter = objectListing.getObjectSummaries().iterator();

      while (objIter.hasNext())
      {
        String objectKey = objIter.next().getKey();

        client.deleteObject(bucketName, objectKey);
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
    VersionListing versionList = client.listVersions(new ListVersionsRequest().withBucketName(bucketName).withPrefix(key));
    while (true)
    {
      Iterator<S3VersionSummary> versionIter = versionList.getVersionSummaries().iterator();
      while (versionIter.hasNext())
      {
        S3VersionSummary vs = versionIter.next();
        client.deleteVersion(bucketName, vs.getKey(), vs.getVersionId());
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

    DeleteObjectsRequest multiObjectDeleteRequest = new DeleteObjectsRequest(bucketName).withKeys(key).withQuiet(false);

    client.deleteObjects(multiObjectDeleteRequest);
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
  public SiteObjectsResultSet getSiteObjects(UasComponentIF component, String folder, List<SiteObject> objects, Integer pageNumber, Integer pageSize)
  {
    final int maxKeys = 500;
    String key = component.getS3location() + folder;

    AmazonS3 client = S3ClientFactory.createClient();

    String bucketName = AppProperties.getBucketName();

    ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName).withPrefix(key);
    listObjectsRequest.setMaxKeys(maxKeys);

    int curIndex = 0;

    int pageIndexStart = 0;
    int pageIndexStop = 0;
    if (pageNumber != null && pageSize != null)
    {
      pageIndexStart = ( pageNumber - 1 ) * pageSize;
      pageIndexStop = pageNumber * pageSize;
    }
    int awsPageNum = 1;

    ObjectListing objectListing = client.listObjects(listObjectsRequest);

    while (true)
    {
      List<S3ObjectSummary> list = objectListing.getObjectSummaries();
      Iterator<S3ObjectSummary> objIter = list.iterator();

      if (pageNumber == null || pageSize == null || ( pageIndexStart >= maxKeys * ( awsPageNum - 1 ) && pageIndexStop <= maxKeys * awsPageNum ))
      {
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
      }

      // If the bucket contains many objects, the listObjects() call
      // might not return all of the objects in the first listing. Check to
      // see whether the listing was truncated.
      if (objectListing.isTruncated())
      {
        objectListing = client.listNextBatchOfObjects(objectListing);
        awsPageNum++;
      }
      else
      {
        break;
      }
    }

    return new SiteObjectsResultSet(curIndex, pageNumber, pageSize, objects, folder);
  }

  @Override
  public void uploadFile(File file, String key, AbstractWorkflowTaskIF task)
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

          if (task != null)
          {
            task.lock();
            task.setMessage(myUpload.getDescription());
            task.apply();
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
      if (task != null)
      {
        task.createAction(e.getMessage(), "error");
      }

      logger.error("Exception occured while uploading [" + key + "].", e);
    }
  }

}
