/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.geoplatform.uasdm.view;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.commongeoregistry.adapter.metadata.OrganizationDTO;
import org.json.JSONObject;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import com.google.gson.JsonObject;
import com.runwaysdk.ComponentIF;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.session.ReadPermissionException;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionIF;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.GenericException;
import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.controller.PointcloudController;
import gov.geoplatform.uasdm.graph.Document;
import gov.geoplatform.uasdm.graph.Platform;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.graph.Sensor;
import gov.geoplatform.uasdm.graph.UAV;
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
import gov.geoplatform.uasdm.processing.ODMZipPostProcessor;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.remote.s3.S3RemoteFileService;
import net.geoprism.registry.Organization;
import net.geoprism.registry.model.ServerOrganization;

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
      if (attribute instanceof AttributeDateType)
      {
        Date date = uasComponent.getObjectValue(attribute.getName());

        siteItem.setValue(attribute.getName(), Util.formatIso8601(date, false));
      }
      else if (attribute instanceof AttributeOrganizationType)
      {
        String oid = uasComponent.getObjectValue(attribute.getName());
        ServerOrganization organization = ServerOrganization.getByGraphId(oid);

        if (organization != null)
        {
          OrganizationDTO dto = organization.toDTO();

          JsonObject object = dto.toJSON();
          object.remove(OrganizationDTO.JSON_LOCALIZED_CONTACT_INFO);
          object.remove(OrganizationDTO.JSON_ENABLED);
          object.remove(OrganizationDTO.JSON_PARENT_CODE);
          object.remove(OrganizationDTO.JSON_PARENT_LABEL);

          siteItem.setValue(attribute.getName(), new JSONObject(object.toString()));
        }
      }
      else
      {
        siteItem.setValue(attribute.getName(), uasComponent.getObjectValue(attribute.getName()));
      }
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
      if ( ( attribute instanceof AttributeDateType ))
      {
        String dateStr = (String) siteItem.getValue(attribute.getName());

        if (dateStr != null)
        {
          try
          {
            uasComponent.setValue(attribute.getName(), Util.parseIso8601(dateStr, false));
          }
          catch (ParseException e)
          {
            GenericException exception = new GenericException(e);
            exception.setUserMessage(e.getMessage());
            throw exception;
          }
        }
        else
        {
          uasComponent.setValue(attribute.getName(), null);
        }
      }
      else if ( ( attribute instanceof AttributeIntegerType ))
      {
        Number number = (Number) siteItem.getValue(attribute.getName());

        if (number != null)
        {
          Integer dec;

          if (number instanceof Number)
          {
            dec = Integer.valueOf((int) number);
          }
          else
          {
            throw new UnsupportedOperationException("Unsupported type [" + number.getClass().getTypeName() + "].");
          }

          uasComponent.setValue(attribute.getName(), dec);
        }
        else
        {
          uasComponent.setValue(attribute.getName(), null);
        }
      }
      else if ( ( attribute instanceof AttributeOrganizationType ))
      {
        JSONObject object = (JSONObject) siteItem.getValue(attribute.getName());

        if (object != null && object.has(OrganizationDTO.JSON_CODE))
        {
          String code = object.getString(OrganizationDTO.JSON_CODE);
          ServerOrganization organization = ServerOrganization.getByCode(code);

          uasComponent.setValue(attribute.getName(), organization.getGraphOrganization());
        }
        else
        {
          uasComponent.setValue(attribute.getName(), null);
        }
      }
      else if ( ( attribute instanceof AttributeNumberType ))
      {
        Number number = (Number) siteItem.getValue(attribute.getName());

        if (number != null)
        {
          BigDecimal dec;
          if (number instanceof Double)
          {
            dec = new BigDecimal((Double) number);
          }
          else if (number instanceof Integer)
          {
            dec = new BigDecimal((Integer) number);
          }
          else if (number instanceof Float)
          {
            dec = new BigDecimal((Float) number);
          }
          else if (number instanceof Long)
          {
            dec = new BigDecimal((Long) number);
          }
          else if (number instanceof Short)
          {
            dec = new BigDecimal((Double) number);
          }
          else
          {
            throw new UnsupportedOperationException("Unsupported type [" + number.getClass().getTypeName() + "].");
          }

          uasComponent.setValue(attribute.getName(), dec);
        }
        else
        {
          uasComponent.setValue(attribute.getName(), null);
        }
      }
      else if (! ( attribute instanceof AttributePointType ))
      {
        uasComponent.setValue(attribute.getName(), siteItem.getValue(attribute.getName()));
      }
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
      Object value = siteItem.getValue(attribute.getName());

      if (attribute instanceof AttributeOrganizationType)
      {
        JSONObject object = (JSONObject) value;
        String code = object.getString(Organization.CODE);

        ServerOrganization org = ServerOrganization.getByCode(code);

        uasComponent.setValue(attribute.getName(), org.getGraphOrganization());
      }
      else
      {
        uasComponent.setValue(attribute.getName(), value);
      }
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
      // EntityDAO entityDAO = (EntityDAO)
      // BusinessFacade.getEntityDAO(uasComponent);
      // Attribute attribute = entityDAO.getAttribute(EntityInfo.OID);
      // attribute.setValue(siteItem.getId());
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

    final String s3Loc = components.size() > 0 ? components.get(components.size() - 1).getS3location() : "";
    boolean hasPointcloud = RemoteFileFacade.objectExists(s3Loc + ODMZipPostProcessor.POTREE + "/metadata.json") || RemoteFileFacade.objectExists(s3Loc + ODMZipPostProcessor.POTREE + "/ept.json") || RemoteFileFacade.objectExists(s3Loc + PointcloudController.LEGACY_POTREE_SUPPORT + "/cloud.js");
    view.setHasPointcloud(hasPointcloud);

    view.setHasAllZip( ( (Product) product ).hasAllZip());

    view.setComponents(list);
    view.setId(product.getOid());
    view.setName(product.getName());
    view.setPublished(product.isPublished());

    List<DocumentIF> mappables = ( (Product) product ).getMappableDocuments();
    view.setMappables(mappables.stream().map(d -> DocumentView.fromDocument(d)).collect(Collectors.toList()));

    if (product.getImageKey() == null || product.getImageKey().length() == 0)
    {
      product.calculateKeys(new LinkedList<UasComponentIF>(components));
    }

    if (product.getImageKey() != null && product.getImageKey().length() > 0)
    {
      view.setImageKey(product.getImageKey());
    }

    Optional<DocumentIF> ortho = ( (Product) product ).getMappableOrtho();
    if (ortho.isPresent())
    {
      view.setOrthoKey( ( (Document) ortho.get() ).getS3location());
    }

    Optional<DocumentIF> dem = ( (Product) product ).getMappableDEM();
    if (dem.isPresent())
    {
      view.setDemKey( ( (Document) dem.get() ).getS3location());
    }

    if (product.isPublished())
    {
      // "https://osmre-uas-dev-public.s3.amazonaws.com/-stac-/2c8712a5-d051-4249-a9bc-fedd3795ce74.json"

      String bucket = "https://" + AppProperties.getPublicBucketName() + ".s3.amazonaws.com/";

      view.setPublicStacUrl(bucket + S3RemoteFileService.STAC_BUCKET + "/" + product.getOid() + ".json");
      // view.setPublicTilejson(bucket + "cog/tilejson.json?path=" +
      // URLEncoder.encode(mappable.getS3location(),
      // StandardCharsets.UTF_8.name()));
    }

    if (mappables.size() > 0)
    {
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
    CollectionIF collection = (CollectionIF) product.getComponent();

    view.setPilotName(collection.getPocName());

    Sensor sensor = ( (CollectionIF) collection ).getSensor();
    if (sensor != null)
    {
      view.setSensor(sensor.toJSON());
    }

    UAV uav = ( (CollectionIF) collection ).getUav();
    if (uav != null)
    {
      view.setUAV(uav.toJSON());

      Platform platform = uav.getPlatform();
      if (platform != null)
      {
        view.setPlatform(platform.toJSON());
      }
    }

    if (collection.getCollectionDate() != null)
    {
      view.setCollectionDate(collection.getCollectionDate());
    }

    view.setDateTime(product.getLastUpdateDate());

    page.setPresignThumnails(true);
    view.setPage(new Page<JSONWrapper>(page.getCount(), page.getPageNumber(), page.getPageSize(), page.getResults().stream().map(r -> {
      return new JSONWrapper(r.toJSON());
    }).collect(Collectors.toList())));

    return view;
  }

}
