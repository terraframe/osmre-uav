package gov.geoplatform.uasdm.odm;

import gov.geoplatform.uasdm.odm.ODMTaskProcessor.TaskResult;
import gov.geoplatform.uasdm.odm.ODMTaskProcessor.TaskStatus;

public class OnceTaskService implements TaskService
{
  public synchronized void startup()
  {
  }

  public synchronized void shutdown()
  {
  }

  public void addTask(ODMProcessingTaskIF task)
  {
    ODMTaskProcessor processor = new ODMTaskProcessor();

    TaskResult result = processor.handleProcessingTask(task);

    if (result.getStatus().equals(TaskStatus.UPLOAD))
    {
      ODMUploadTaskIF uploadTask = result.getDownstreamTask();

      processor.handleUploadTask(uploadTask);
    }
  }
}
