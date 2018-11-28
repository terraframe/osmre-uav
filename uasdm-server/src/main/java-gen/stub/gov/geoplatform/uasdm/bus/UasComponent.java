package gov.geoplatform.uasdm.bus;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.runwaysdk.dataaccess.MdClassDAOIF;
import com.runwaysdk.dataaccess.metadata.MdClassDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Session;

public abstract class UasComponent extends UasComponentBase
{
  private static final long   serialVersionUID = -2027002868;

  // private static final String S3_BUCKET = "osmre-uas-repo";
  private static final String S3_BUCKET        = "osmre-uas-test";

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
    if (this.isModified(UasComponent.NAME))
    {
      String name = this.getName();

      if (!isValidName(name))
      {
        throw new InvalidUasComponentNameException("The name field has an invalid character");
      }
    }

    if (this.isNew())
    {
      if (parent != null)
      {
        boolean isDuplicate = isDuplicateName(parent.getOid(), this.getOid(), this.getName());
        
        if(isDuplicate)
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
  }

  public void delete()
  {
    super.delete();

    if (!this.getS3location().trim().equals(""))
    {
      this.deleteS3Folder(this.getS3location());
    }
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

    PutObjectRequest putObjectRequest = new PutObjectRequest(S3_BUCKET, key, emptyContent, metadata);

    // send request to S3 to create folder
    client.putObject(putObjectRequest);
  }

  protected void deleteS3Folder(String key)
  {
    AmazonS3 client = new AmazonS3Client(new ClasspathPropertiesFileCredentialsProvider());

    DeleteObjectsRequest multiObjectDeleteRequest = new DeleteObjectsRequest(S3_BUCKET).withKeys(key).withQuiet(false);

    client.deleteObjects(multiObjectDeleteRequest);
    // DeleteObjectsResult delObjRes =
    // client.deleteObjects(multiObjectDeleteRequest);
    // int successfulDeletes = delObjRes.getDeletedObjects().size();
    // System.out.println(successfulDeletes + " objects successfully deleted.
    // "+key);
  }
  
  public static boolean isValidName(String name)
  {
    if (name.contains(" ") ||
        name.contains("<") ||
        name.contains(">") ||
        name.contains("-") ||
        name.contains("+") ||
        name.contains("=") ||
        name.contains("!") ||
        name.contains("@") ||
        name.contains("#") ||
        name.contains("$") ||
        name.contains("%") ||
        name.contains("^") ||
        name.contains("&") ||
        name.contains("*") ||
        name.contains("?") ||
        name.contains(";") ||
        name.contains(":") ||
        name.contains(",") ||
        name.contains("^") ||
        name.contains("{") ||
        name.contains("}") ||
        name.contains("]") ||
        name.contains("[") ||
        name.contains("`") ||
        name.contains("~") ||
        name.contains("|") ||
        name.contains("/") ||
        name.contains("\\"))
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
    if(!isValidName(name))
    {
      throw new InvalidUasComponentNameException("The name field has an invalid character");
    }
    else if(isDuplicateName(parentId, null, name))
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
}
