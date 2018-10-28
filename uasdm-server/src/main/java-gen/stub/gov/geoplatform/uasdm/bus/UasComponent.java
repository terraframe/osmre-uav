package gov.geoplatform.uasdm.bus;

public abstract class UasComponent extends UasComponentBase
{
  private static final long serialVersionUID = -2027002868;
  
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
  
}
