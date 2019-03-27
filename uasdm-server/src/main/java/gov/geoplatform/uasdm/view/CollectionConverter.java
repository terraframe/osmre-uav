package gov.geoplatform.uasdm.view;

import gov.geoplatform.uasdm.bus.Collection;
import gov.geoplatform.uasdm.bus.UasComponent;

public class CollectionConverter extends Converter
{
  public CollectionConverter()
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

  protected Collection convertNew(UasComponent uasComponent, SiteItem siteItem)
  {
    return (Collection) super.convertNew(uasComponent, siteItem);
  }
}
