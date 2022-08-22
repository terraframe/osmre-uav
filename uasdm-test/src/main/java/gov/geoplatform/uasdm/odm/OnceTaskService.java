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
