package gov.geoplatform.uasdm.datamanagement;

public class MissionHasCollection extends MissionHasCollectionBase
{
  private static final long serialVersionUID = 153190574;
  
  public MissionHasCollection(String parentOid, String childOid)
  {
    super(parentOid, childOid);
  }
  
  public MissionHasCollection(gov.geoplatform.uasdm.datamanagement.Mission parent, gov.geoplatform.uasdm.datamanagement.Collection child)
  {
    this(parent.getOid(), child.getOid());
  }
  
}
