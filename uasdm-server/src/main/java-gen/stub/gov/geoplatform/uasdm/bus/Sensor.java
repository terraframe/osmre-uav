package gov.geoplatform.uasdm.bus;

import org.json.JSONObject;

import com.runwaysdk.query.QueryFactory;

public class Sensor extends SensorBase
{
  private static final long serialVersionUID = -1299753426;

  private static String     NEW_INSTANCE     = "newInstance";

  public Sensor()
  {
    super();
  }

  public JSONObject toJSON()
  {
    JSONObject object = new JSONObject();
    object.put(NEW_INSTANCE, this.isNew());
    object.put(Sensor.NAME, this.getName());
    object.put(Sensor.DISPLAYLABEL, this.getDisplayLabel());

    if (!this.isNew() || this.isAppliedToDB())
    {
      object.put(Sensor.OID, this.getOid());
    }

    return object;
  }

  public static SensorQuery page(Integer pageSize, Integer pageNumber)
  {
    SensorQuery query = getAll();
    query.restrictRows(pageSize, pageNumber);

    return query;
  }

  public static SensorQuery getAll()
  {
    SensorQuery query = new SensorQuery(new QueryFactory());
    query.ORDER_BY_DESC(query.getDisplayLabel());

    return query;
  }

  public static Sensor fromJSON(JSONObject json)
  {
    Sensor sensor = json.getBoolean(NEW_INSTANCE) ? new Sensor() : Sensor.get(json.getString(Sensor.OID));
    sensor.setDisplayLabel(json.getString(Sensor.DISPLAYLABEL));
    sensor.setName(json.getString(Sensor.NAME));

    return sensor;
  }
}
