package gov.geoplatform.uasdm.controller.body;

import org.hibernate.validator.constraints.NotEmpty;

public class PlatformBody
{
  @NotEmpty
  String platform;

  public String getPlatform()
  {
    return platform;
  }

  public void setPlatform(String platform)
  {
    this.platform = platform;
  }
}