package gov.geoplatform.uasdm.view;

import gov.geoplatform.uasdm.model.ProjectIF;
import gov.geoplatform.uasdm.model.UasComponentIF;

public class ProjectConverter extends Converter
{
  public ProjectConverter()
  {
    super();
  }

  @Override
  protected UasComponentIF convert(SiteItem siteItem, UasComponentIF uasComponent)
  {
    return super.convert(siteItem, uasComponent);
  }

  @Override
  protected SiteItem convert(UasComponentIF uasComponent, boolean metadata, boolean hasChildren)
  {
    return super.convert(uasComponent, metadata, hasChildren);
  }

  protected ProjectIF convertNew(UasComponentIF uasComponent, SiteItem siteItem)
  {
    return (ProjectIF) super.convertNew(uasComponent, siteItem);
  }
}
