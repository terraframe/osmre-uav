package gov.geoplatform.uasdm.model;

public class StacLocation
{
  private String label;

  private String uuid;

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }

  public String getUuid()
  {
    return uuid;
  }

  public void setUuid(String uuid)
  {
    this.uuid = uuid;
  }

  public static StacLocation build(String uuid, String label)
  {
    StacLocation location = new StacLocation();
    location.setUuid(uuid);
    location.setLabel(label);

    return location;
  }

}
