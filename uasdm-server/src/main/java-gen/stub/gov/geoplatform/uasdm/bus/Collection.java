package gov.geoplatform.uasdm.bus;

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
  
}
