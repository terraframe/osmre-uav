package gov.geoplatform.uasdm.view;

import org.json.JSONObject;

import gov.geoplatform.uasdm.model.JSONSerializable;

public class JSONWrapper implements JSONSerializable
{
  private JSONObject object;

  public JSONWrapper(JSONObject object)
  {
    this.object = object;
  }

  @Override
  public Object toJSON()
  {
    return this.object;
  }

}
