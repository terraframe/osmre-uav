package gov.geoplatform.uasdm.mock;

import gov.geoplatform.uasdm.odm.ODMResponse;
import gov.geoplatform.uasdm.odm.Response;

public class MockODMResponse implements ODMResponse
{

  @Override
  public boolean hasError()
  {
    return false;
  }

  @Override
  public Response getHTTPResponse()
  {
    return new MockResponse();
  }

  @Override
  public String getError()
  {
    return "Mock error message";
  }

}
