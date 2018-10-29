package gov.geoplatform.uasdm.view;

import gov.geoplatform.uasdm.bus.Project;
import gov.geoplatform.uasdm.bus.Site;
import gov.geoplatform.uasdm.bus.UasComponent;

public class ProjectConverter extends Converter
{
  public ProjectConverter()
  {
    super();
  }
  
  @Override
  protected Project convert(SiteItem siteItem)
  {
    Project project = new Project();
    
    return (Project)convert(siteItem, project);
  }
  
  protected UasComponent convert(SiteItem siteItem, Project project)
  {
    return super.convert(siteItem, project);
  }
  
  protected SiteItem convert(Project project)
  {
    return super.convert(project);
  }
  
  protected Project convertNew(UasComponent uasComponent, SiteItem siteItem)
  {
    return (Project)super.convertNew(uasComponent, siteItem);
  }
}
