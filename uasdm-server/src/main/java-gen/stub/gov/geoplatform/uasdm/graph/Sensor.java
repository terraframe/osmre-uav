package gov.geoplatform.uasdm.graph;

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

import gov.geoplatform.uasdm.Util;
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

    super.apply();
  }

  @Override
  public JSONObject toJSON()
  {
    JSONObject object = new JSONObject();
    object.put(Sensor.OID, this.getOid());
    object.put(Sensor.NAME, this.getName());
    object.put(Sensor.DESCRIPTION, this.getDescription());
    object.put(Sensor.PIXELSIZEHEIGHT, this.getPixelSizeHeight());
    object.put(Sensor.PIXELSIZEWIDTH, this.getPixelSizeWidth());
    object.put(Sensor.SENSORHEIGHT, this.getSensorHeight());
    object.put(Sensor.SENSORWIDTH, this.getSensorWidth());

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
    sensor.setDescription(json.getString(Sensor.DESCRIPTION));
    sensor.setPixelSizeHeight(json.getInt(Sensor.PIXELSIZEHEIGHT));
    sensor.setPixelSizeWidth(json.getInt(Sensor.PIXELSIZEWIDTH));
    sensor.setSensorHeight(json.getInt(Sensor.SENSORHEIGHT));
    sensor.setSensorWidth(json.getInt(Sensor.SENSORWIDTH));

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

    if (!isNew)
    {
      List<WaveLength> wavelengths = sensor.getSensorHasWaveLengthChildWaveLengths();

      for (WaveLength wavelength : wavelengths)
      {
        if (set.contains(wavelength.getOid()))
        {
          set.remove(wavelength.getOid());
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
    }

    return sensor;
  }

  public static Long getCount()
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Sensor.CLASS);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT COUNT(*) FROM " + mdVertex.getDBClassName() + "");

    final GraphQuery<Long> query = new GraphQuery<Long>(statement.toString());

    return query.getSingleResult();
  }

  public static Page<Sensor> getPage(Integer pageNumber, Integer pageSize)
  {
    final Long count = Sensor.getCount();

    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Sensor.CLASS);
    MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(Sensor.NAME);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName() + "");
    statement.append(" ORDER BY " + mdAttribute.getColumnName());
    statement.append(" SKIP " + ( ( pageNumber - 1 ) * pageSize ) + " LIMIT " + pageSize);

    final GraphQuery<Sensor> query = new GraphQuery<Sensor>(statement.toString());

    return new Page<Sensor>(count, pageNumber, pageSize, query.getResults());
  }

  public static JSONArray getAll()
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Sensor.CLASS);
    MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(Sensor.NAME);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName() + "");
    statement.append(" ORDER BY " + mdAttribute.getColumnName());

    final GraphQuery<Sensor> query = new GraphQuery<Sensor>(statement.toString());

    List<Sensor> results = query.getResults();

    return results.stream().map(w -> {
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
