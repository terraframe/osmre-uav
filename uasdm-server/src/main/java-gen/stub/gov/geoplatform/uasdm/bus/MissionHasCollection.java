package gov.geoplatform.uasdm.bus;

public class MissionHasCollection extends MissionHasCollectionBase
{
  public static final long serialVersionUID = 1677126317;
  
  public MissionHasCollection(String parentOid, String childOid)
  {
    super(parentOid, childOid);
  }
  
  public MissionHasCollection(gov.geoplatform.uasdm.bus.Mission parent, gov.geoplatform.uasdm.bus.Collection child)
  {
    this(parent.getOid(), child.getOid());
  }
  
}
