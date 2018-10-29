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
  protected Site convert(SiteItem siteItem)
  {
    Site site = new Site();
    
    return (Site)convert(siteItem, site);
  }
  
  protected UasComponent convert(SiteItem siteItem, Site site)
  {
    return super.convert(siteItem, site);
  }
  
  protected SiteItem convert(Site site)
  {
    return super.convert(site);
  }
  
  protected Site convertNew(UasComponent uasComponent, SiteItem siteItem)
  {
    return (Site)super.convertNew(uasComponent, siteItem);
  }
}
