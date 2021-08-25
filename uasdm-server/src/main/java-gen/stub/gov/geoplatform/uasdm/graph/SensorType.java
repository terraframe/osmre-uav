package gov.geoplatform.uasdm.graph;

import org.json.JSONArray;
import org.json.JSONObject;

import gov.geoplatform.uasdm.GenericException;
import gov.geoplatform.uasdm.model.Page;

public class SensorType extends SensorTypeBase implements Classification
{
  private static final long serialVersionUID = 826086552;

  public SensorType()
  {
    super();
  }

  @Override
  public void delete()
  {
    if (Sensor.isSensorTypeReferenced(this))
    {
      GenericException message = new GenericException();
      message.setUserMessage("The sensor type cannot be deleted because it is being used in a sensor");
      throw message;
    }

    super.delete();
  }

  @Override
  public JSONObject toJSON()
  {
    JSONObject object = new JSONObject();
    object.put(SensorType.OID, this.getOid());
    object.put(SensorType.CODE, this.getCode());
    object.put(SensorType.LABEL, this.getLabel());
    object.put(SensorType.ISMULTISPECTRAL, this.getIsMultispectral());

    if (this.getSeq() != null)
    {
      object.put(SensorType.SEQ, this.getSeq());
    }

    return object;
  }

  public static Long getCount()
  {
    return Classification.getCount(SensorType.CLASS);
  }

  public static Page<Classification> getPage(Integer pageNumber, Integer pageSize)
  {
    return Classification.getPage(SensorType.CLASS, pageNumber, pageSize);
  }

  public static JSONArray getAll()
  {
    return Classification.getAll(SensorType.CLASS);
  }

  public static SensorType fromJSON(JSONObject json)
  {
    SensorType classification = null;

    if (json.has(SensorType.OID))
    {
      String oid = json.getString(SensorType.OID);

      if (oid != null)
      {
        classification = SensorType.get(oid);
      }
    }

    if (classification == null)
    {
      classification = new SensorType();
    }

    classification.setCode(json.getString(SensorType.CODE));
    classification.setLabel(json.getString(SensorType.LABEL));

    if (json.has(SensorType.ISMULTISPECTRAL))
    {
      classification.setIsMultispectral(json.getBoolean(SensorType.ISMULTISPECTRAL));
    }
    else
    {
      classification.setIsMultispectral(Boolean.FALSE);
    }

    if (json.has(SensorType.SEQ))
    {
      classification.setSeq(json.getLong(SensorType.SEQ));
    }

    return classification;
  }
}
