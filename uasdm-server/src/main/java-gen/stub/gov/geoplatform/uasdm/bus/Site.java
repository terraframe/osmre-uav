package gov.geoplatform.uasdm.bus;

public class Site extends SiteBase
{
  private static final long serialVersionUID = -986618112;
  
  public Site()
  {
    super();
  }
  
  public Project createChild()
  {
    return new Project();
  }
}
