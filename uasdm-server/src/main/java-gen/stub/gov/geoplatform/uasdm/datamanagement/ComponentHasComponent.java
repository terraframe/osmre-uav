package gov.geoplatform.uasdm.datamanagement;

public abstract class ComponentHasComponent extends ComponentHasComponentBase
{
  private static final long serialVersionUID = -1456507202;
  
  public ComponentHasComponent(String parentOid, String childOid)
  {
    super(parentOid, childOid);
  }
  
  public ComponentHasComponent(gov.geoplatform.uasdm.datamanagement.UasComponent parent, gov.geoplatform.uasdm.datamanagement.UasComponent child)
  {
    this(parent.getOid(), child.getOid());
  }
  
}
