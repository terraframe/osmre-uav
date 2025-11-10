package gov.geoplatform.uasdm.controller.body;

import org.hibernate.validator.constraints.NotBlank;

public class RemoveUploadBody
{
  @NotBlank
  private String uploadUrl;

  public String getUploadUrl()
  {
    return uploadUrl;
  }

  public void setUploadUrl(String uploadUrl)
  {
    this.uploadUrl = uploadUrl;
  }
}