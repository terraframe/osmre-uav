package gov.geoplatform.uasdm.view;

import org.json.JSONObject;

public interface QueryResult
{
  public static enum Type {
    SITE, LOCATION
  }

  public JSONObject toJSON();
  
  public Type getType();


}
