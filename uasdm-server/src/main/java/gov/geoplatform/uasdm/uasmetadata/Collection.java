package gov.geoplatform.uasdm.uasmetadata;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "Collect")
public class Collection
{
  private String name;
  
  private String description;
  
  private String exifIncluded;
  
  private Double northBound;
  
  private Double eastBound;
  
  private Double southBound;
  
  private Double westBound;
  
  private Date acquisitionDateStart;
  
  private Date acquisitionDateEnd;

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getExifIncluded()
  {
    return exifIncluded;
  }

  public void setExifIncluded(String exifIncluded)
  {
    this.exifIncluded = exifIncluded;
  }

  public Double getNorthBound()
  {
    return northBound;
  }

  public void setNorthBound(Double northBound)
  {
    this.northBound = northBound;
  }

  public Double getEastBound()
  {
    return eastBound;
  }

  public void setEastBound(Double eastBound)
  {
    this.eastBound = eastBound;
  }

  public Double getSouthBound()
  {
    return southBound;
  }

  public void setSouthBound(Double southBound)
  {
    this.southBound = southBound;
  }

  public Double getWestBound()
  {
    return westBound;
  }

  public void setWestBound(Double westBound)
  {
    this.westBound = westBound;
  }

  public Date getAcquisitionDateStart()
  {
    return acquisitionDateStart;
  }

  public void setAcquisitionDateStart(Date acquisitionDateStart)
  {
    this.acquisitionDateStart = acquisitionDateStart;
  }

  public Date getAcquisitionDateEnd()
  {
    return acquisitionDateEnd;
  }

  public void setAcquisitionDateEnd(Date acquisitionDateEnd)
  {
    this.acquisitionDateEnd = acquisitionDateEnd;
  }
}
