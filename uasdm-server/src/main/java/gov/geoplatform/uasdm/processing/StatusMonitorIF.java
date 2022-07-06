package gov.geoplatform.uasdm.processing;

import java.util.List;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask.WorkflowTaskStatus;

public interface StatusMonitorIF
{
  public WorkflowTaskStatus getStatus();
  
  public void setStatus(WorkflowTaskStatus status);
  
  public String getMessage();
  
  public void setMessage(String message);
  
  public void addWarning(String warning);
  
  public void addError(String error);
  
  public List<String> getErrors();
  
  public List<String> getWarnings();
}
