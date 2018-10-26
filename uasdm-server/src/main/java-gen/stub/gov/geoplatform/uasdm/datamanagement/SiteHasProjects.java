package gov.geoplatform.uasdm.datamanagement;

public class SiteHasProjects extends SiteHasProjectsBase
{
  private static final long serialVersionUID = -760712291;
  
  public SiteHasProjects(String parentOid, String childOid)
  {
    super(parentOid, childOid);
  }
  
  public SiteHasProjects(gov.geoplatform.uasdm.datamanagement.Site parent, gov.geoplatform.uasdm.datamanagement.Project child)
  {
    this(parent.getOid(), child.getOid());
  }
  
}
