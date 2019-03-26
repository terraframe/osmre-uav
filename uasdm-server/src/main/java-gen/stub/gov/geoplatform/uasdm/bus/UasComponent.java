package gov.geoplatform.uasdm.bus;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.geotools.geojson.geom.GeometryJSON;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.S3VersionSummary;
import com.amazonaws.services.s3.model.VersionListing;
import com.runwaysdk.dataaccess.MdClassDAOIF;
import com.runwaysdk.dataaccess.metadata.MdClassDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Session;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.service.SolrService;
import gov.geoplatform.uasdm.view.AttributeType;
import gov.geoplatform.uasdm.view.SiteObject;
import net.geoprism.JSONStringImpl;

public abstract class UasComponent extends UasComponentBase
{
  private static final long serialVersionUID = -2027002868;

  public UasComponent()
  {
    super();
  }

  /**
   * For the POC, each type has only one child type. Use polymorphism to return
   * the correct type.
   * 
   * @return a new {@link UasComponent} of the correct type.
   */
  public abstract UasComponent createChild();

  /**
   * @return The name of the solr field for the components id.
   */
  public abstract String getSolrIdField();

  /**
   * @return The name of the solr field for the components name.
   */
  public abstract String getSolrNameField();

  /**
   * Creates the object and builds the relationship with the parent.
   * 
   * Creates directory in S3.
   * 
   * @param parent
   */
  @Transaction
  public void applyWithParent(UasComponent parent)
  {

    /*
     * https://docs.aws.amazon.com/AmazonS3/latest/dev/UsingMetadata.html
     * 
     * Characters That Might Require Special Handling
     */
    boolean isNameModified = this.isModified(UasComponent.NAME);

    if (isNameModified)
    {
      String name = this.getName();

      if (!isValidName(name))
      {
        throw new InvalidUasComponentNameException("The name field has an invalid character");
      }
    }

    boolean isNew = this.isNew();

    if (isNew)
    {
      if (parent != null)
      {
        boolean isDuplicate = isDuplicateName(parent.getOid(), this.getOid(), this.getName());

        if (isDuplicate)
        {
          DuplicateComponentException e = new DuplicateComponentException();
          e.setParentName(parent.getName());
          e.setChildComponentLabel(this.getMdClass().getDisplayLabel(Session.getCurrentLocale()));
          e.setChildName(this.getName());

          throw e;
        }
      }

      String key;

      if (parent != null)
      {
        key = this.buildS3Key(parent);
      }
      else
      {
        key = this.buildS3Key(null);
      }

      this.setS3location(key);

      this.createS3Folder(key);
    }

    this.apply();

    if (parent != null)
    {
      this.addComponent(parent).apply();
    }

    if (!isNew && isNameModified)
    {
      SolrService.updateName(this);
    }
    else if (isNew)
    {
      SolrService.createDocument(this.getAncestors(), this);
    }
  }

  public void delete()
  {
    super.delete();

    if (!this.getS3location().trim().equals(""))
    {
      this.deleteS3Folder(this.getS3location());
    }

    SolrService.deleteDocuments(this);
  }

  /**
   * Builds a key for S3 that conforms to the directory structure requirements.
   * If the parent is null, then
   * 
   * @param uasComponent
   *          null if no parent
   * @return a key for S3 that conforms to the directory structure requirements.
   */
  public String buildS3Key(UasComponent parent)
  {
    String key = new String();

    if (parent != null)
    {
      key += parent.getS3location();
    }

    key += this.getName() + "/";

    return key;
  }

  protected void createS3Folder(String key)
  {
    AmazonS3 client = new AmazonS3Client(new ClasspathPropertiesFileCredentialsProvider());

    // create meta-data for your folder and set content-length to 0
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength(0);

    // create empty content
    InputStream emptyContent = new ByteArrayInputStream(new byte[0]);

    PutObjectRequest putObjectRequest = new PutObjectRequest(AppProperties.getBucketName(), key, emptyContent, metadata);

    // send request to S3 to create folder
    client.putObject(putObjectRequest);
  }

  protected void deleteS3Folder(String key)
  {
    AmazonS3 client = new AmazonS3Client(new ClasspathPropertiesFileCredentialsProvider());

    String bucketName = AppProperties.getBucketName();

    ObjectListing objectListing = client.listObjects(bucketName, key);

    while (true)
    {
      Iterator<S3ObjectSummary> objIter = objectListing.getObjectSummaries().iterator();

      while (objIter.hasNext())
      {
        client.deleteObject(bucketName, objIter.next().getKey());
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
    VersionListing versionList = client.listVersions(new ListVersionsRequest().withBucketName(bucketName).withKeyMarker(key));
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

  public static boolean isValidName(String name)
  {
    if (name.contains(" ") || name.contains("<") || name.contains(">") || name.contains("-") || name.contains("+") || name.contains("=") || name.contains("!") || name.contains("@") || name.contains("#") || name.contains("$") || name.contains("%") || name.contains("^") || name.contains("&") || name.contains("*") || name.contains("?") || name.contains(";") || name.contains(":") || name.contains(",") || name.contains("^") || name.contains("{") || name.contains("}") || name.contains("]") || name.contains("[") || name.contains("`") || name.contains("~") || name.contains("|") || name.contains("/") || name.contains("\\"))
    {
      return false;
    }

    return true;
  }

  public static boolean isDuplicateName(String parentId, String oid, String name)
  {
    QueryFactory qf = new QueryFactory();
    UasComponentQuery childQ = new UasComponentQuery(qf);

    UasComponentQuery parentQ = new UasComponentQuery(qf);
    parentQ.WHERE(parentQ.getOid().EQ(parentId));

    childQ.WHERE(childQ.getName().EQ(name));
    childQ.AND(childQ.component(parentQ));

    if (oid != null)
    {
      childQ.AND(childQ.getOid().NE(oid));
    }

    OIterator<? extends UasComponent> i = childQ.getIterator();

    try
    {
      if (i.hasNext())
      {
        return true;
      }
    }
    finally
    {
      i.close();
    }

    return false;
  }

  public static void validateName(String parentId, String name)
  {
    if (!isValidName(name))
    {
      throw new InvalidUasComponentNameException("The name field has an invalid character");
    }
    else if (isDuplicateName(parentId, null, name))
    {
      UasComponent parent = UasComponent.get(parentId);
      MdClassDAOIF mdClass = MdClassDAO.getMdClassDAO(Collection.CLASS);

      DuplicateComponentException e = new DuplicateComponentException();
      e.setParentName(parent.getName());
      e.setChildComponentLabel(mdClass.getDisplayLabel(Session.getCurrentLocale()));
      e.setChildName(name);

      throw e;
    }
  }

  public List<SiteObject> getSiteObjects(String folder)
  {
    return new LinkedList<SiteObject>();
  }

  protected void getSiteObjects(String folder, List<SiteObject> objects)
  {
    String key = this.getS3location() + folder;

    AmazonS3 client = new AmazonS3Client(new ClasspathPropertiesFileCredentialsProvider());

    String bucketName = AppProperties.getBucketName();

    ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName).withPrefix(key);

    ObjectListing objectListing = client.listObjects(listObjectsRequest);

    while (true)
    {
      Iterator<S3ObjectSummary> objIter = objectListing.getObjectSummaries().iterator();

      while (objIter.hasNext())
      {
        S3ObjectSummary summary = objIter.next();

        if (!summary.getKey().endsWith("/"))
        {
          objects.add(SiteObject.create(this, key, summary));
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
  }

  public S3Object download(String key)
  {
    AmazonS3 client = new AmazonS3Client(new ClasspathPropertiesFileCredentialsProvider());
    String bucketName = AppProperties.getBucketName();

    GetObjectRequest request = new GetObjectRequest(bucketName, key);

    return client.getObject(request);
  }

  public List<UasComponent> getAncestors()
  {
    List<UasComponent> ancestors = new LinkedList<UasComponent>();

    List<UasComponent> parents = new LinkedList<>();

    OIterator<? extends UasComponent> it = this.getAllComponent();

    try
    {
      parents.addAll(it.getAll());
    }
    finally
    {
      it.close();
    }

    ancestors.addAll(parents);

    for (UasComponent parent : parents)
    {
      ancestors.addAll(parent.getAncestors());
    }

    return ancestors;
  }

  public List<AttributeType> attributes()
  {
    List<AttributeType> list = new LinkedList<AttributeType>();
    list.add(AttributeType.create(this.getMdAttributeDAO(UasComponent.NAME)));
    list.add(AttributeType.create(this.getMdAttributeDAO(UasComponent.FOLDERNAME)));

    return list;
  }

  public void writeFeature(JSONWriter writer) throws IOException
  {
    GeometryJSON gjson = new GeometryJSON();

    StringWriter geomWriter = new StringWriter();

    gjson.write(this.getGeoPoint(), geomWriter);

    writer.object();

    writer.key("type");
    writer.value("Feature");

    writer.key("properties");
    writer.value(this.getProperties());

    writer.key("id");
    writer.value(this.getOid());

    writer.key("geometry");
    writer.value(new JSONStringImpl(geomWriter.toString()));

    writer.endObject();
  }

  public JSONObject getProperties()
  {
    JSONObject properties = new JSONObject();
    properties.put("oid", this.getOid());
    properties.put("name", this.getName());

    return properties;
  }

  public static JSONObject features() throws IOException
  {
    StringWriter sWriter = new StringWriter();
    JSONWriter writer = new JSONWriter(sWriter);

    SiteQuery query = new SiteQuery(new QueryFactory());
    query.WHERE(query.getGeoPoint().NE((String) null));

    OIterator<? extends Site> it = query.getIterator();

    try
    {
      writer.object();

      writer.key("type");
      writer.value("FeatureCollection");
      writer.key("features");
      writer.array();

      while (it.hasNext())
      {
        Site site = it.next();

        if (site.getGeoPoint() != null)
        {
          site.writeFeature(writer);
        }
      }

      writer.endArray();

      writer.key("totalFeatures");
      writer.value(query.getCount());

      writer.key("crs");
      writer.value(new JSONObject("{\"type\":\"name\",\"properties\":{\"name\":\"urn:ogc:def:crs:EPSG::4326\"}}"));

      writer.endObject();
    }
    finally
    {
      it.close();
    }

    return new JSONObject(sWriter.toString());
  }

}
