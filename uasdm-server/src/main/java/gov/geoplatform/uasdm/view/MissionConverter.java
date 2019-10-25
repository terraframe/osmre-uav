package gov.geoplatform.uasdm.view;

import gov.geoplatform.uasdm.model.MissionIF;
import gov.geoplatform.uasdm.model.UasComponentIF;

public class MissionConverter extends Converter
{
  public MissionConverter()
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

  protected MissionIF convertNew(UasComponentIF uasComponent, SiteItem siteItem)
  {
    return (MissionIF) super.convertNew(uasComponent, siteItem);
  }
}
