package gov.geoplatform.uasdm.bus;

import com.runwaysdk.dataaccess.transaction.Transaction;

public class Collection extends CollectionBase
{
  private static final long serialVersionUID = 1371809368;
  
  public static final String RAW             = "raw";
  
  public static final String PTCLOUD         = "ptcloud";
  
  public static final String DEM             = "dem";
  
  public static final String ORTHO           = "ortho";
  
  public Collection()
  {
    super();
  }
  
  /**
   * Returns null, as a Collection cannot have a child.
   */
  public UasComponent createChild()
  {
    // TODO throw exception.
    
    return null;
  }
  
  public ComponentHasComponent addComponent(UasComponent uasComponent)
  {
    return this.addMission((Mission)uasComponent);
  }
  
  /**
   * Creates the object and builds the relationship with the parent.
   * 
   * Creates directory in S3.
   * 
   * @param parent
   */
  @Transaction
  @Override 
  public void applyWithParent(UasComponent parent)
  { 
    super.applyWithParent(parent);
    
    if (this.isNew())
    {      
      this.createS3Folder(this.buildRawKey());
      
      this.createS3Folder(this.buildPointCloudKey());
      
      this.createS3Folder(this.buildDemKey());
      
      this.createS3Folder(this.buildOrthoKey());
    }
  }
  
  public void delete()
  { 
    super.delete();
    
    if (!this.getS3location().trim().equals(""))
    {
      this.deleteS3Folder(this.buildRawKey());
      
      this.deleteS3Folder(this.buildPointCloudKey());
      
      this.deleteS3Folder(this.buildDemKey());
      
      this.deleteS3Folder(this.buildOrthoKey());
    }
  }
  
  private String buildRawKey()
  {
    return this.getS3location()+RAW+"/";
  }
  
  private String buildPointCloudKey()
  {
    return this.getS3location()+PTCLOUD+"/";
  }
  
  private String buildDemKey()
  {
    return this.getS3location()+DEM+"/";
  }
  
  private String buildOrthoKey()
  {
    return this.getS3location()+ORTHO+"/";
  }
}
