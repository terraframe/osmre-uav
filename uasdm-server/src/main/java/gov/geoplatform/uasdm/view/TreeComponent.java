package gov.geoplatform.uasdm.view;

import org.json.JSONObject;

public interface TreeComponent
{
  public JSONObject toJSON();

  public String getId();

  public void addChild(TreeComponent child);
}
