package gov.geoplatform.uasdm.graph;

import org.json.JSONArray;
import org.json.JSONObject;

import gov.geoplatform.uasdm.GenericException;
import gov.geoplatform.uasdm.model.Page;

public class PlatformType extends PlatformTypeBase implements Classification
{
  private static final long serialVersionUID = -1918394479;

  public PlatformType()
  {
    super();
  }

  @Override
  public void delete()
  {
    if (Platform.isPlatformTypeReferenced(this))
    {
      GenericException message = new GenericException();
      message.setUserMessage("The platform type cannot be deleted because it is being used in a platform");
      throw message;
    }

    super.delete();
  }

  public static Long getCount()
  {
    return Classification.getCount(PlatformType.CLASS);
  }

  public static Page<Classification> getPage(JSONObject criteria)
  {
    return Classification.getPage(PlatformType.CLASS, criteria);
  }

  public static JSONArray getAll()
  {
    return Classification.getAll(PlatformType.CLASS);
  }

  public static PlatformType fromJSON(JSONObject json)
  {
    PlatformType classification = null;

    if (json.has(PlatformType.OID))
    {
      String oid = json.getString(PlatformType.OID);

      if (oid != null)
      {
        classification = PlatformType.get(oid);
      }
    }

    if (classification == null)
    {
      classification = new PlatformType();
    }

    classification.setName(json.getString(PlatformType.NAME));

    if (json.has(PlatformType.SEQ))
    {
      classification.setSeq(json.getLong(PlatformType.SEQ));
    }

    return classification;
  }

}
