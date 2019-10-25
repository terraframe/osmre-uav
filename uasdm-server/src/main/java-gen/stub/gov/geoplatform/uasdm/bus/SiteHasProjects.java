package gov.geoplatform.uasdm.bus;

public class SiteHasProjects extends SiteHasProjectsBase
{
  public static final long serialVersionUID = -1157135022;
  
  public SiteHasProjects(String parentOid, String childOid)
  {
    super(parentOid, childOid);
  }
  
  public SiteHasProjects(gov.geoplatform.uasdm.bus.Site parent, gov.geoplatform.uasdm.bus.Project child)
  {
    this(parent.getOid(), child.getOid());
  }
  
}
