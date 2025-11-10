package gov.geoplatform.uasdm.controller.body;

import javax.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;

public class UploadArtifactFileBody extends EntityArtifactBody
{
  @NotNull
  private MultipartFile file;

  public MultipartFile getFile()
  {
    return file;
  }

  public void setFile(MultipartFile file)
  {
    this.file = file;
  }
}
