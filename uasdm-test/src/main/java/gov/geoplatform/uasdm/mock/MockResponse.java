package gov.geoplatform.uasdm.mock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gov.geoplatform.uasdm.odm.Response;

public class MockResponse implements Response
{
  private int statusCode = 200;

  private String response = "Mock Message";

  @Override
  public boolean isJSONArray()
  {
    return false;
  }

  @Override
  public boolean isJSONObject()
  {
    return false;
  }

  @Override
  public JSONObject getJSONObject() throws JSONException
  {
    return null;
  }

  @Override
  public JSONArray getJSONArray() throws JSONException
  {
    return null;
  }

  @Override
  public String getResponse()
  {
    return this.response;
  }

  @Override
  public void setResponse(String response)
  {
    this.response = response;
  }

  @Override
  public int getStatusCode()
  {
    return this.statusCode;
  }

  @Override
  public void setStatusCode(int statusCode)
  {
    this.statusCode = statusCode;
  }

  @Override
  public boolean isError()
  {
    return false;
  }

  @Override
  public boolean isUnreachableHost()
  {
    return false;
  }

}
