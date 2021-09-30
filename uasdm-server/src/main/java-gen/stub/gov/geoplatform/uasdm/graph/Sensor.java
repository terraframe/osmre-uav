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

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collector;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;

import gov.geoplatform.uasdm.GenericException;
import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.bus.CollectionReport;
import gov.geoplatform.uasdm.model.JSONSerializable;
import gov.geoplatform.uasdm.model.Page;

public class Sensor extends SensorBase implements JSONSerializable
{
  private static final long serialVersionUID = 1045467848;

  public Sensor()
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
      CollectionReport.update(this);
    }
  }

  @Override
  public void delete()
  {
    CollectionReport.handleDelete(this);

    super.delete();
  }

  public SensorType getSensorType()
  {
    return SensorType.get(this.getObjectValue(SENSORTYPE));
  }

  @Override
  public JSONObject toJSON()
  {
    JSONObject object = new JSONObject();
    object.put(Sensor.OID, this.getOid());
    object.put(Sensor.NAME, this.getName());
    object.put(Sensor.DESCRIPTION, this.getDescription());
    object.put(Sensor.MODEL, this.getModel());
    object.put(Sensor.PIXELSIZEHEIGHT, this.getRealPixelSizeHeight());
    object.put(Sensor.PIXELSIZEWIDTH, this.getRealPixelSizeWidth());
    object.put(Sensor.SENSORHEIGHT, this.getRealSensorHeight());
    object.put(Sensor.SENSORWIDTH, this.getRealSensorWidth());

    if (this.getDateCreated() != null)
    {
      object.put(Sensor.DATECREATED, Util.formatIso8601(this.getDateCreated(), false));
    }

    if (this.getDateUpdated() != null)
    {
      object.put(Sensor.DATEUPDATED, Util.formatIso8601(this.getDateUpdated(), false));
    }

    String sensorType = this.getObjectValue(Sensor.SENSORTYPE);

    if (sensorType != null)
    {
      object.put(Sensor.SENSORTYPE, sensorType);
    }

    if (this.getSeq() != null)
    {
      object.put(Sensor.SEQ, this.getSeq());
    }

    List<WaveLength> wavelengths = this.getSensorHasWaveLengthChildWaveLengths();

    object.put("wavelengths", (JSONArray) wavelengths.stream().map(w -> w.getOid()).collect(Collector.of(JSONArray::new, JSONArray::put, JSONArray::put)));

    List<Platform> platforms = this.getPlatformHasSensorParentPlatforms();

    JSONArray jsonArray = platforms.stream().map(w -> {
      JSONObject obj = new JSONObject();
      obj.put(Platform.OID, w.getOid());
      obj.put(Platform.NAME, w.getName());
      return obj;
    }).collect(Collector.of(JSONArray::new, JSONArray::put, JSONArray::put));

    object.put("platforms", jsonArray);

    return object;
  }

  public JSONObject toView()
  {
    SensorType sensorType = this.getSensorType();

    JSONObject object = new JSONObject();
    object.put(Sensor.OID, this.getOid());
    object.put(Sensor.NAME, this.getName());
    object.put(Sensor.DESCRIPTION, this.getDescription());
    object.put(Sensor.MODEL, this.getModel());
    object.put(Sensor.PIXELSIZEHEIGHT, this.getRealPixelSizeHeight());
    object.put(Sensor.PIXELSIZEWIDTH, this.getRealPixelSizeWidth());
    object.put(Sensor.SENSORHEIGHT, this.getRealSensorHeight());
    object.put(Sensor.SENSORWIDTH, this.getRealSensorWidth());
    object.put(Sensor.SENSORTYPE, sensorType.getName());

    List<WaveLength> wavelengths = this.getSensorHasWaveLengthChildWaveLengths();

    object.put("wavelengths", (JSONArray) wavelengths.stream().map(w -> {
      JSONObject obj = new JSONObject();
      obj.put(WaveLength.OID, w.getOid());
      obj.put(WaveLength.NAME, w.getName());
      return obj;
    }).collect(Collector.of(JSONArray::new, JSONArray::put, JSONArray::put)));

    return object;
  }

  @Transaction
  public static Sensor apply(JSONObject json)
  {
    Sensor sensor = null;

    if (json.has(Sensor.OID))
    {
      String oid = json.getString(Sensor.OID);

      if (oid != null)
      {
        sensor = Sensor.get(oid);
      }
    }

    if (sensor == null)
    {
      sensor = new Sensor();
    }

    sensor.setName(json.getString(Sensor.NAME));

    sensor.setRealPixelSizeHeight(new BigDecimal(json.getDouble(Sensor.PIXELSIZEHEIGHT)));
    sensor.setRealPixelSizeWidth(new BigDecimal(json.getDouble(Sensor.PIXELSIZEWIDTH)));
    sensor.setRealSensorHeight(new BigDecimal(json.getDouble(Sensor.SENSORHEIGHT)));
    sensor.setRealSensorWidth(new BigDecimal(json.getDouble(Sensor.SENSORWIDTH)));
    sensor.setDescription(json.has(Sensor.DESCRIPTION) ? json.getString(Sensor.DESCRIPTION) : null);
    sensor.setModel(json.has(Sensor.MODEL) ? json.getString(Sensor.MODEL) : null);

    if (json.has(Sensor.SENSORTYPE))
    {
      String oid = json.getString(Sensor.SENSORTYPE);

      sensor.setSensorType(SensorType.get(oid));
    }
    else
    {
      sensor.setSensorType(null);
    }

    if (json.has(Sensor.SEQ))
    {
      sensor.setSeq(json.getLong(Sensor.SEQ));
    }

    boolean isNew = sensor.isNew();

    sensor.apply();

    JSONArray array = json.getJSONArray("wavelengths");

    Set<String> set = new TreeSet<String>();

    for (int i = 0; i < array.length(); i++)
    {
      set.add(array.getString(i));
    }

    // Assign wavelength edges
    int count = 0;

    if (!isNew)
    {
      List<WaveLength> wavelengths = sensor.getSensorHasWaveLengthChildWaveLengths();

      for (WaveLength wavelength : wavelengths)
      {
        if (set.contains(wavelength.getOid()))
        {
          set.remove(wavelength.getOid());

          count++;
        }
        else
        {
          sensor.removeSensorHasWaveLengthChild(wavelength);
        }
      }
    }

    for (String oid : set)
    {
      sensor.addSensorHasWaveLengthChild(WaveLength.get(oid)).apply();

      count++;
    }

    if (count == 0)
    {
      GenericException exception = new GenericException();
      exception.setUserMessage("A sensor must have at least one wavelength");
      throw exception;
    }

    return sensor;
  }

  public static Long getCount()
  {
    GraphPageQuery<Sensor> query = new GraphPageQuery<Sensor>(Sensor.CLASS);

    return query.getCount();
  }

  public static Page<SensorPageView> getPage(JSONObject criteria)
  {
    SensorPageQuery query = new SensorPageQuery(criteria);

    return query.getPage();
  }

  public static List<Sensor> getAll()
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Sensor.CLASS);
    MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(Sensor.NAME);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName() + "");
    statement.append(" ORDER BY " + mdAttribute.getColumnName());

    final GraphQuery<Sensor> query = new GraphQuery<Sensor>(statement.toString());

    return query.getResults();
  }

  public static JSONArray getAllJson()
  {
    return getAll().stream().map(w -> {
      JSONObject obj = new JSONObject();
      obj.put(Sensor.OID, w.getOid());
      obj.put(Sensor.NAME, w.getName());

      return obj;
    }).collect(Collector.of(JSONArray::new, JSONArray::put, JSONArray::put));
  }

  public static boolean isSensorTypeReferenced(SensorType type)
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Sensor.CLASS);
    MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(Sensor.SENSORTYPE);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT COUNT(*) FROM " + mdVertex.getDBClassName() + "");
    statement.append(" WHERE " + mdAttribute.getColumnName() + " = :sensorType");

    final GraphQuery<Long> query = new GraphQuery<Long>(statement.toString());
    query.setParameter("sensorType", type.getRID());

    Long result = query.getSingleResult();

    return ( result != null && result > 0 );
  }

}
