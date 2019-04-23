package gov.geoplatform.uasdm.odm;

public enum ODMStatus
{
  NEW("New", -1),
  QUEUED("Queued", 10),
  RUNNING("Running", 20),
  FAILED("Failed", 30),
  COMPLETED("Complete", 40),
  CANCELED("Canceled", 50);
  
  private String label;
  
  private Integer code;
  
  private ODMStatus(String label, Integer code)
  {
    this.label = label;
    this.code = code;
  }
  
  public String getLabel()
  {
    return this.label;
  }
  
  public Integer getCode()
  {
    return this.code;
  }
  
  public static ODMStatus getByCode(Integer code)
  {
    if (code == NEW.getCode() || code == null)
    {
      return NEW;
    }
    else if (code == QUEUED.getCode())
    {
      return QUEUED;
    }
    else if (code == RUNNING.getCode())
    {
      return RUNNING;
    }
    else if (code == FAILED.getCode())
    {
      return FAILED;
    }
    else if (code == COMPLETED.getCode())
    {
      return COMPLETED;
    }
    else if (code == CANCELED.getCode())
    {
      return CANCELED;
    }
    
    throw new UnsupportedOperationException("No status by code [" + code + "].");
  }
}
