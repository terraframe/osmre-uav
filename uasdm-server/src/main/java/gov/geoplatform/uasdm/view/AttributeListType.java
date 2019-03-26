package gov.geoplatform.uasdm.view;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class AttributeListType extends AttributeType
{
  private List<Option> options;

  @Override
  protected String getType()
  {
    return "list";
  }

  public List<Option> getOptions()
  {
    return options;
  }

  public void setOptions(List<Option> options)
  {
    this.options = options;
  }

  @Override
  public JSONObject toJSON()
  {
    JSONArray array = new JSONArray();

    if (this.options != null)
    {
      for (Option option : this.options)
      {
        array.put(option.toJSON());
      }
    }

    JSONObject object = super.toJSON();
    object.put("options", array);

    return object;
  }
}
