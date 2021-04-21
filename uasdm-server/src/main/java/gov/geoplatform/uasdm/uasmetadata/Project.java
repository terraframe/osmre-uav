package gov.geoplatform.uasdm.uasmetadata;

public class Project
{
  private String name;
  
  private String shortName;
  
  private String description;
  
  private Integer restricted;
  
  private String sunsetDate;

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getShortName()
  {
    return shortName;
  }

  public void setShortName(String shortName)
  {
    this.shortName = shortName;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public Integer getRestricted()
  {
    return restricted;
  }

  public void setRestricted(Integer restricted)
  {
    this.restricted = restricted;
  }

  public String getSunsetDate()
  {
    return sunsetDate;
  }

  public void setSunsetDate(String sunsetDate)
  {
    this.sunsetDate = sunsetDate;
  }
}
