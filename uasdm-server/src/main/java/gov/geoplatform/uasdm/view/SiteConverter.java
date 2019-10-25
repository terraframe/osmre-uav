package gov.geoplatform.uasdm.view;

import gov.geoplatform.uasdm.model.SiteIF;
import gov.geoplatform.uasdm.model.UasComponentIF;

public class SiteConverter extends Converter
{
  public SiteConverter()
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

  protected SiteIF convertNew(UasComponentIF uasComponent, SiteItem siteItem)
  {
    return (SiteIF) super.convertNew(uasComponent, siteItem);
  }
}
