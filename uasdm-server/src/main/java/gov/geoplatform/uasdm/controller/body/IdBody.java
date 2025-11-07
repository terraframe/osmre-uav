package gov.geoplatform.uasdm.controller.body;

import org.hibernate.validator.constraints.NotEmpty;

public class IdBody
{
  @NotEmpty
  private String id;

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }
}