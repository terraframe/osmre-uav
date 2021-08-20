package gov.geoplatform.uasdm.graph;

import org.json.JSONArray;
import org.json.JSONObject;

import gov.geoplatform.uasdm.model.Page;

public class WaveLength extends WaveLengthBase implements Classification
{
  private static final long serialVersionUID = 920922499;

  public WaveLength()
  {
    super();
  }

  public static Long getCount()
  {
    return Classification.getCount(WaveLength.CLASS);
  }

  public static Page<Classification> getPage(Integer pageNumber, Integer pageSize)
  {
    return Classification.getPage(WaveLength.CLASS, pageNumber, pageSize);
  }

  public static JSONArray getAll()
  {
    return Classification.getAll(WaveLength.CLASS);
  }

  public static WaveLength fromJSON(JSONObject json)
  {
    WaveLength classification = null;

    if (json.has(WaveLength.OID))
    {
      String oid = json.getString(WaveLength.OID);

      if (oid != null)
      {
        classification = WaveLength.get(oid);
      }
    }

    if (classification == null)
    {
      classification = new WaveLength();
    }

    classification.setCode(json.getString(WaveLength.CODE));
    classification.setLabel(json.getString(WaveLength.LABEL));

    if (json.has(WaveLength.SEQ))
    {
      classification.setSeq(json.getLong(WaveLength.SEQ));
    }

    return classification;
  }

}
