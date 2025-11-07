package gov.geoplatform.uasdm.controller.body;

import org.hibernate.validator.constraints.NotBlank;

public class ParentIdBody
{
  @NotBlank
  private String parentId;

  public String getParentId()
  {
    return parentId;
  }

  public void setParentId(String parentId)
  {
    this.parentId = parentId;
  }
}
