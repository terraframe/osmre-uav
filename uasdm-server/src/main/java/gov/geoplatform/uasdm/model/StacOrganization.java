package gov.geoplatform.uasdm.model;

public class StacOrganization
{
  private String label;

  private String code;

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }

  public String getCode()
  {
    return code;
  }

  public void setCode(String code)
  {
    this.code = code;
  }

  public static StacOrganization build(String code, String label)
  {
    StacOrganization location = new StacOrganization();
    location.setCode(code);
    location.setLabel(label);

    return location;
  }

}
