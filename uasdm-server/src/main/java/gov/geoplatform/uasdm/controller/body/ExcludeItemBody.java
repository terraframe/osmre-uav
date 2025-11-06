package gov.geoplatform.uasdm.controller.body;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

public class ExcludeItemBody
{
  @NotBlank
  private String  id;

  @NotNull
  private Boolean exclude;

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public Boolean getExclude()
  {
    return exclude;
  }

  public void setExclude(Boolean exclude)
  {
    this.exclude = exclude;
  }

}
