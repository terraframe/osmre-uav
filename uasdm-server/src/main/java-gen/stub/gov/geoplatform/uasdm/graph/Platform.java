package gov.geoplatform.uasdm.graph;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collector;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;

import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.model.JSONSerializable;
import gov.geoplatform.uasdm.model.Page;

public class Platform extends PlatformBase implements JSONSerializable
{
  private static final long serialVersionUID = -62034328;

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

    super.apply();
  }

  @Override
  public JSONObject toJSON()
  {
    JSONObject object = new JSONObject();
    object.put(Platform.OID, this.getOid());
    object.put(Platform.CODE, this.getCode());
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

    String platformType = this.getObjectValue(Platform.PLATFORMTYPE);

    if (platformType != null)
    {
      object.put(Platform.PLATFORMTYPE, platformType);
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

    platform.setCode(UUID.randomUUID().toString());
    platform.setName(json.getString(Platform.NAME));
    platform.setDescription(json.getString(Platform.DESCRIPTION));

    if (json.has(Platform.PLATFORMTYPE))
    {
      String oid = json.getString(Platform.PLATFORMTYPE);

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

    if (!isNew)
    {
      List<Sensor> sensors = platform.getPlatformHasSensorChildSensors();

      for (Sensor sensor : sensors)
      {
        if (set.contains(sensor.getOid()))
        {
          set.remove(sensor.getOid());
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
    }

    return platform;
  }

  public static Long getCount()
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Platform.CLASS);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT COUNT(*) FROM " + mdVertex.getDBClassName() + "");

    final GraphQuery<Long> query = new GraphQuery<Long>(statement.toString());

    return query.getSingleResult();
  }

  public static Page<Platform> getPage(Integer pageNumber, Integer pageSize)
  {
    final Long count = Platform.getCount();

    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Platform.CLASS);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName() + "");
    statement.append(" ORDER BY code");
    statement.append(" SKIP " + ( ( pageNumber - 1 ) * pageSize ) + " LIMIT " + pageSize);

    final GraphQuery<Platform> query = new GraphQuery<Platform>(statement.toString());

    return new Page<Platform>(count, pageNumber, pageSize, query.getResults());
  }

}
