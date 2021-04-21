package gov.geoplatform.uasdm.uasmetadata;

import java.util.Date;

public class Upload
{
  private String dataType;
  
  private String accessibleSupportType;
  
  private String inaccessibleSupportType;
  
  private Date orthoStartDate;
  
  private Date orthoEndDate;
  
  private String orthoCorrectionModel;
  
  private Date ptCloudStartDate;
  
  private Date ptCloudEndDate;
  
  private String ptCloudEpsgNumber;
  
  private Double rawFocalLength;

  public String getDataType()
  {
    return dataType;
  }

  public void setDataType(String dataType)
  {
    this.dataType = dataType;
  }

  public String getAccessibleSupportType()
  {
    return accessibleSupportType;
  }

  public void setAccessibleSupportType(String accessibleSupportType)
  {
    this.accessibleSupportType = accessibleSupportType;
  }

  public String getInaccessibleSupportType()
  {
    return inaccessibleSupportType;
  }

  public void setInaccessibleSupportType(String inaccessibleSupportType)
  {
    this.inaccessibleSupportType = inaccessibleSupportType;
  }

  public Date getOrthoStartDate()
  {
    return orthoStartDate;
  }

  public void setOrthoStartDate(Date orthoStartDate)
  {
    this.orthoStartDate = orthoStartDate;
  }

  public Date getOrthoEndDate()
  {
    return orthoEndDate;
  }

  public void setOrthoEndDate(Date orthoEndDate)
  {
    this.orthoEndDate = orthoEndDate;
  }

  public String getOrthoCorrectionModel()
  {
    return orthoCorrectionModel;
  }

  public void setOrthoCorrectionModel(String orthoCorrectionModel)
  {
    this.orthoCorrectionModel = orthoCorrectionModel;
  }

  public Date getPtCloudStartDate()
  {
    return ptCloudStartDate;
  }

  public void setPtCloudStartDate(Date ptCloudStartDate)
  {
    this.ptCloudStartDate = ptCloudStartDate;
  }

  public Date getPtCloudEndDate()
  {
    return ptCloudEndDate;
  }

  public void setPtCloudEndDate(Date ptCloudEndDate)
  {
    this.ptCloudEndDate = ptCloudEndDate;
  }

  public String getPtCloudEpsgNumber()
  {
    return ptCloudEpsgNumber;
  }

  public void setPtCloudEpsgNumber(String ptCloudEpsgNumber)
  {
    this.ptCloudEpsgNumber = ptCloudEpsgNumber;
  }

  public Double getRawFocalLength()
  {
    return rawFocalLength;
  }

  public void setRawFocalLength(Double rawFocalLength)
  {
    this.rawFocalLength = rawFocalLength;
  }
}
