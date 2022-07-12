package gov.geoplatform.uasdm.processing;

import java.util.LinkedList;
import java.util.List;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask.WorkflowTaskStatus;

public class InMemoryMonitor implements StatusMonitorIF
{

  private WorkflowTaskStatus status = null;
  
  private String message = null;
  
  private List<String> warnings = new LinkedList<String>();
  
  private List<String> errors = new LinkedList<String>();
  
  @Override
  public WorkflowTaskStatus getStatus()
  {
    return this.status;
  }

  @Override
  public void setStatus(WorkflowTaskStatus status)
  {
    this.status = status;
  }

  @Override
  public String getMessage()
  {
    return this.message;
  }

  @Override
  public void setMessage(String message)
  {
    this.message = message;
  }

  @Override
  public void addWarning(String warning)
  {
    this.warnings.add(warning);
  }

  @Override
  public void addError(String error)
  {
    this.errors.add(error);
  }

  @Override
  public List<String> getErrors()
  {
    return this.errors;
  }

  @Override
  public List<String> getWarnings()
  {
    return this.warnings;
  }

}
