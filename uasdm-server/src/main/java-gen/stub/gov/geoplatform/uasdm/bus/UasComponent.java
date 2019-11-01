package gov.geoplatform.uasdm.bus;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.geotools.geojson.geom.GeometryJSON;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
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
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.MdClassDAOIF;
import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.dataaccess.metadata.MdClassDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.metadata.MdBusiness;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.service.SolrService;
import gov.geoplatform.uasdm.view.AdminCondition;
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
   * There will be a default child type for each node.
   * 
   * @return a new {@link UasComponent} of the correct type.
   */
  public abstract UasComponent createDefaultChild();

  /**
   * Create the child of the given type.
   * 
   * @param return
   *          the child of the given type. It assumes the type is valid. It is
   *          the type name of the Runway {@link MdBusiness}.
   * 
   * @return a new {@link UasComponent} of the correct type.
   */
  public UasComponent createChild(String typeName)
  {
    return this.createDefaultChild();
  }

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
    boolean isNew = this.isNew();

    if (isNew)
    {
      this.setFolderName(UUID.randomUUID().toString().replaceAll("-", ""));
    }

    if (this.isModified(UasComponent.FOLDERNAME))
    {
      String name = this.getFolderName();

      if (!isValidName(name))
      {
        MdBusinessDAOIF mdBusiness = MdBusinessDAO.getMdBusinessDAO(UasComponent.CLASS);
        MdAttributeConcreteDAOIF mdAttribute = mdBusiness.definesAttribute(UasComponent.FOLDERNAME);

        InvalidUasComponentNameException ex = new InvalidUasComponentNameException("The folder name field has an invalid character");
        ex.setAttributeName(mdAttribute.getDisplayLabel(Session.getCurrentLocale()));
        throw ex;
      }
    }

    if (isNew)
    {
      if (parent != null)
      {
        boolean isDuplicate = isDuplicateFolderName(parent.getOid(), this.getOid(), this.getFolderName());

        if (isDuplicate)
        {
          DuplicateComponentException e = new DuplicateComponentException();
          e.setParentName(parent.getName());
          e.setChildComponentLabel(this.getMdClass().getDisplayLabel(Session.getCurrentLocale()));
          e.setChildName(this.getFolderName());

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

    if (isNew)
    {
      SolrService.createDocument(this.getAncestors(), this);
    }
  }

  @Override
  public void apply()
  {
    boolean isNameModified = this.isModified(UasComponent.NAME);
    boolean needsUpdate = this.needsUpdate();
    boolean isNew = this.isNew();

    super.apply();

    if (!isNew)
    {
      if (isNameModified)
      {
        SolrService.updateName(this);
      }

      if (needsUpdate)
      {
        SolrService.updateComponent(this);
      }
    }
  }

  protected boolean needsUpdate()
  {
    return this.isModified(UasComponent.DESCRIPTION);
  }

  @Transaction
  public void delete()
  {
    List<AbstractWorkflowTask> tasks = this.getTasks();

    for (AbstractWorkflowTask task : tasks)
    {
      task.delete();
    }

    // Delete all of the products
    List<Product> products = this.getProducts();

    for (Product product : products)
    {
      product.delete();
    }

    // Delete all of the documents
    List<Document> documents = this.getDocuments();

    for (Document document : documents)
    {
      document.delete();
    }

    super.delete();

    if (!this.getS3location().trim().equals(""))
    {
      this.deleteS3Folder(this.getS3location(), null);
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

    key += this.getFolderName() + "/";

    return key;
  }

  protected void createS3Folder(String key)
  {
    BasicAWSCredentials awsCreds = new BasicAWSCredentials(AppProperties.getS3AccessKey(), AppProperties.getS3SecretKey());
    AmazonS3 client = new AmazonS3Client(new StaticCredentialsProvider(awsCreds));

    // create meta-data for your folder and set content-length to 0
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength(0);

    // create empty content
    InputStream emptyContent = new ByteArrayInputStream(new byte[0]);

    PutObjectRequest putObjectRequest = new PutObjectRequest(AppProperties.getBucketName(), key, emptyContent, metadata);

    // send request to S3 to create folder
    client.putObject(putObjectRequest);
  }

  protected void deleteS3Folder(String key, String folderName)
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

        this.deleteS3Object(objectKey);
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

  protected void deleteS3Object(String objectKey)
  {

  }

  public static boolean isValidName(String name)
  {
    if (name.contains(" ") || name.contains("<") || name.contains(">") || name.contains("-") || name.contains("+") || name.contains("=") || name.contains("!") || name.contains("@") || name.contains("#") || name.contains("$") || name.contains("%") || name.contains("^") || name.contains("&") || name.contains("*") || name.contains("?") || name.contains(";") || name.contains(":") || name.contains(",") || name.contains("^") || name.contains("{") || name.contains("}") || name.contains("]") || name.contains("[") || name.contains("`") || name.contains("~") || name.contains("|") || name.contains("/") || name.contains("\\"))
    {
      return false;
    }

    return true;
  }

  public static boolean isDuplicateFolderName(String parentId, String oid, String folderName)
  {
    QueryFactory qf = new QueryFactory();
    UasComponentQuery childQ = new UasComponentQuery(qf);

    UasComponentQuery parentQ = new UasComponentQuery(qf);
    parentQ.WHERE(parentQ.getOid().EQ(parentId));

    childQ.WHERE(childQ.getFolderName().EQ(folderName));
    childQ.AND(childQ.component(parentQ));

    if (oid != null)
    {
      childQ.AND(childQ.getOid().NE(oid));
    }

    try (OIterator<? extends UasComponent> i = childQ.getIterator())
    {
      if (i.hasNext())
      {
        return true;
      }
    }

    return false;
  }

  public static void validateFolderName(String parentId, String name)
  {
    if (!isValidName(name))
    {
      MdBusinessDAOIF mdBusiness = MdBusinessDAO.getMdBusinessDAO(UasComponent.CLASS);
      MdAttributeConcreteDAOIF mdAttribute = mdBusiness.definesAttribute(UasComponent.NAME);

      InvalidUasComponentNameException ex = new InvalidUasComponentNameException("The component name has an invalid character");
      ex.setAttributeName(mdAttribute.getDisplayLabel(Session.getCurrentLocale()));
    }
    else if (isDuplicateFolderName(parentId, null, name))
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

  public SiteObjectsResultSet getSiteObjects(String folder, Integer pageNumber, Integer pageSize)
  {
    return new SiteObjectsResultSet(0, pageNumber, pageSize, new LinkedList<SiteObject>(), folder);
  }

  protected SiteObjectsResultSet getSiteObjects(String folder, List<SiteObject> objects, Integer pageNumber, Integer pageSize)
  {
    final int maxKeys = 500;
    String key = this.getS3location() + folder;

    BasicAWSCredentials awsCreds = new BasicAWSCredentials(AppProperties.getS3AccessKey(), AppProperties.getS3SecretKey());
    AmazonS3 client = new AmazonS3Client(new StaticCredentialsProvider(awsCreds));

    String bucketName = AppProperties.getBucketName();

    ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName).withPrefix(key);
    listObjectsRequest.setMaxKeys(maxKeys);
    
    int curIndex = 0;
    
    int pageIndexStart = 0;
    int pageIndexStop = 0;
    if (pageNumber != null && pageSize != null)
    {
      pageIndexStart = (pageNumber-1)*pageSize;
      pageIndexStop = pageNumber*pageSize;
    }
    int awsPageNum = 1;
    
    ObjectListing objectListing = client.listObjects(listObjectsRequest);

    while (true)
    {
      List<S3ObjectSummary> list = objectListing.getObjectSummaries();
      Iterator<S3ObjectSummary> objIter = list.iterator();
      
      if (pageNumber == null || pageSize == null || (pageIndexStart >= maxKeys*(awsPageNum-1) && pageIndexStop <= maxKeys*awsPageNum))
      {
        while (objIter.hasNext())
        {
          S3ObjectSummary summary = objIter.next();
          
          String summaryKey = summary.getKey();
          
          if (!summaryKey.endsWith("/") && !summaryKey.contains("thumbnails/"))
          {
            if ((pageSize == null || (curIndex >= pageIndexStart && curIndex < pageIndexStop)))
            {
              objects.add(SiteObject.create(this, key, summary));
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

  public void delete(String key)
  {
    BasicAWSCredentials awsCreds = new BasicAWSCredentials(AppProperties.getS3AccessKey(), AppProperties.getS3SecretKey());
    AmazonS3 client = new AmazonS3Client(new StaticCredentialsProvider(awsCreds));
    String bucketName = AppProperties.getBucketName();

    DeleteObjectRequest request = new DeleteObjectRequest(bucketName, key);

    client.deleteObject(request);

    SolrService.deleteDocument(this, key);
  }

  public S3Object download(String key)
  {
    BasicAWSCredentials awsCreds = new BasicAWSCredentials(AppProperties.getS3AccessKey(), AppProperties.getS3SecretKey());
    AmazonS3 client = new AmazonS3Client(new StaticCredentialsProvider(awsCreds));
    String bucketName = AppProperties.getBucketName();

    GetObjectRequest request = new GetObjectRequest(bucketName, key);

    return client.getObject(request);
  }

  public int getItemCount(String key)
  {
    int count = 0;

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
    list.add(AttributeType.create(this.getMdAttributeDAO(UasComponent.FOLDERNAME), true, new AdminCondition()));
    list.add(AttributeType.create(this.getMdAttributeDAO(UasComponent.DESCRIPTION)));

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

    try (OIterator<? extends Site> it = query.getIterator())
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

    return new JSONObject(sWriter.toString());
  }

  public static JSONArray bbox()
  {
    try
    {
      MdBusinessDAOIF mdBusiness = MdBusinessDAO.getMdBusinessDAO(UasComponent.CLASS);
      MdAttributeConcreteDAOIF mdAttribute = mdBusiness.definesAttribute(UasComponent.GEOPOINT);

      StringBuffer sql = new StringBuffer();
      sql.append("SELECT ST_AsText(ST_Extent(" + mdAttribute.getColumnName() + ")) AS bbox");
      sql.append(" FROM " + mdBusiness.getTableName());
      sql.append(" WHERE " + mdAttribute.getColumnName() + " IS NOT NULL");

      try (ResultSet resultSet = Database.query(sql.toString()))
      {
        if (resultSet.next())
        {
          String bbox = resultSet.getString("bbox");

          if (bbox != null)
          {
            Pattern p = Pattern.compile("POLYGON\\(\\((.*)\\)\\)");
            Matcher m = p.matcher(bbox);

            if (m.matches())
            {
              String coordinates = m.group(1);
              List<Coordinate> coords = new LinkedList<Coordinate>();

              for (String c : coordinates.split(","))
              {
                String[] xAndY = c.split(" ");
                double x = Double.valueOf(xAndY[0]);
                double y = Double.valueOf(xAndY[1]);

                coords.add(new Coordinate(x, y));
              }

              Envelope e = new Envelope(coords.get(0), coords.get(2));

              JSONArray bboxArr = new JSONArray();
              bboxArr.put(e.getMinX());
              bboxArr.put(e.getMinY());
              bboxArr.put(e.getMaxX());
              bboxArr.put(e.getMaxY());

              return bboxArr;
            }
          }
        }
      }
    }
    catch (Exception e)
    {
      // Ignore the error and just return the default bounding box
    }

    // Extent of the continental United States
    JSONArray bboxArr = new JSONArray();
    bboxArr.put(-125.0011);
    bboxArr.put(24.9493);
    bboxArr.put(-66.9326);
    bboxArr.put(49.5904);

    return bboxArr;
  }

  /**
   * Returns tasks associated with this item.
   * 
   * @return tasks associated with this item.
   */
  public abstract List<AbstractWorkflowTask> getTasks();

  public List<String> uploadArchive(AbstractWorkflowTask task, ApplicationResource archive, String uploadTarget)
  {
    throw new UnsupportedOperationException();
  }

  public List<String> uploadZipArchive(AbstractWorkflowTask task, ApplicationResource archive, String uploadTarget)
  {
    throw new UnsupportedOperationException();
  }

  public Integer getNumberOfChildren()
  {
    try (OIterator<? extends UasComponent> children = this.getAllComponents())
    {
      return children.getAll().size();
    }
  }

  public UasComponent getChild(String name)
  {
    QueryFactory factory = new QueryFactory();
    ComponentHasComponentQuery rQuery = new ComponentHasComponentQuery(factory);
    rQuery.WHERE(rQuery.getParent().EQ(this));

    UasComponentQuery query = new UasComponentQuery(factory);
    query.WHERE(query.getName().EQ(name));
    query.AND(query.component(rQuery));

    try (OIterator<? extends UasComponent> children = query.getIterator())
    {
      if (children.hasNext())
      {
        return children.next();
      }
    }

    return null;
  }

  public String getStoreName(String key)
  {
    String baseName = FilenameUtils.getBaseName(key);

    return this.getOid() + "-" + baseName;
  }

  public List<Document> getDocuments()
  {
    DocumentQuery query = new DocumentQuery(new QueryFactory());
    query.WHERE(query.getComponent().EQ(this));

    try (OIterator<? extends Document> iterator = query.getIterator())
    {
      return new LinkedList<Document>(iterator.getAll());
    }
  }

  public List<Product> getProducts()
  {
    ProductQuery query = new ProductQuery(new QueryFactory());
    query.WHERE(query.getComponent().EQ(this));

    try (OIterator<? extends Product> iterator = query.getIterator())
    {
      return new LinkedList<Product>(iterator.getAll());
    }
  }
}
