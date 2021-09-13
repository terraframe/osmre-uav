package gov.geoplatform.uasdm.graph;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collector;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Session;

import gov.geoplatform.uasdm.GenericException;
import gov.geoplatform.uasdm.bus.Bureau;
import gov.geoplatform.uasdm.bus.CollectionReport;
import gov.geoplatform.uasdm.model.JSONSerializable;
import gov.geoplatform.uasdm.model.Page;
import net.geoprism.GeoprismUser;

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
      CollectionReport.update(this);
    }
  }

  @Override
  public void delete()
  {
    if (Collection.isUAVReferenced(this))
    {
      GenericException message = new GenericException();
      message.setUserMessage("The UAV cannot be deleted because it is being used in a Collection");
      throw message;
    }

    CollectionReport.handleDelete(this);

    super.delete();
  }

  public Platform getPlatform()
  {
    return Platform.get(this.getObjectValue(PLATFORM));
  }

  @Override
  public JSONObject toJSON()
  {
    JSONObject object = new JSONObject();
    object.put(UAV.OID, this.getOid());
    object.put(UAV.SERIALNUMBER, this.getSerialNumber());
    object.put(UAV.FAANUMBER, this.getFaaNumber());
    object.put(UAV.DESCRIPTION, this.getDescription());
    object.put(UAV.BUREAU, this.getBureauOid());

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

    JSONObject object = new JSONObject();
    object.put(UAV.OID, this.getOid());
    object.put(UAV.SERIALNUMBER, this.getSerialNumber());
    object.put(UAV.FAANUMBER, this.getFaaNumber());
    object.put(UAV.BUREAU, bureau.getName());
    object.put(Platform.NAME, platform.getName());
    object.put(Platform.PLATFORMTYPE, platformType.getLabel());
    object.put(Platform.MANUFACTURER, manufacturer.getLabel());

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
    uav.setBureauId(json.getString(UAV.BUREAU));

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
    SingleActorDAOIF user = Session.getCurrentSession().getUser();
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

    JSONObject pointOfContact = new JSONObject();
    pointOfContact.put("name", user.getValue(GeoprismUser.FIRSTNAME) + " " + user.getValue(GeoprismUser.LASTNAME));
    pointOfContact.put("email", user.getValue(GeoprismUser.EMAIL));

    JSONObject obj = new JSONObject();
    obj.put(UAV.OID, this.getOid());
    obj.put(UAV.SERIALNUMBER, this.getSerialNumber());
    obj.put(UAV.FAANUMBER, this.getFaaNumber());
    obj.put(UAV.PLATFORM, platform.getName());
    obj.put(Platform.PLATFORMTYPE, platformType.getLabel());
    obj.put(UAV.BUREAU, bureau.getDisplayLabel());
    obj.put("sensors", array);
    obj.put("pointOfContact", pointOfContact);

    return obj;
  }

  public static Long getCount()
  {
    GraphPageQuery<UAV> query = new GraphPageQuery<UAV>(UAV.CLASS);

    return query.getCount();
  }

  public static Page<UAV> getPage(JSONObject criteria)
  {
    GraphPageQuery<UAV> query = new GraphPageQuery<UAV>(UAV.CLASS, criteria);

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

  public static List<UAV> search(String text)
  {
    if (text != null)
    {
      final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(UAV.CLASS);
      MdAttributeDAOIF serialAttribute = mdVertex.definesAttribute(UAV.SERIALNUMBER);
      MdAttributeDAOIF faaAttribute = mdVertex.definesAttribute(UAV.FAANUMBER);

      StringBuilder statement = new StringBuilder();
      statement.append("SELECT FROM " + mdVertex.getDBClassName() + "");
      statement.append(" WHERE " + serialAttribute.getColumnName() + ".toUpperCase() LIKE :text");
      statement.append(" OR " + faaAttribute.getColumnName() + ".toUpperCase() LIKE :text");
      statement.append(" ORDER BY " + serialAttribute.getColumnName());

      final GraphQuery<UAV> query = new GraphQuery<UAV>(statement.toString());
      query.setParameter("text", "%" + text.toUpperCase() + "%");

      return query.getResults();
    }

    return new LinkedList<UAV>();
  }
}
