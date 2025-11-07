package gov.geoplatform.uasdm.controller.body;

import org.hibernate.validator.constraints.NotBlank;

public class TaskIdBody
{
  @NotBlank
  private String taskId;

  public String getTaskId()
  {
    return taskId;
  }

  public void setTaskId(String taskId)
  {
    this.taskId = taskId;
  }

}
