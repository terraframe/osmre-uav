package gov.geoplatform.uasdm.view;

import org.json.JSONObject;

public class Option
{
  private String value;

  private String label;

  public Option()
  {
  }

  public Option(String value, String label)
  {
    this.value = value;
    this.label = label;
  }

  public String getValue()
  {
    return value;
  }

  public void setValue(String value)
  {
    this.value = value;
  }

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }

  public JSONObject toJSON()
  {
    JSONObject object = new JSONObject();
    object.put("value", value);
    object.put("label", label);

    return object;
  }

}
