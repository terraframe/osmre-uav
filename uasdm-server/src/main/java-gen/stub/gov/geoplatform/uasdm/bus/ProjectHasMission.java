package gov.geoplatform.uasdm.bus;

public class ProjectHasMission extends ProjectHasMissionBase
{
  private static final long serialVersionUID = -914737203;
  
  public ProjectHasMission(String parentOid, String childOid)
  {
    super(parentOid, childOid);
  }
  
  public ProjectHasMission(gov.geoplatform.uasdm.bus.Project parent, gov.geoplatform.uasdm.bus.Mission child)
  {
    this(parent.getOid(), child.getOid());
  }
  
}
