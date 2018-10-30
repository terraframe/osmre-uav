package gov.geoplatform.uasdm.bus;

import com.runwaysdk.dataaccess.transaction.Transaction;

public class Mission extends MissionBase
{
  private static final long serialVersionUID = -112103870;
  
  public static final String ACCESSIBLE_SUPPORT = "accessible_support";
  
  public Mission()
  {
    super();
  }
  
  public Collection createChild()
  {
    return new Collection();
  }
  
  public ComponentHasComponent addComponent(UasComponent uasComponent)
  {
    return this.addProject((Project)uasComponent);
  }
  
  /**
   * Creates the object and builds the relationship with the parent.
   * 
   * Creates directory in S3.
   * 
   * @param parent
   */
  @Transaction
  public void applyWithParent(Project parent)
  { 
    super.applyWithParent(parent);
    
    if (this.isNew())
    {      
      this.createS3Folder(this.buildAccessibleSupportKey());
    }
  }
  
  public void delete()
  { 
    super.delete();
    
    if (!this.getS3location().trim().equals(""))
    {
      this.deleteS3Folder(this.buildAccessibleSupportKey());
    }
  }
  
  private String buildAccessibleSupportKey()
  {
    return this.getS3location()+ACCESSIBLE_SUPPORT+"/";
  }
}
