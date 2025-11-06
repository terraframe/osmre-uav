package gov.geoplatform.uasdm.controller.body;

import org.hibernate.validator.constraints.NotBlank;

public class ParentIdWithTypeBody extends ParentIdBody
{

  @NotBlank
  private String type;

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

}
