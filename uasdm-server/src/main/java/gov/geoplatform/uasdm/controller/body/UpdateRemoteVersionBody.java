package gov.geoplatform.uasdm.controller.body;

import javax.validation.constraints.NotNull;

public class UpdateRemoteVersionBody
{
  @NotNull
  String  oid;

  @NotNull
  String  versionId;

  @NotNull
  Integer versionNumber;

  public String getOid()
  {
    return oid;
  }

  public void setOid(String oid)
  {
    this.oid = oid;
  }

  public String getVersionId()
  {
    return versionId;
  }

  public void setVersionId(String versionId)
  {
    this.versionId = versionId;
  }

  public Integer getVersionNumber()
  {
    return versionNumber;
  }

  public void setVersionNumber(Integer versionNumber)
  {
    this.versionNumber = versionNumber;
  }
}