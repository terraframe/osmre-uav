package gov.geoplatform.uasdm.controller.body;

import org.hibernate.validator.constraints.NotEmpty;

public class MoveOrganizationBody
{
  @NotEmpty
  String code;

  @NotEmpty
  String parentCode;

  public String getCode()
  {
    return code;
  }

  public void setCode(String code)
  {
    this.code = code;
  }

  public String getParentCode()
  {
    return parentCode;
  }

  public void setParentCode(String parentCode)
  {
    this.parentCode = parentCode;
  }
}