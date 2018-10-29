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
  protected UasComponent convert(SiteItem siteItem, UasComponent uasComponent)
  {
    return super.convert(siteItem, uasComponent);
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
