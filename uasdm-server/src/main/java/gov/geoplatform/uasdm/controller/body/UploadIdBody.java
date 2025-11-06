package gov.geoplatform.uasdm.controller.body;

import org.hibernate.validator.constraints.NotBlank;

public class UploadIdBody
{
  @NotBlank
  private String uploadId;

  public String getUploadId()
  {
    return uploadId;
  }

  public void setUploadId(String uploadId)
  {
    this.uploadId = uploadId;
  }

}
