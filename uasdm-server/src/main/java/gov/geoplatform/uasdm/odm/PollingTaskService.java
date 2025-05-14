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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTaskQuery;
import gov.geoplatform.uasdm.odm.ODMTaskProcessor.TaskResult;
import gov.geoplatform.uasdm.odm.ODMTaskProcessor.TaskStatus;

public class PollingTaskService implements TaskService
{
  private static Logger logger = LoggerFactory.getLogger(PollingTaskService.class);

  private final Integer ODM_STATUS_UPDATE_INTERVAL = 5000; // in
                                                           // miliseconds

  private ODMStatusThread statusThread;

  private List<S3ResultsUploadThread> uploadThreads = new ArrayList<S3ResultsUploadThread>();

  public synchronized void startup()
  {
    if (statusThread != null)
    {
      return;
    }

    statusThread = new ODMStatusThread("ODM Status Server");
    statusThread.setDaemon(true);

    restartRunningJobs();

    statusThread.start();
  }

  public synchronized void shutdown()
  {
    if (statusThread == null)
    {
      return;
    }

    statusThread.interrupt();

    for (Thread t : uploadThreads)
    {
      if (t != null)
      {
        t.interrupt();
      }
    }
  }

  /**
   * Subscribes a task for polling updates from the ODM processing server.
   * 
   * @param task
   */
  public void addTask(ODMProcessingTaskIF task)
  {
    statusThread.addTask(task);
  }

  private void restartRunningJobs()
  {
    AbstractWorkflowTaskQuery query = new AbstractWorkflowTaskQuery(new QueryFactory());
    query.WHERE(query.getStatus().EQ(ODMStatus.RUNNING.getLabel()).OR(query.getStatus().EQ(ODMStatus.QUEUED.getLabel()).OR(query.getStatus().EQ(ODMStatus.NEW.getLabel())).OR(query.getStatus().EQ("Processing"))));

    try (OIterator<? extends AbstractWorkflowTask> it = query.getIterator())
    {
      while (it.hasNext())
      {
        AbstractWorkflowTask task = it.next();

        // Heads up: Imagery
        if (task instanceof ODMProcessingTaskIF)
        {
          ODMProcessingTaskIF processingTask = (ODMProcessingTaskIF) task;

          statusThread.addTask(processingTask);
        }
        else if (task instanceof ODMUploadTask)
        {
          ODMUploadTask uploadTask = (ODMUploadTask) task;

          S3ResultsUploadThread thread = new S3ResultsUploadThread("S3Uploader for " + uploadTask.getOdmUUID(), uploadTask);
          thread.setDaemon(true);
          uploadThreads.add(thread);
          thread.start();
        }
        else
        {
          task.appLock();
          task.setStatus(ODMStatus.FAILED.getLabel());
          task.setMessage("The server was shut down while the task was running.");
          task.apply();
        }
      }
    }
  }

  private class ODMStatusThread extends Thread
  {
    /**
     * A list of tasks that require status updates.
     */
    private List<ODMProcessingTaskIF> activeTasks = new ArrayList<ODMProcessingTaskIF>();

    /**
     * A way to add additional tasks that require status updates to our list.
     */
    private List<ODMProcessingTaskIF> pendingTasks = new ArrayList<ODMProcessingTaskIF>();

    protected ODMStatusThread(String name)
    {
      super(name);
    }

    protected void addTask(ODMProcessingTaskIF task)
    {
      synchronized (pendingTasks)
      {
        pendingTasks.add(task);
      }
    }

    @Override
    public void run()
    {
      while (!Thread.interrupted())
      {
        try
        {
          // We want to make sure that we are sleeping OUTSIDE of any request because we don't want to hold onto a DB connection
          Thread.sleep(ODM_STATUS_UPDATE_INTERVAL);

//          runInRequest();
        }
        catch (InterruptedException t)
        {
          return;
        }
        catch (Throwable t)
        {
          logger.error("ODM status server encountered an error!", t);
        }
      }
    }

    @Request
    public void runInRequest() throws InterruptedException
    {
      runInTrans();
    }

    // @Transaction
    public void runInTrans() throws InterruptedException
    {
      synchronized (pendingTasks)
      {
        activeTasks.addAll(pendingTasks);
        pendingTasks.clear();
      }

      Iterator<ODMProcessingTaskIF> it = activeTasks.iterator();

      while (it.hasNext())
      {
        if (Thread.interrupted())
        {
          Thread.currentThread().interrupt();
          throw new InterruptedException();
        }

        ODMProcessingTaskIF task = it.next();

        TaskResult result = new ODMTaskProcessor().handleProcessingTask(task);

        if (!result.getStatus().equals(TaskStatus.ACTIVE))
        {
          it.remove();
        }

        if (result.getStatus().equals(TaskStatus.UPLOAD))
        {
          ODMUploadTaskIF uploadTask = result.getDownstreamTask();

          S3ResultsUploadThread thread = new S3ResultsUploadThread("S3Uploader for " + uploadTask.getOdmUUID(), uploadTask);
          uploadThreads.add(thread);
          thread.start();
        }
      }
    }
  }

  private class S3ResultsUploadThread extends Thread
  {
    private ODMUploadTaskIF uploadTask;

    protected S3ResultsUploadThread(String name, ODMUploadTaskIF uploadTask)
    {
      super(name);

      this.uploadTask = uploadTask;
    }

    @Override
    public void run()
    {
      try
      {
        new ODMTaskProcessor().handleUploadTask(this.uploadTask);
      }
      finally
      {
        uploadThreads.remove(this);
      }
    }

  }
}
