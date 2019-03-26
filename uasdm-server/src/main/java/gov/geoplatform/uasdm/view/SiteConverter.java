package gov.geoplatform.uasdm.view;

import gov.geoplatform.uasdm.bus.Site;
import gov.geoplatform.uasdm.bus.UasComponent;

public class SiteConverter extends Converter
{
  public SiteConverter()
  {
    super();
  }

  @Override
  protected UasComponent convert(SiteItem siteItem, UasComponent uasComponent)
  {
    return super.convert(siteItem, uasComponent);
  }

  @Override
  protected SiteItem convert(UasComponent uasComponent, boolean metadata)
  {
    return super.convert(uasComponent, metadata);
  }

  protected Site convertNew(UasComponent uasComponent, SiteItem siteItem)
  {
    return (Site) super.convertNew(uasComponent, siteItem);
  }
}
