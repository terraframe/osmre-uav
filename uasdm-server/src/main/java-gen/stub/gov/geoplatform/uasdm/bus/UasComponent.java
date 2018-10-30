package gov.geoplatform.uasdm.bus;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsResult;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.runwaysdk.dataaccess.transaction.Transaction;

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
    if (this.isNew())
    {
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
    AmazonS3 client = new AmazonS3Client(new ClasspathPropertiesFileCredentialsProvider());
    
    // create meta-data for your folder and set content-length to 0
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength(0);

    // create empty content
    InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
    
    PutObjectRequest putObjectRequest = new PutObjectRequest(S3_BUCKET,
        key, emptyContent, metadata);
    
    // send request to S3 to create folder
    client.putObject(putObjectRequest);
 
  }
  
  protected void deleteS3Folder(String key)
  {
    AmazonS3 client = new AmazonS3Client(new ClasspathPropertiesFileCredentialsProvider());
    
    DeleteObjectsRequest multiObjectDeleteRequest = new DeleteObjectsRequest(S3_BUCKET)
    .withKeys(key)
    .withQuiet(false);
    
    DeleteObjectsResult delObjRes = client.deleteObjects(multiObjectDeleteRequest);
    int successfulDeletes = delObjRes.getDeletedObjects().size();
    System.out.println(successfulDeletes + " objects successfully deleted. "+key);
  }
}
