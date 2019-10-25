package gov.geoplatform.uasdm.bus;

public abstract class ComponentHasComponent extends ComponentHasComponentBase
{
  public static final long serialVersionUID = 39531277;
  
  public ComponentHasComponent(String parentOid, String childOid)
  {
    super(parentOid, childOid);
  }
  
  public ComponentHasComponent(gov.geoplatform.uasdm.bus.UasComponent parent, gov.geoplatform.uasdm.bus.UasComponent child)
  {
    this(parent.getOid(), child.getOid());
  }
  
}
