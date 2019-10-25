package gov.geoplatform.uasdm.view;

import gov.geoplatform.uasdm.bus.Imagery;
import gov.geoplatform.uasdm.model.UasComponentIF;

public class ImageryConverter extends Converter
{
  public ImageryConverter()
  {
    super();
  }

  @Override
  protected UasComponentIF convert(SiteItem siteItem, UasComponentIF uasComponent)
  {
    Imagery imagery = (Imagery)super.convert(siteItem, uasComponent);
    
    return imagery;
  }

  @Override
  protected SiteItem convert(UasComponentIF uasComponent, boolean metadata, boolean hasChildren)
  {
    SiteItem siteItem = super.convert(uasComponent, metadata, hasChildren);

    return siteItem;
  }

  protected Imagery convertNew(UasComponentIF uasComponent, SiteItem siteItem)
  {
    Imagery imagery = (Imagery)super.convertNew(uasComponent, siteItem);
    
    return imagery;
  }
}
