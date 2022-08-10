package gov.geoplatform.uasdm.mock;

import gov.geoplatform.uasdm.odm.TaskRemoveResponse;

public class MockTaskRemoveResponse extends MockODMResponse implements TaskRemoveResponse
{

  @Override
  public boolean isSuccess()
  {
    return true;
  }
}
