package gov.geoplatform.uasdm.view;

import gov.geoplatform.uasdm.bus.Collection;
import gov.geoplatform.uasdm.bus.Mission;
import gov.geoplatform.uasdm.bus.UasComponent;

public class CollectionConverter extends Converter
{
  public CollectionConverter()
  {
    super();
  }
  
  @Override
  protected Collection convert(SiteItem siteItem)
  {
    Collection collection = new Collection();
    
    return (Collection)convert(siteItem, collection);
  }
  
  protected UasComponent convert(SiteItem siteItem, Collection collection)
  {
    return super.convert(siteItem, collection);
  }
  
  protected SiteItem convert(Collection collection)
  {
    return super.convert(collection);
  }
  
  protected Collection convertNew(UasComponent uasComponent, SiteItem siteItem)
  {
    return (Collection)super.convertNew(uasComponent, siteItem);
  }
}
