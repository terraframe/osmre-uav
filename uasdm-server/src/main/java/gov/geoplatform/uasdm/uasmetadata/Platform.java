package gov.geoplatform.uasdm.uasmetadata;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Platform
{
  private String name;
  
  @JsonProperty("class")
  private String platformClass;

  private String type;

  private String serialNumber;

  private String faaIdNumber;

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getPlatformClass()
  {
    return platformClass;
  }

  public void setPlatformClass(String platformClass)
  {
    this.platformClass = platformClass;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String getSerialNumber()
  {
    return serialNumber;
  }

  public void setSerialNumber(String serialNumber)
  {
    this.serialNumber = serialNumber;
  }

  public String getFaaIdNumber()
  {
    return faaIdNumber;
  }

  public void setFaaIdNumber(String faaIdNumber)
  {
    this.faaIdNumber = faaIdNumber;
  }
}