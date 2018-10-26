package gov.geoplatform.uasdm.datamanagement;

public class ProjectHasMission extends ProjectHasMissionBase
{
  private static final long serialVersionUID = 1088100052;
  
  public ProjectHasMission(String parentOid, String childOid)
  {
    super(parentOid, childOid);
  }
  
  public ProjectHasMission(gov.geoplatform.uasdm.datamanagement.Project parent, gov.geoplatform.uasdm.datamanagement.Mission child)
  {
    this(parent.getOid(), child.getOid());
  }
  
}
