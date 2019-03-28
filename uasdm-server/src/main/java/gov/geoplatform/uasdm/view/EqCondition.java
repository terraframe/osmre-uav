package gov.geoplatform.uasdm.view;

import org.json.JSONObject;

public class EqCondition implements Condition
{
  private String name;

  private String value;

  public EqCondition(String name, String value)
  {
    super();
    this.name = name;
    this.value = value;
  }

  @Override
  public JSONObject toJSON()
  {
    JSONObject object = new JSONObject();
    object.put("name", name);
    object.put("value", value);
    object.put("type", "eq");

    return object;
  }

}
