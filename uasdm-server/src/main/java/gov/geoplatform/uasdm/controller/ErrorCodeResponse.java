package gov.geoplatform.uasdm.controller;

import com.runwaysdk.mvc.AbstractRestResponse;
import com.runwaysdk.mvc.ResponseIF;

public class ErrorCodeResponse extends AbstractRestResponse implements ResponseIF
{
  private String message;

  public ErrorCodeResponse(int status, String message)
  {
    super(status);

    this.message = message;
  }

  @Override
  protected Object serialize()
  {
    return this.message;
  }
}
