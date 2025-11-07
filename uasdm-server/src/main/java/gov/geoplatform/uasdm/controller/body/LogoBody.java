package gov.geoplatform.uasdm.controller.body;

import org.springframework.web.multipart.MultipartFile;

public class LogoBody
{
  private String        oid;

  private MultipartFile file;

  public String getOid()
  {
    return oid;
  }

  public void setOid(String oid)
  {
    this.oid = oid;
  }

  public MultipartFile getFile()
  {
    return file;
  }

  public void setFile(MultipartFile file)
  {
    this.file = file;
  }

}
