package gov.geoplatform.uasdm.remote;

public class KnowStacNoopResponse implements KnowStacResponseIF
{

  @Override
  public boolean hasError()
  {
    return false;
  }

  @Override
  public String getError()
  {
    return "";
  }

}
