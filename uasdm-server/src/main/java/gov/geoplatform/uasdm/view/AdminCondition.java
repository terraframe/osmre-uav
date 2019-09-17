package gov.geoplatform.uasdm.view;

import org.json.JSONObject;

public class AdminCondition implements Condition
{
  @Override
  public JSONObject toJSON()
  {
    JSONObject object = new JSONObject();
    object.put("type", "admin");

    return object;
  }
}
