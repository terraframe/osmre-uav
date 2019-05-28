package gov.geoplatform.uasdm.view;

import gov.geoplatform.uasdm.bus.Imagery;
import gov.geoplatform.uasdm.bus.UasComponent;

public class ImageryConverter extends Converter
{
  public ImageryConverter()
  {
    super();
  }

  @Override
  protected UasComponent convert(SiteItem siteItem, UasComponent uasComponent)
  {
    Imagery imagery = (Imagery)super.convert(siteItem, uasComponent);
    
    return imagery;
  }

  @Override
  protected SiteItem convert(UasComponent uasComponent, boolean metadata, boolean hasChildren)
  {
    SiteItem siteItem = super.convert(uasComponent, metadata, hasChildren);

    return siteItem;
  }

  protected Imagery convertNew(UasComponent uasComponent, SiteItem siteItem)
  {
    Imagery imagery = (Imagery)super.convertNew(uasComponent, siteItem);
    
    return imagery;
  }
}
