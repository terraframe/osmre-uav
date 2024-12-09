/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    return task.getActions().stream().filter(action -> TaskActionType.ERROR.getType().equals(action.getActionType())).map(action -> action.getDescription()).collect(Collectors.toList());
  }

  @Override
  public List<String> getWarnings()
  {
    return task.getActions().stream().filter(action -> TaskActionType.WARNING.getType().equals(action.getActionType())).map(action -> action.getDescription()).collect(Collectors.toList());
  }

}
