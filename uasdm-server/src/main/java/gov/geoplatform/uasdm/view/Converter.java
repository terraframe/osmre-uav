package gov.geoplatform.uasdm.view;

import java.util.LinkedList;
import java.util.List;

import com.runwaysdk.ComponentIF;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.session.ReadPermissionException;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionIF;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import gov.geoplatform.uasdm.MetadataXMLGenerator;
import gov.geoplatform.uasdm.bus.Collection;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.ComponentFacade;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.ImageryIF;
import gov.geoplatform.uasdm.model.MissionIF;
import gov.geoplatform.uasdm.model.Page;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.ProjectIF;
import gov.geoplatform.uasdm.model.SiteIF;
import gov.geoplatform.uasdm.model.UasComponentIF;

public abstract class Converter
{

  public Converter()
  {

  }

  protected SiteItem convert(UasComponentIF uasComponent, boolean metadata, boolean hasChildren)
  {
    final SessionIF session = Session.getCurrentSession();

    if (!session.checkTypeAccess(Operation.READ, uasComponent.getMdClass()))
    {
      throw new ReadPermissionException("User does not have read access", (ComponentIF) uasComponent, session.getUser());
    }

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

  protected UasComponentIF convert(SiteItem siteItem, UasComponentIF uasComponent)
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

  protected UasComponentIF convertNew(UasComponentIF uasComponent, SiteItem siteItem)
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
//      EntityDAO entityDAO = (EntityDAO) BusinessFacade.getEntityDAO(uasComponent);
//      Attribute attribute = entityDAO.getAttribute(EntityInfo.OID);
//      attribute.setValue(siteItem.getId());
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
  public static UasComponentIF toNewUasComponent(UasComponentIF parent, SiteItem siteItem)
  {
    UasComponentIF newChild = parent != null ? parent.createChild(siteItem.getType()) : ComponentFacade.newRoot();

    if (newChild != null)
    {
      return factory(newChild).convertNew(newChild, siteItem);
    }
    else
    {
      return null;
    }
  }

  public static UasComponentIF toExistingUasComponent(SiteItem siteItem)
  {
    UasComponentIF uasComponent = ComponentFacade.getComponent(siteItem.getId());

    return factory(uasComponent).convert(siteItem, uasComponent);
  }

  public static SiteItem toSiteItem(UasComponentIF uasComponent, boolean metadata)
  {
    return toSiteItem(uasComponent, metadata, false);
  }

  public static SiteItem toSiteItem(UasComponentIF uasComponent, boolean metadata, boolean hasChildren)
  {
    return factory(uasComponent).convert(uasComponent, metadata, hasChildren);
  }

  private static Converter factory(UasComponentIF uasComponent)
  {
    if (uasComponent instanceof SiteIF)
    {
      return new SiteConverter();
    }
    else if (uasComponent instanceof ProjectIF)
    {
      return new ProjectConverter();
    }
    else if (uasComponent instanceof MissionIF)
    {
      return new MissionConverter();
    }
    else if (uasComponent instanceof CollectionIF)
    {
      return new CollectionConverter();
    }
    else if (uasComponent instanceof ImageryIF)
    {
      return new ImageryConverter();
    }
    else
    {
      // Should never hit this case unless a new type is added to the hierarchy
      return null;
    }
  }

  public static ProductView toView(ProductIF product, List<UasComponentIF> components)
  {
    final SessionIF session = Session.getCurrentSession();

    if (!session.checkTypeAccess(Operation.READ, product.getMdClass()))
    {
      throw new ReadPermissionException("User does not have read access", (ComponentIF) product, session.getUser());
    }

    ProductView view = new ProductView();

    populate(view, product, components);

    return view;
  }

  protected static void populate(ProductView view, ProductIF product, List<UasComponentIF> components)
  {
    List<SiteItem> list = new LinkedList<SiteItem>();

    for (UasComponentIF component : components)
    {
      list.add(Converter.toSiteItem(component, false));
    }

    view.setComponents(list);
    view.setId(product.getOid());
    view.setName(product.getName());

    if (product.getImageKey() == null || product.getMapKey() == null)
    {
      product.calculateKeys(new LinkedList<UasComponentIF>(components));
    }

    if (product.getImageKey() != null && product.getImageKey().length() > 0)
    {
      view.setImageKey(product.getImageKey());
    }

    if (product.getMapKey() != null && product.getMapKey().length() > 0)
    {
      view.setMapKey(product.getMapKey());

      if ( ( product.getBoundingBox() == null || product.getBoundingBox().length() == 0 ))
      {
        product.updateBoundingBox();
      }

      String bbox = product.getBoundingBox();

      if (bbox != null)
      {
        view.setBoundingBox(bbox);
      }
    }
  }

  public static ProductDetailView toDetailView(ProductIF product, List<UasComponentIF> components, List<DocumentIF> generated, Integer pageNumber, Integer pageSize)
  {
    final SessionIF session = Session.getCurrentSession();

    if (!session.checkTypeAccess(Operation.READ, product.getMdClass()))
    {
      throw new ReadPermissionException("User does not have read access", (ComponentIF) product, session.getUser());
    }

    ProductDetailView view = new ProductDetailView();

    populate(view, product, components);

    Page<DocumentIF> page = product.getGeneratedFromDocuments(pageNumber, pageSize);

    // Get metadata
    FlightMetadata metadata = FlightMetadata.get(product.getComponent(), Collection.RAW, MetadataXMLGenerator.FILENAME);

    if (metadata != null)
    {
      view.setPilotName(metadata.getName());
      view.setSensor(metadata.getSensor().getName());
    }

    view.setDateTime(product.getLastUpdateDate());
    view.setPage(page);

    return view;
  }

}
