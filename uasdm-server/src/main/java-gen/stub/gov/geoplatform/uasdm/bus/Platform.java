package gov.geoplatform.uasdm.bus;

import org.json.JSONObject;

import com.runwaysdk.query.QueryFactory;

public class Platform extends PlatformBase
{
  private static final long serialVersionUID = 1790259303;

  private static String     NEW_INSTANCE     = "newInstance";

  public Platform()
  {
    super();
  }

  public JSONObject toJSON()
  {
    JSONObject object = new JSONObject();
    object.put(NEW_INSTANCE, this.isNew());
    object.put(Platform.NAME, this.getName());
    object.put(Platform.DISPLAYLABEL, this.getDisplayLabel());

    if (!this.isNew() || this.isAppliedToDB())
    {
      object.put(Platform.OID, this.getOid());
    }

    return object;
  }

  public static PlatformQuery page(Integer pageSize, Integer pageNumber)
  {
    PlatformQuery query = getAll();
    query.restrictRows(pageSize, pageNumber);

    return query;
  }

  public static PlatformQuery getAll()
  {
    PlatformQuery query = new PlatformQuery(new QueryFactory());
    query.ORDER_BY_DESC(query.getDisplayLabel());

    return query;
  }

  public static Platform fromJSON(JSONObject json)
  {
    Platform platform = json.getBoolean(NEW_INSTANCE) ? new Platform() : Platform.get(json.getString(Platform.OID));
    platform.setDisplayLabel(json.getString(Platform.DISPLAYLABEL));
    platform.setName(json.getString(Platform.NAME));

    return platform;
  }
}
