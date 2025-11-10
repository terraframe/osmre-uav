package gov.geoplatform.uasdm.service.request;

import org.json.JSONArray;
import org.json.JSONObject;

public interface CrudService
{
  public JSONObject page(String sessionId, JSONObject criteria);

  public JSONArray getAll(String sessionId);

  public JSONObject apply(String sessionId, JSONObject json);

  public void remove(String sessionId, String oid);

  public JSONObject get(String sessionId, String oid);

  public JSONObject newInstance(String sessionId);
}