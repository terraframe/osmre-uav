package gov.geoplatform.uasdm.view;

import gov.geoplatform.uasdm.bus.Collection;
import gov.geoplatform.uasdm.bus.Mission;
import gov.geoplatform.uasdm.bus.Project;
import gov.geoplatform.uasdm.bus.Site;
import gov.geoplatform.uasdm.bus.UasComponent;

import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.constants.EntityInfo;
import com.runwaysdk.dataaccess.EntityDAO;
import com.runwaysdk.dataaccess.attributes.entity.Attribute;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.session.Session;

public abstract class Converter
{

  public Converter()
  {

  }

  protected SiteItem convert(UasComponent uasComponent)
  {
    SiteItem siteItem = new SiteItem();

    siteItem.setId(uasComponent.getOid());

    String typeName = uasComponent.getMdClass().getTypeName();
    siteItem.setType(typeName);

    String typeLabel = uasComponent.getMdClass().getDisplayLabel(Session.getCurrentLocale());
    siteItem.setTypeLabel(typeLabel);

    siteItem.setName(uasComponent.getName());

    OIterator<? extends UasComponent> children = uasComponent.getAllComponents();

    try
    {
      if (children.hasNext())
      {
        siteItem.setHasChildren(true);
      }
      else
      {
        siteItem.setHasChildren(false);
      }
    }
    finally
    {
      children.close();
    }

    return siteItem;
  }

  protected UasComponent convert(SiteItem siteItem, UasComponent uasComponent)
  {
    uasComponent.setName(siteItem.getName());

    return uasComponent;
  }

  protected UasComponent convertNew(UasComponent uasComponent, SiteItem siteItem)
  {
    uasComponent.setName(siteItem.getName());

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
    UasComponent newChild = parent.createChild();

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

  public static SiteItem toSiteItem(UasComponent uasComponent)
  {
    return factory(uasComponent).convert(uasComponent);
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
    else
    {
      // Should never hit this case unless a new type is added to the hierarchy
      return null;
    }

  }

}
