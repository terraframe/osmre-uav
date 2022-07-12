package gov.geoplatform.uasdm.processing;

import java.util.List;
import java.util.stream.Collectors;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask.TaskActionType;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask.WorkflowTaskStatus;

public class WorkflowTaskMonitor implements StatusMonitorIF
{

  private AbstractWorkflowTask task;
  
  public WorkflowTaskMonitor(AbstractWorkflowTask task)
  {
    this.task = task;
  }
  
  public AbstractWorkflowTask getWorkflowTask()
  {
    return this.task;
  }
  
  public void setWorkflowTask(AbstractWorkflowTask task)
  {
    this.task = task;
  }
  
  @Override
  public WorkflowTaskStatus getStatus()
  {
    return WorkflowTaskStatus.valueOf(this.task.getStatus());
  }

  @Override
  public void setStatus(WorkflowTaskStatus status)
  {
    task.lock();
    task.setStatus(status.name());
    task.apply();
  }

  @Override
  public String getMessage()
  {
    return this.task.getMessage();
  }

  @Override
  public void setMessage(String message)
  {
    task.lock();
    task.setMessage(message);
    task.apply();
  }

  @Override
  public void addWarning(String warning)
  {
    task.createAction(warning, TaskActionType.WARNING.getType());
  }

  @Override
  public void addError(String error)
  {
    task.createAction(error, TaskActionType.ERROR.getType());
  }

  @Override
  public List<String> getErrors()
  {
    return task.getActions().stream().filter(action -> TaskActionType.ERROR.getType().equals(action.getType())).map(action -> action.getDescription()).collect(Collectors.toList());
  }

  @Override
  public List<String> getWarnings()
  {
    return task.getActions().stream().filter(action -> TaskActionType.WARNING.getType().equals(action.getType())).map(action -> action.getDescription()).collect(Collectors.toList());
  }

}
