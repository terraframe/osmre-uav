package gov.geoplatform.uasdm.bus;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

public abstract class UasComponent extends UasComponentBase
{
  private static final long serialVersionUID = -2027002868;
  
  private static final String S3_BUCKET      = "osmre-uas-repo";
  
  public UasComponent()
  {
    super();
  }
  
  /**
   * For the POC, each type has only one child type. Use polymorphism
   * to return the correct type. 
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
    if (this.isModified(UasComponent.NAME))
    {
      String name = this.getName();
      
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
          name.contains("/") ||
          name.contains("\\"))
      {
        throw new InvalidUasComponentNameException("The name field has an invalid character");
      }
    }
    
    
//    However, no spaces or special characters such as <, >, -, +, =, !, @, #, $, %, ^, &, *, ?,/, \ or apostrophes will be allowed.
    
    if (this.isNew())
    {
//      QueryFactory qf = new QueryFactory();
//      UasComponentQuery childQ = new UasComponentQuery(qf);
//      
//      UasComponentQuery parentQ = new UasComponentQuery(qf);
//      parentQ.WHERE(parentQ.getOid().EQ(parent.getOid()));
//      
//      
//      
//      childQ.WHERE(childQ.getName().EQ(this.getName()));
//      childQ.AND(childQ.getOid().NE(this.getOid()));
//      childQ.AND(childQ.component(parentQ));
//      
//      OIterator<? extends UasComponent> i = childQ.getIterator();
//      
//      for (UasComponent uasComponent : i)
//      {
//        System.out.println("Duplicate Found! "+uasComponent.getName());
//      }
      
      
//      DuplicateComponentException e = new DuplicateComponentException();
//      e.setParentName("");
//      e.setChildComponentLabel("");
//      e.setChildName("");
      
      
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
   * Builds a key for S3 that conforms to the directory structure requirements. If the parent is null, then 
   * 
   * @param uasComponent null if no parent
   * @return a key for S3 that conforms to the directory structure requirements.
   */
  public String buildS3Key(UasComponent parent)
  {
    String key = new String();
    
    if (parent != null)
    {
      key += parent.getS3location();
    }

    key += this.getName()+"/";
    
    return key;
  }
  
  
  protected void createS3Folder(String key)
  {    
//    AmazonS3 client = new AmazonS3Client(new ClasspathPropertiesFileCredentialsProvider());
//    
//    // create meta-data for your folder and set content-length to 0
//    ObjectMetadata metadata = new ObjectMetadata();
//    metadata.setContentLength(0);
//
//    // create empty content
//    InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
//    
//    PutObjectRequest putObjectRequest = new PutObjectRequest(S3_BUCKET,
//        key, emptyContent, metadata);
//    
//    // send request to S3 to create folder
//    client.putObject(putObjectRequest);
 
  }
  
  protected void deleteS3Folder(String key)
  {
//    AmazonS3 client = new AmazonS3Client(new ClasspathPropertiesFileCredentialsProvider());
//    
//    DeleteObjectsRequest multiObjectDeleteRequest = new DeleteObjectsRequest(S3_BUCKET)
//    .withKeys(key)
//    .withQuiet(false);
//    
//    DeleteObjectsResult delObjRes = client.deleteObjects(multiObjectDeleteRequest);
//    int successfulDeletes = delObjRes.getDeletedObjects().size();
//    System.out.println(successfulDeletes + " objects successfully deleted. "+key);
  }
}
