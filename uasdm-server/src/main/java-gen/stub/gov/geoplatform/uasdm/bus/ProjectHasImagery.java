package gov.geoplatform.uasdm.bus;

public class ProjectHasImagery extends ProjectHasImageryBase
{
  public static final long serialVersionUID = -2017450119;
  
  public ProjectHasImagery(String parentOid, String childOid)
  {
    super(parentOid, childOid);
  }
  
  public ProjectHasImagery(gov.geoplatform.uasdm.bus.Project parent, gov.geoplatform.uasdm.bus.Imagery child)
  {
    this(parent.getOid(), child.getOid());
  }
  
}
