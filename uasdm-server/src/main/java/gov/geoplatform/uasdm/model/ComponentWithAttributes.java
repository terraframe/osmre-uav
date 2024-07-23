package gov.geoplatform.uasdm.model;

public interface ComponentWithAttributes
{
  public void setValue(String name, Object value);

  public <T> T getObjectValue(String name);

  public void apply();

}
