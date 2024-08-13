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
package gov.geoplatform.uasdm.graph;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collector;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;

import gov.geoplatform.uasdm.GenericException;
import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.command.GenerateMetadataCommand;
import gov.geoplatform.uasdm.model.JSONSerializable;
import gov.geoplatform.uasdm.model.Page;
import gov.geoplatform.uasdm.processing.report.CollectionReportFacade;

public class Platform extends PlatformBase implements JSONSerializable
{
  private static final long  serialVersionUID  = -62034328;

  public static final String PLATFORM_TYPE_OID = "platformTypeOid";

  public Platform()
  {
    super();
  }

  @Override
  public void apply()
  {
    if (this.isNew())
    {
      this.setDateCreated(new Date());
    }

    this.setDateUpdated(new Date());

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

  public List<CollectionMetadata> getReferencingMetadata()
  {
    // SELECT from collection0 where collectionSensor IN (
    // select out('platform_has_sensor') from platform0)

    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(CollectionMetadata.CLASS);
    MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(CollectionMetadata.SENSOR);
    MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO("gov.geoplatform.uasdm.graph.PlatformHasSensor");

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName() + "");
    statement.append(" WHERE " + mdAttribute.getColumnName() + " IN (");
    statement.append("   SELECT OUT ('" + mdEdge.getDBClassName() + "') FROM :rid");
    statement.append(" )");

    final GraphQuery<CollectionMetadata> query = new GraphQuery<CollectionMetadata>(statement.toString());
    query.setParameter("rid", this.getRID());

    return query.getResults();
  }

  @Override
  @Transaction
  public void delete()
  {
    if (UAV.isPlatformReferenced(this))
    {
      GenericException message = new GenericException();
      message.setUserMessage("The platform cannot be deleted because it is being used in a UAV");
      throw message;
    }

    CollectionReportFacade.handleDelete(this).doIt();

    super.delete();
  }

  public PlatformType getPlatformType()
  {
    return PlatformType.get(this.getObjectValue(PLATFORMTYPE));
  }

  public PlatformManufacturer getManufacturer()
  {
    return PlatformManufacturer.get(this.getObjectValue(MANUFACTURER));
  }

  @Override
  public JSONObject toJSON()
  {
    JSONObject object = new JSONObject();
    object.put(Platform.OID, this.getOid());
    object.put(Platform.NAME, this.getName());
    object.put(Platform.DESCRIPTION, this.getDescription());

    if (this.getDateCreated() != null)
    {
      object.put(Platform.DATECREATED, Util.formatIso8601(this.getDateCreated(), false));
    }

    if (this.getDateUpdated() != null)
    {
      object.put(Platform.DATEUPDATED, Util.formatIso8601(this.getDateUpdated(), false));
    }

    PlatformType platformType = this.getPlatformType();

    if (platformType != null)
    {
      object.put(Platform.PLATFORM_TYPE_OID, platformType.getOid());
      object.put(Platform.PLATFORMTYPE, platformType.toJSON());
    }

    String manufacturer = this.getObjectValue(Platform.MANUFACTURER);

    if (manufacturer != null)
    {
      object.put(Platform.MANUFACTURER, manufacturer);
    }

    if (this.getSeq() != null)
    {
      object.put(Platform.SEQ, this.getSeq());
    }

    List<Sensor> sensors = this.getPlatformHasSensorChildSensors();

    JSONArray array = sensors.stream().map(w -> w.getOid()).collect(Collector.of(JSONArray::new, JSONArray::put, JSONArray::put));

    object.put("sensors", array);

    return object;
  }

  @Transaction
  public static Platform apply(JSONObject json)
  {
    Platform platform = null;

    if (json.has(Platform.OID))
    {
      String oid = json.getString(Platform.OID);

      if (oid != null)
      {
        platform = Platform.get(oid);
      }
    }

    if (platform == null)
    {
      platform = new Platform();
    }

    platform.setName(json.getString(Platform.NAME));
    platform.setDescription(json.has(Platform.DESCRIPTION) ? json.getString(Platform.DESCRIPTION) : null);

    if (json.has(PLATFORM_TYPE_OID))
    {
      String oid = json.getString(PLATFORM_TYPE_OID);

      platform.setPlatformType(PlatformType.get(oid));
    }
    else
    {
      platform.setPlatformType(null);
    }

    if (json.has(Platform.MANUFACTURER))
    {
      String oid = json.getString(Platform.MANUFACTURER);

      platform.setManufacturer(PlatformManufacturer.get(oid));
    }
    else
    {
      platform.setManufacturer(null);
    }

    if (json.has(Platform.SEQ))
    {
      platform.setSeq(json.getLong(Platform.SEQ));
    }

    boolean isNew = platform.isNew();

    platform.apply();

    JSONArray array = json.getJSONArray("sensors");

    Set<String> set = new TreeSet<String>();

    for (int i = 0; i < array.length(); i++)
    {
      set.add(array.getString(i));
    }

    // Assign sensor edges
    int count = 0;

    if (!isNew)
    {
      List<Sensor> sensors = platform.getPlatformHasSensorChildSensors();

      for (Sensor sensor : sensors)
      {
        if (set.contains(sensor.getOid()))
        {
          set.remove(sensor.getOid());

          count++;
        }
        else
        {
          platform.removePlatformHasSensorChild(sensor);
        }
      }
    }

    for (String oid : set)
    {
      platform.addPlatformHasSensorChild(Sensor.get(oid)).apply();

      count++;
    }

    if (count == 0)
    {
      GenericException exception = new GenericException();
      exception.setUserMessage("A platform must be assigned at least one sensor");
      throw exception;
    }

    return platform;
  }

  public static Long getCount()
  {
    GraphPageQuery<Platform> query = new GraphPageQuery<Platform>(Platform.CLASS);

    return query.getCount();
  }

  public static Page<Platform> getPage(JSONObject criteria)
  {
    GraphPageQuery<Platform> query = new GraphPageQuery<Platform>(Platform.CLASS, criteria);

    return query.getPage();
  }

  public static JSONArray getAll()
  {
    List<Platform> results = getAllPlatforms();

    return results.stream().map(w -> {
      JSONObject obj = new JSONObject();
      obj.put(Platform.OID, w.getOid());
      obj.put(Platform.NAME, w.getName());

      return obj;
    }).collect(Collector.of(JSONArray::new, JSONArray::put, JSONArray::put));
  }

  public static List<Platform> getAllPlatforms()
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Platform.CLASS);
    MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(Platform.NAME);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName() + "");
    statement.append(" ORDER BY " + mdAttribute.getColumnName());

    final GraphQuery<Platform> query = new GraphQuery<Platform>(statement.toString());

    return query.getResults();
  }

  public static boolean isPlatformTypeReferenced(PlatformType type)
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Platform.CLASS);
    MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(Platform.PLATFORMTYPE);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT COUNT(*) FROM " + mdVertex.getDBClassName() + "");
    statement.append(" WHERE " + mdAttribute.getColumnName() + " = :platformType");

    final GraphQuery<Long> query = new GraphQuery<Long>(statement.toString());
    query.setParameter("platformType", type.getRID());

    Long result = query.getSingleResult();

    return ( result != null && result > 0 );
  }

  public static boolean isPlatformManufacturerReferenced(PlatformManufacturer manufacturer)
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Platform.CLASS);
    MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(Platform.MANUFACTURER);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT COUNT(*) FROM " + mdVertex.getDBClassName() + "");
    statement.append(" WHERE " + mdAttribute.getColumnName() + " = :manufacturer");

    final GraphQuery<Long> query = new GraphQuery<Long>(statement.toString());
    query.setParameter("manufacturer", manufacturer.getRID());

    Long result = query.getSingleResult();

    return ( result != null && result > 0 );
  }

  public static List<Platform> search(String text)
  {
    if (text != null)
    {
      final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Platform.CLASS);
      MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(Platform.NAME);

      if (mdAttribute != null)
      {
        StringBuilder statement = new StringBuilder();
        statement.append("SELECT FROM " + mdVertex.getDBClassName() + "");
        statement.append(" WHERE " + mdAttribute.getColumnName() + ".toUpperCase() LIKE :text");
        statement.append(" ORDER BY " + mdAttribute.getColumnName());

        final GraphQuery<Platform> query = new GraphQuery<Platform>(statement.toString());
        query.setParameter("text", "%" + text.toUpperCase() + "%");

        return query.getResults();
      }
      else
      {
        throw new GenericException("Unable to search on field [" + Platform.NAME + "]");
      }
    }

    return new LinkedList<Platform>();
  }

}
