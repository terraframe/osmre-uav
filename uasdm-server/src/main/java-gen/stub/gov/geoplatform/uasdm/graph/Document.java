package gov.geoplatform.uasdm.graph;

import java.util.Iterator;
import java.util.List;

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
import com.runwaysdk.business.graph.VertexQuery;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.EdgeType;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.UasComponentIF;

public class Document extends DocumentBase implements DocumentIF
{
  private static final long serialVersionUID = -1445705168;

  public Document()
  {
    super();
  }

  @Transaction
  public void apply(UasComponentIF component)
  {
    final boolean isNew = this.isNew();

    this.apply();

    if (isNew)
    {
      this.addParent((UasComponent) component, EdgeType.COMPONENT_HAS_DOCUMENT);
    }
  }

  @Override
  public void delete()
  {
    this.delete(true);
  }

  @Transaction
  public void delete(boolean removeFromS3)
  {
    super.delete();

    if (removeFromS3 && !this.getS3location().trim().equals(""))
    {
      this.deleteS3File(this.getS3location());
    }
  }

  public UasComponent getComponent()
  {
    final MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.COMPONENT_HAS_DOCUMENT);
    final List<UasComponent> parents = this.getParents(mdEdge, UasComponent.class);

    return parents.get(0);
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

  public static Document createIfNotExist(UasComponentIF uasComponent, String key, String name)
  {
    Document document = Document.find(key);

    if (document == null)
    {
      document = new Document();
      document.setS3location(key);
    }
    else
    {
    }

    document.setName(name);
    document.apply(uasComponent);

    return document;
  }

  private static Document find(String key)
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Document.CLASS);

    String statement = "SELECT FROM " + mdVertex.getDBClassName() + " WHERE s3location = :s3location";

    final VertexQuery<Document> query = new VertexQuery<Document>(statement);
    query.setParameter("s3location", key);

    return query.getSingleResult();
  }

  public void addGeneratedProduct(ProductIF product)
  {
    final MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.DOCUMENT_GENERATED_PRODUCT);

    this.addChild((Product) product, mdEdge);
  }

  public JSONObject toJSON()
  {
    JSONObject object = new JSONObject();
    object.put("id", this.getOid());
    object.put("key", this.getS3location());
    object.put("name", this.getName());
    object.put("component", this.getComponent().getOid());

    return object;
  }

}
