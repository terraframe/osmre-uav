package gov.geoplatform.uasdm.controller.body;

import org.hibernate.validator.constraints.NotBlank;

public class RunProcessBody
{
  @NotBlank
  private String id;

  @NotBlank
  private String configuration;

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public String getConfiguration()
  {
    return configuration;
  }

  public void setConfiguration(String configuration)
  {
    this.configuration = configuration;
  }

}
