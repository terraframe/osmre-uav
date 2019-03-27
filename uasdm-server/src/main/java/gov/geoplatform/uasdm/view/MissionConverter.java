package gov.geoplatform.uasdm.view;

import gov.geoplatform.uasdm.bus.Mission;
import gov.geoplatform.uasdm.bus.UasComponent;

public class MissionConverter extends Converter
{
  public MissionConverter()
  {
    super();
  }

  @Override
  protected UasComponent convert(SiteItem siteItem, UasComponent uasComponent)
  {
    return super.convert(siteItem, uasComponent);
  }

  @Override
  protected SiteItem convert(UasComponent uasComponent, boolean metadata, boolean hasChildren)
  {
    return super.convert(uasComponent, metadata, hasChildren);
  }

  protected Mission convertNew(UasComponent uasComponent, SiteItem siteItem)
  {
    return (Mission) super.convertNew(uasComponent, siteItem);
  }
}
