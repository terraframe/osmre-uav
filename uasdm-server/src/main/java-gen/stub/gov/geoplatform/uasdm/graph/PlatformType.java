package gov.geoplatform.uasdm.graph;

import org.json.JSONArray;
import org.json.JSONObject;

import gov.geoplatform.uasdm.model.Page;

public class PlatformType extends PlatformTypeBase implements Classification
{
  private static final long serialVersionUID = -1918394479;

  public PlatformType()
  {
    super();
  }

  public static Long getCount()
  {
    return Classification.getCount(PlatformType.CLASS);
  }

  public static Page<Classification> getPage(Integer pageNumber, Integer pageSize)
  {
    return Classification.getPage(PlatformType.CLASS, pageNumber, pageSize);
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

    classification.setCode(json.getString(PlatformType.CODE));
    classification.setLabel(json.getString(PlatformType.LABEL));

    if (json.has(PlatformType.SEQ))
    {
      classification.setSeq(json.getLong(PlatformType.SEQ));
    }

    return classification;
  }

}