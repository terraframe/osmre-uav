package gov.geoplatform.uasdm.graph;

import org.json.JSONArray;
import org.json.JSONObject;

import gov.geoplatform.uasdm.model.Page;

public class PlatformManufacturer extends PlatformManufacturerBase implements Classification
{
  private static final long serialVersionUID = 1353153000;

  public PlatformManufacturer()
  {
    super();
  }

  public static Long getCount()
  {
    return Classification.getCount(PlatformManufacturer.CLASS);
  }

  public static Page<Classification> getPage(Integer pageNumber, Integer pageSize)
  {
    return Classification.getPage(PlatformManufacturer.CLASS, pageNumber, pageSize);
  }

  public static JSONArray getAll()
  {
    return Classification.getAll(PlatformManufacturer.CLASS);
  }

  public static PlatformManufacturer fromJSON(JSONObject json)
  {
    PlatformManufacturer classification = null;

    if (json.has(PlatformManufacturer.OID))
    {
      String oid = json.getString(PlatformManufacturer.OID);

      if (oid != null)
      {
        classification = PlatformManufacturer.get(oid);
      }
    }

    if (classification == null)
    {
      classification = new PlatformManufacturer();
    }

    classification.setCode(json.getString(PlatformManufacturer.CODE));
    classification.setLabel(json.getString(PlatformManufacturer.LABEL));

    if (json.has(PlatformManufacturer.SEQ))
    {
      classification.setSeq(json.getLong(PlatformManufacturer.SEQ));
    }

    return classification;
  }

}