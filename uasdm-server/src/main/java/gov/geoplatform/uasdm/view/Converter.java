package gov.geoplatform.uasdm.view;

import gov.geoplatform.uasdm.bus.Collection;
import gov.geoplatform.uasdm.bus.Imagery;
import gov.geoplatform.uasdm.bus.Mission;
import gov.geoplatform.uasdm.bus.Project;
import gov.geoplatform.uasdm.bus.Site;
import gov.geoplatform.uasdm.bus.UasComponent;

import java.util.List;

import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.constants.EntityInfo;
import com.runwaysdk.dataaccess.EntityDAO;
import com.runwaysdk.dataaccess.attributes.entity.Attribute;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.session.Session;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public abstract class Converter
{

  public Converter()
  {

  }

  protected SiteItem convert(UasComponent uasComponent, boolean metadata, boolean hasChildren)
  {
    SiteItem siteItem = new SiteItem();

    siteItem.setId(uasComponent.getOid());

    String typeName = uasComponent.getMdClass().getTypeName();
    siteItem.setType(typeName);

    String typeLabel = uasComponent.getMdClass().getDisplayLabel(Session.getCurrentLocale());
    siteItem.setTypeLabel(typeLabel);

    List<AttributeType> attributes = uasComponent.attributes();

    for (AttributeType attribute : attributes)
    {
      siteItem.setValue(attribute.getName(), uasComponent.getObjectValue(attribute.getName()));
    }

    siteItem.setGeometry(uasComponent.getGeoPoint());
    siteItem.setNumberOfChildren(uasComponent.getNumberOfChildren());

    if (metadata)
    {
      siteItem.setAttributes(attributes);
    }

    return siteItem;
  }

  protected UasComponent convert(SiteItem siteItem, UasComponent uasComponent)
  {
    List<AttributeType> attributes = uasComponent.attributes();

    for (AttributeType attribute : attributes)
    {
      uasComponent.setValue(attribute.getName(), siteItem.getValue(attribute.getName()));
    }

    Geometry geometry = siteItem.getGeometry();

    if (geometry != null && geometry instanceof Point)
    {
      uasComponent.setGeoPoint((Point) geometry);
    }

    return uasComponent;
  }

  protected UasComponent convertNew(UasComponent uasComponent, SiteItem siteItem)
  {
    List<AttributeType> attributes = uasComponent.attributes();

    for (AttributeType attribute : attributes)
    {
      uasComponent.setValue(attribute.getName(), siteItem.getValue(attribute.getName()));
    }

    Geometry geometry = siteItem.getGeometry();

    if (geometry != null && geometry instanceof Point)
    {
      uasComponent.setGeoPoint((Point) geometry);
    }

    // Sets the id to the id of the {@link SiteItem} which came from the backend
    // initially.
    if (siteItem.getId() != null)
    {
      EntityDAO entityDAO = (EntityDAO) BusinessFacade.getEntityDAO(uasComponent);
      Attribute attribute = entityDAO.getAttribute(EntityInfo.OID);
      attribute.setValue(siteItem.getId());
    }

    return uasComponent;
  }

  /**
   * Returns null if the given parent type has no child type.
   * 
   * @param parent
   * @param siteItem
   * @return
   */
  public static UasComponent toNewUasComponent(UasComponent parent, SiteItem siteItem)
  {
    UasComponent newChild = parent != null ? parent.createChild(siteItem.getType()) : new Site();

    if (newChild != null)
    {
      return factory(newChild).convertNew(newChild, siteItem);
    }
    else
    {
      return null;
    }
  }

  public static UasComponent toExistingUasComponent(SiteItem siteItem)
  {
    UasComponent uasComponent = UasComponent.get(siteItem.getId());

    return factory(uasComponent).convert(siteItem, uasComponent);
  }

  public static SiteItem toSiteItem(UasComponent uasComponent, boolean metadata)
  {
    return toSiteItem(uasComponent, metadata, false);
  }

  public static SiteItem toSiteItem(UasComponent uasComponent, boolean metadata, boolean hasChildren)
  {
    return factory(uasComponent).convert(uasComponent, metadata, hasChildren);
  }

  private static Converter factory(UasComponent uasComponent)
  {
    if (uasComponent instanceof Site)
    {
      return new SiteConverter();
    }
    else if (uasComponent instanceof Project)
    {
      return new ProjectConverter();
    }
    else if (uasComponent instanceof Mission)
    {
      return new MissionConverter();
    }
    else if (uasComponent instanceof Collection)
    {
      return new CollectionConverter();
    }
    else if (uasComponent instanceof Imagery)
    {
      return new ImageryConverter();
    }
    else
    {
      // Should never hit this case unless a new type is added to the hierarchy
      return null;
    }

  }

}
