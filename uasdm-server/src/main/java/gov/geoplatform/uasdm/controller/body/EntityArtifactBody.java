package gov.geoplatform.uasdm.controller.body;

import org.hibernate.validator.constraints.NotBlank;

public class EntityArtifactBody extends EntityProductBody
{

  @NotBlank
  private String folder;

  public String getFolder()
  {
    return folder;
  }

  public void setFolder(String folder)
  {
    this.folder = folder;
  }

}
