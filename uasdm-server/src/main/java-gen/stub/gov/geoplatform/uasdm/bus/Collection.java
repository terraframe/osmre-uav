package gov.geoplatform.uasdm.bus;

import com.runwaysdk.dataaccess.transaction.Transaction;

public class Collection extends CollectionBase
{
  private static final long serialVersionUID = 1371809368;
  
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
  public void applyWithParent(Mission parent)
  { 
    super.applyWithParent(parent);
  }
}
