package gov.geoplatform.uasdm.odm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public interface Response
{

  boolean isJSONArray();

  boolean isJSONObject();

  JSONObject getJSONObject() throws JSONException;

  JSONArray getJSONArray() throws JSONException;

  String getResponse();

  void setResponse(String response);

  int getStatusCode();

  void setStatusCode(int statusCode);

  boolean isError();

  boolean isUnreachableHost();

}