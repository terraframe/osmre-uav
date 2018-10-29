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
  
  protected SiteItem convert(Mission mission)
  {
    return super.convert(mission);
  }
  
  protected Mission convertNew(UasComponent uasComponent, SiteItem siteItem)
  {
    return (Mission)super.convertNew(uasComponent, siteItem);
  }
}
