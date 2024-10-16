/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.geoplatform.uasdm.graph;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;

import org.commongeoregistry.adapter.metadata.OrganizationDTO;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionIF;

import gov.geoplatform.uasdm.GenericException;
import gov.geoplatform.uasdm.command.GenerateMetadataCommand;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.JSONSerializable;
import gov.geoplatform.uasdm.model.Page;
import gov.geoplatform.uasdm.processing.report.CollectionReportFacade;
import net.geoprism.GeoprismUser;
import net.geoprism.registry.Organization;
import net.geoprism.registry.model.ServerOrganization;

public class UAV extends UAVBase implements JSONSerializable
{
  private static final long serialVersionUID = 1730854538;

  public UAV()
  {
    super();
  }

  @Override
  public void apply()
  {
    boolean isNew = this.isNew();

    super.apply();

    if (!isNew)
    {
      this.getReferencingMetadata().forEach(metadata -> {
        Optional<Collection> col = metadata.getCollection();
        
        if (col.isPresent()) {
          new GenerateMetadataCommand(col.get(), null, metadata).doIt();
        } else {
          List<Product> prods = metadata.getProducts();
          
          if (prods.size() > 0) {
            new GenerateMetadataCommand(prods.get(0).getComponent(), prods.get(0), metadata).doIt();
          }
        }
      });

      CollectionReportFacade.update(this).doIt();
    }
  }

  @Override
  @Transaction
  public void delete()
  {
    if (Collection.isUAVReferenced(this))
    {
      GenericException message = new GenericException();
      message.setUserMessage("The UAV cannot be deleted because it is being used in a Collection");
      throw message;
    }

    CollectionReportFacade.handleDelete(this).doIt();

    super.delete();
  }

  public String getBureauOid()
  {
    return this.getObjectValue(BUREAU);
  }

  @Override
  public Bureau getBureau()
  {
    return Bureau.get(getBureauOid());
  }

  public Platform getPlatform()
  {
    return Platform.get(this.getObjectValue(PLATFORM));
  }

  public ServerOrganization getServerOrganization()
  {
    return ServerOrganization.getByGraphId((String) this.getObjectValue(ORGANIZATION));
  }

  public List<CollectionMetadata> getReferencingMetadata()
  {
    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(CollectionMetadata.CLASS);
    MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(CollectionMetadata.UAV);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName() + "");
    statement.append(" WHERE " + mdAttribute.getColumnName() + " = :rid");

    final GraphQuery<CollectionMetadata> query = new GraphQuery<CollectionMetadata>(statement.toString());
    query.setParameter("rid", this.getRID());

    return query.getResults();
  }

  @Override
  public JSONObject toJSON()
  {
    JSONObject object = new JSONObject();
    object.put(UAV.OID, this.getOid());
    object.put(UAV.SERIALNUMBER, this.getSerialNumber());
    object.put(UAV.FAANUMBER, this.getFaaNumber());
    object.put(UAV.DESCRIPTION, this.getDescription());
    object.put(UAV.BUREAU, (String) this.getObjectValue(BUREAU));

    ServerOrganization organization = getServerOrganization();

    if (organization != null)
    {
      OrganizationDTO dto = organization.toDTO();

      JsonObject oObject = dto.toJSON();
      oObject.remove(OrganizationDTO.JSON_LOCALIZED_CONTACT_INFO);
      oObject.remove(OrganizationDTO.JSON_ENABLED);
      oObject.remove(OrganizationDTO.JSON_PARENT_CODE);
      oObject.remove(OrganizationDTO.JSON_PARENT_LABEL);

      object.put(UAV.ORGANIZATION, new JSONObject(oObject.toString()));
    }

    String platform = this.getObjectValue(UAV.PLATFORM);

    if (platform != null)
    {
      object.put(UAV.PLATFORM, platform);
    }

    if (this.getSeq() != null)
    {
      object.put(UAV.SEQ, this.getSeq());
    }

    return object;
  }

  public JSONObject toView()
  {
    Platform platform = this.getPlatform();
    PlatformType platformType = platform.getPlatformType();
    PlatformManufacturer manufacturer = platform.getManufacturer();
    Bureau bureau = this.getBureau();
    ServerOrganization organization = getServerOrganization();

    JSONObject object = new JSONObject();
    object.put(UAV.OID, this.getOid());
    object.put(UAV.SERIALNUMBER, this.getSerialNumber());
    object.put(UAV.FAANUMBER, this.getFaaNumber());
    if (bureau != null)
    {
      object.put(UAV.BUREAU, bureau.getName());
    }
    object.put(Platform.NAME, platform.getName());
    object.put(Platform.PLATFORMTYPE, platformType.getName());
    object.put(Platform.MANUFACTURER, manufacturer.getName());

    if (organization != null)
    {
      object.put(UAV.ORGANIZATION, organization.getDisplayLabel().getValue());
    }

    return object;
  }

  @Transaction
  public static UAV apply(JSONObject json)
  {
    UAV uav = null;

    if (json.has(UAV.OID))
    {
      String oid = json.getString(UAV.OID);

      if (oid != null)
      {
        uav = UAV.get(oid);
      }
    }

    if (uav == null)
    {
      uav = new UAV();
    }

    uav.setSerialNumber(json.getString(UAV.SERIALNUMBER));
    uav.setFaaNumber(json.getString(UAV.FAANUMBER));
    uav.setDescription(json.has(UAV.DESCRIPTION) ? json.getString(UAV.DESCRIPTION) : null);
    // uav.setBureau(Bureau.get(json.getString(UAV.BUREAU)));

    JSONObject organization = json.getJSONObject(UAV.ORGANIZATION);
    String code = organization.getString(Organization.CODE);

    uav.setOrganization(ServerOrganization.getByCode(code).getGraphOrganization());

    if (json.has(UAV.PLATFORM))
    {
      String oid = json.getString(UAV.PLATFORM);

      uav.setPlatform(Platform.get(oid));
    }
    else
    {
      uav.setPlatform(null);
    }

    if (json.has(UAV.SEQ))
    {
      uav.setSeq(json.getLong(UAV.SEQ));
    }

    uav.apply();

    return uav;
  }

  public JSONObject getMetadataOptions()
  {
    String platformOid = this.getObjectValue(UAV.PLATFORM);

    Platform platform = Platform.get(platformOid);
    PlatformType platformType = platform.getPlatformType();
    Bureau bureau = this.getBureau();
    List<Sensor> sensors = platform.getPlatformHasSensorChildSensors();

    Collections.sort(sensors, (o1, o2) -> o1.getName().compareTo(o2.getName()));

    JSONArray array = sensors.stream().map(w -> {
      JSONObject obj = new JSONObject();
      obj.put(Sensor.OID, w.getOid());
      obj.put(Sensor.NAME, w.getName());

      return obj;
    }).collect(Collector.of(JSONArray::new, JSONArray::put, JSONArray::put));

    JSONObject obj = new JSONObject();
    obj.put(UAV.OID, this.getOid());
    obj.put(UAV.SERIALNUMBER, this.getSerialNumber());
    obj.put(UAV.FAANUMBER, this.getFaaNumber());
    obj.put(UAV.PLATFORM, platform.getName());
    obj.put(Platform.PLATFORMTYPE, platformType.getName());
    if (bureau != null)
    {
      obj.put(UAV.BUREAU, bureau.getDisplayLabel());
    }

    ServerOrganization organization = this.getServerOrganization();

    if (organization != null)
    {
      obj.put(UAV.ORGANIZATION, organization.getDisplayLabel().getValue());
    }

    obj.put("sensors", array);

    SessionIF session = Session.getCurrentSession();

    // This will happen under testing contexts
    if (session != null)
    {
      SingleActorDAOIF user = session.getUser();

      JSONObject pointOfContact = new JSONObject();
      pointOfContact.put("name", user.getValue(GeoprismUser.FIRSTNAME) + " " + user.getValue(GeoprismUser.LASTNAME));
      pointOfContact.put("email", user.getValue(GeoprismUser.EMAIL));

      obj.put("pointOfContact", pointOfContact);
    }

    return obj;
  }

  public static List<UAV> getAll()
  {
    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(UAV.CLASS);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName());

    GraphQuery<UAV> query = new GraphQuery<UAV>(statement.toString());

    return query.getResults();
  }

  public static Long getCount()
  {
    GraphPageQuery<UAV> query = new GraphPageQuery<UAV>(UAV.CLASS);

    return query.getCount();
  }

  public static Page<UAVPageView> getPage(JSONObject criteria)
  {
    UAVPageQuery query = new UAVPageQuery(criteria);

    return query.getPage();
  }

  public static boolean isPlatformReferenced(Platform platform)
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(UAV.CLASS);
    MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(UAV.PLATFORM);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT COUNT(*) FROM " + mdVertex.getDBClassName() + "");
    statement.append(" WHERE " + mdAttribute.getColumnName() + " = :platform");

    final GraphQuery<Long> query = new GraphQuery<Long>(statement.toString());
    query.setParameter("platform", platform.getRID());

    Long result = query.getSingleResult();

    return ( result != null && result > 0 );
  }

  public static List<UAV> search(String text, String field)
  {
    if (text != null)
    {
      final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(UAV.CLASS);
      MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(field);

      if (mdAttribute != null)
      {
        StringBuilder statement = new StringBuilder();
        statement.append("SELECT FROM " + mdVertex.getDBClassName() + "");
        statement.append(" WHERE " + mdAttribute.getColumnName() + ".toUpperCase() LIKE :text");
        statement.append(" ORDER BY " + mdAttribute.getColumnName());

        final GraphQuery<UAV> query = new GraphQuery<UAV>(statement.toString());
        query.setParameter("text", "%" + text.toUpperCase() + "%");

        return query.getResults();
      }
      else
      {
        throw new GenericException("Unable to search on field [" + field + "]");
      }
    }

    return new LinkedList<UAV>();
  }
}
