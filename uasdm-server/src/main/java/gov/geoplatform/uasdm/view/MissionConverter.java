package gov.geoplatform.uasdm.view;

import gov.geoplatform.uasdm.bus.Mission;
import gov.geoplatform.uasdm.bus.Project;
import gov.geoplatform.uasdm.bus.UasComponent;

public class MissionConverter extends Converter
{
  public MissionConverter()
  {
    super();
  }
  
  @Override
  protected Mission convert(SiteItem siteItem)
  {
    Mission mission = new Mission();
    
    return (Mission)convert(siteItem, mission);
  }
  
  protected UasComponent convert(SiteItem siteItem, Mission mission)
  {
    return super.convert(siteItem, mission);
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
