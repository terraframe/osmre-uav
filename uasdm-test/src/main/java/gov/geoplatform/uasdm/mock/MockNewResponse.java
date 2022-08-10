package gov.geoplatform.uasdm.mock;

import java.util.UUID;

import gov.geoplatform.uasdm.odm.NewResponse;
import gov.geoplatform.uasdm.odm.Response;

public class MockNewResponse extends MockODMResponse implements NewResponse
{
  private String uuid;

  public MockNewResponse()
  {
    this.uuid = UUID.randomUUID().toString();
  }

  @Override
  public String getUUID()
  {
    return this.uuid;
  }
}
