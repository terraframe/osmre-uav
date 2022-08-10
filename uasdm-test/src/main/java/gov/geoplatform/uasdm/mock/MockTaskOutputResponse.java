package gov.geoplatform.uasdm.mock;

import org.json.JSONArray;

import gov.geoplatform.uasdm.odm.TaskOutputResponse;

public class MockTaskOutputResponse extends MockODMResponse implements TaskOutputResponse
{

  @Override
  public Boolean hasOutput()
  {
    return true;
  }

  @Override
  public JSONArray getOutput()
  {
    return new JSONArray();
  }
}
