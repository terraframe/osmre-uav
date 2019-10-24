package gov.geoplatform.uasdm.bus;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import gov.geoplatform.uasdm.bus.Sensor.WaveLength;

public class Sensor extends SensorBase
{
  private static final long   serialVersionUID = -1299753426;

  public static final String  OTHER            = "OTHER";

  private static final String NEW_INSTANCE     = "newInstance";
  
  public static enum WaveLength
  {
	  NATURAL_COLOR_RGB("Natural Color RGB"),
	  THERMAL("Thermal"),
	  RED_EDGE("Red Edge"),
	  NEAR_INFRA_RED("Near Infra Red"),
	  LIDAR("LiDAR"),
	  OTHER("Other");
	  
	  private String code;
	  
	  private WaveLength(String code)
	  {
		  this.code = code;
	  }
	  
	  public String getCode()
	  {
		  return this.code;
	  }
	  
	  public static WaveLength getFromCode(String code)
	  {
	    if (code.equals(NATURAL_COLOR_RGB.getCode()))
	    {
	      return NATURAL_COLOR_RGB;
	    }
	    else if (code.equals(THERMAL.getCode()))
	    {
	      return THERMAL;
	    }
	    else if (code.equals(RED_EDGE.getCode()))
	    {
	      return RED_EDGE;
	    }
	    else if (code.equals(NEAR_INFRA_RED.getCode()))
	    {
	      return NEAR_INFRA_RED;
	    }
	    else if (code.equals(LIDAR.getCode()))
	    {
	      return LIDAR;
	    }
	    else
	    {
	      return OTHER;
	    }
	 }
  }

  public Sensor()
  {
    super();
  }

  public boolean isOther()
  {
    return this.getName().equals(OTHER);
  }
  
  public boolean isMultiSpectral()
  {
    List<WaveLength> wl = this.getWaveLengthList();
    
    if (wl.contains(WaveLength.RED_EDGE) || wl.contains(WaveLength.NEAR_INFRA_RED) || wl.contains(WaveLength.THERMAL))
    {
      return true;
    }
    
    return false;
  }
  
  public List<WaveLength> getWaveLengthList()
  {
    String saWL = this.getWaveLength();
    
    if (saWL == null || saWL.equals(""))
    {
      return new ArrayList<WaveLength>();
    }
    
    JSONArray jaWL = new JSONArray(saWL);
    ArrayList<WaveLength> tsaWL = new ArrayList<WaveLength>(jaWL.length());
    
    for (int i = 0; i < jaWL.length(); ++i)
    {
      String sWL = jaWL.getString(i);
      
      WaveLength wl = WaveLength.getFromCode(sWL);
      
      tsaWL.add(wl);
    }
    
    return tsaWL;
  }

  public JSONObject toJSON()
  {
    try
    {
      JSONObject object = new JSONObject();
      object.put(NEW_INSTANCE, this.isNew());
      object.put(Sensor.NAME, this.getName());
      object.put(Sensor.DISPLAYLABEL, this.getDisplayLabel());
      object.put(Sensor.SENSORTYPE, this.getSensorType());
      object.put(Sensor.MODEL, this.getModel());

      if (this.getWaveLength() != null && this.getWaveLength().length() > 0)
      {
        object.put(Sensor.WAVELENGTH, new JSONArray(this.getWaveLength()));
      }
      else
      {
        object.put(Sensor.WAVELENGTH, new JSONArray());
      }

      if (!this.isNew() || this.isAppliedToDB())
      {
        object.put(Sensor.OID, this.getOid());
      }

      return object;
    }
    catch (JSONException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public static SensorQuery page(Integer pageSize, Integer pageNumber)
  {
    SensorQuery query = getQuery();
    query.restrictRows(pageSize, pageNumber);

    return query;
  }

  public static SensorQuery getQuery()
  {
    SensorQuery query = new SensorQuery(new QueryFactory());
    query.ORDER_BY_ASC(query.getDisplayLabel());

    return query;
  }

  public static Sensor fromJSON(JSONObject json)
  {
    Sensor sensor = json.getBoolean(NEW_INSTANCE) ? new Sensor() : Sensor.get(json.getString(Sensor.OID));
    sensor.setDisplayLabel(json.getString(Sensor.DISPLAYLABEL));
    sensor.setName(json.getString(Sensor.NAME));
    sensor.setSensorType(json.getString(Sensor.SENSORTYPE));
    sensor.setModel(json.getString(Sensor.MODEL));
    sensor.setWaveLength(json.getJSONArray(Sensor.WAVELENGTH).toString());

    return sensor;
  }

  public static JSONObject toJSON(SensorQuery query)
  {
    JSONArray resultSet = new JSONArray();

    try (OIterator<? extends Sensor> it = query.getIterator())
    {
      for (Sensor sensor : it)
      {
        resultSet.put(sensor.toJSON());
      }
    }

    JSONObject json = new JSONObject();
    json.put("count", query.getCount());
    json.put("pageNumber", query.getPageNumber());
    json.put("pageSize", query.getPageSize());
    json.put("resultSet", resultSet);

    return json;
  }

  public static JSONArray getAll()
  {
    JSONArray array = new JSONArray();

    SensorQuery query = Sensor.getQuery();

    try (OIterator<? extends Sensor> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        array.put(iterator.next().toJSON());
      }
    }

    return array;
  }

}
