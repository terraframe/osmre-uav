package gov.geoplatform.uasdm.controller.body;

import org.hibernate.validator.constraints.NotEmpty;

public class UserAccessBody
{
  @NotEmpty
  private String componentId;

  @NotEmpty
  private String identifier;

  public String getComponentId()
  {
    return componentId;
  }

  public void setComponentId(String componentId)
  {
    this.componentId = componentId;
  }

  public String getIdentifier()
  {
    return identifier;
  }

  public void setIdentifier(String identifier)
  {
    this.identifier = identifier;
  }

}