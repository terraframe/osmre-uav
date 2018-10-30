package gov.geoplatform.uasdm.bus;


public class Site extends SiteBase
{
  private static final long serialVersionUID    = -986618112;
  
  public static final String DEFAULT_SITE_NAME = "Cottonwood";
  
  public Site()
  {
    super();
  }
  
  public Project createChild()
  {
    return new Project();
  }
  
  public ComponentHasComponent addComponent(gov.geoplatform.uasdm.bus.UasComponent uasComponent)
  {
    return this.addProjects((Project)uasComponent);
  }
}
