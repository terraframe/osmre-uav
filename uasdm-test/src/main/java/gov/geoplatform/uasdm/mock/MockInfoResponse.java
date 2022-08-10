package gov.geoplatform.uasdm.mock;

import java.util.Date;
import java.util.UUID;

import org.json.JSONObject;

import gov.geoplatform.uasdm.odm.InfoResponse;
import gov.geoplatform.uasdm.odm.ODMStatus;

public class MockInfoResponse extends MockODMResponse implements InfoResponse
{
  private String uuid;

  public MockInfoResponse()
  {
    this.uuid = UUID.randomUUID().toString();
  }

  @Override
  public String getUUID()
  {
    return this.uuid;
  }

  @Override
  public Date getDateCreated()
  {
    return new Date();
  }

  @Override
  public Long getProcessingTime()
  {
    return 1L;
  }

  @Override
  public Long getImagesCount()
  {
    return 1L;
  }

  @Override
  public JSONObject getOptions()
  {
    return new JSONObject();
  }

  @Override
  public ODMStatus getStatus()
  {
    return ODMStatus.COMPLETED;
  }

  @Override
  public String getStatusError()
  {
    return "Mock Error message";
  }
}
