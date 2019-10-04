package gov.geoplatform.uasdm.bus;

import java.util.Iterator;

import org.json.JSONObject;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.S3VersionSummary;
import com.amazonaws.services.s3.model.VersionListing;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import gov.geoplatform.uasdm.AppProperties;

public class Document extends DocumentBase
{
  private static final long serialVersionUID = -877956259;

  public Document()
  {
    super();
  }

  @Override
  public void delete()
  {
    super.delete();

    if (!this.getS3location().trim().equals(""))
    {
      this.deleteS3File(this.getS3location());
    }
  }

  protected void deleteS3File(String key)
  {
    BasicAWSCredentials awsCreds = new BasicAWSCredentials(AppProperties.getS3AccessKey(), AppProperties.getS3SecretKey());
    AmazonS3 client = new AmazonS3Client(new StaticCredentialsProvider(awsCreds));

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
      // objects
      // and delete them.
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
  protected String buildKey()
  {
    return this.getS3location();
  }

  public static Document createIfNotExist(UasComponent uasComponent, String key, String name)
  {
    Document document = Document.find(key);

    if (document == null)
    {
      document = new Document();
      document.setS3location(key);
    }
    else
    {
      document.appLock();
    }

    document.setComponent(uasComponent);
    document.setName(name);
    document.apply();

    return document;
  }

  private static Document find(String key)
  {
    DocumentQuery query = new DocumentQuery(new QueryFactory());
    query.WHERE(query.getS3location().EQ(key));

    try (OIterator<? extends Document> it = query.getIterator())
    {
      if (it.hasNext())
      {
        return it.next();
      }
    }

    return null;
  }

  public void addGeneratedProduct(Product product)
  {
    DocumentGeneratedProduct pd = this.getDocumentGeneratedProduct(product);

    if (pd == null)
    {
      this.addGeneratedProducts(product).apply();
    }
  }

  public DocumentGeneratedProduct getDocumentGeneratedProduct(Product product)
  {
    DocumentGeneratedProductQuery query = new DocumentGeneratedProductQuery(new QueryFactory());
    query.WHERE(query.getParent().EQ(this));
    query.AND(query.getChild().EQ(product));

    try (OIterator<? extends DocumentGeneratedProduct> iterator = query.getIterator())
    {
      if (iterator.hasNext())
      {
        return iterator.next();
      }
    }

    return null;
  }

  public JSONObject toJSON()
  {
    JSONObject object = new JSONObject();
    object.put("id", this.getOid());
    object.put("key", this.getS3location());
    object.put("name", this.getName());

    return object;
  }

}
