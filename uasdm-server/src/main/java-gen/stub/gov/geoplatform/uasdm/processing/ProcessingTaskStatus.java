package gov.geoplatform.uasdm.processing;

public enum ProcessingTaskStatus {
  NEW("New"),
  QUEUED("Queued"),
  RUNNING("Running"),
  FAILED("Failed"),
  COMPLETED("Complete"),
  CANCELED("Canceled");
  
  private String label;
  
  private ProcessingTaskStatus(String label)
  {
    this.label = label;
  }
  
  public String getLabel()
  {
    return this.label;
  }
}
