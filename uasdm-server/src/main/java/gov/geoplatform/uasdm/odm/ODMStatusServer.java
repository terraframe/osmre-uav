package gov.geoplatform.uasdm.odm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

public class ODMStatusServer
{
  private static final Integer ODM_STATUS_UPDATE_INTERVAL = 5000; // in miliseconds
  
  private static ODMStatusThread statusThread;
  
  private static Logger logger = LoggerFactory.getLogger(ODMStatusServer.class);
  
  public synchronized static void startup()
  {
    if (statusThread != null)
    {
      return;
    }
    
    statusThread = new ODMStatusThread("ODM Status Server");
    
    populateActiveTasks();
    
    statusThread.start();
  }
  
  public synchronized static void shutdown()
  {
    if (statusThread == null)
    {
      return;
    }
    
    statusThread.interrupt();
  }
  
  /**
   * Subscribes a task for polling updates from the ODM processing server.
   * 
   * @param task
   */
  public static void addTask(ODMProcessingTask task)
  {
    statusThread.addTask(task);
  }
  
  private static void populateActiveTasks()
  {
    ODMProcessingTaskQuery query = new ODMProcessingTaskQuery(new QueryFactory());
    query.WHERE(query.getStatus().EQ(ODMStatus.RUNNING.getLabel()).OR(query.getStatus().EQ(ODMStatus.QUEUED.getLabel()).OR(query.getStatus().EQ(ODMStatus.NEW.getLabel()))));

    OIterator<? extends ODMProcessingTask> it = query.getIterator();

    try
    {
      if (it.hasNext())
      {
        statusThread.addTask(it.next());
      }
    }
    finally
    {
      it.close();
    }
  }

  private static class ODMStatusThread extends Thread
  {
    /**
     * A list of tasks that require status updates.
     */
    private List<ODMProcessingTask> activeTasks = new ArrayList<ODMProcessingTask>();
    
    /**
     * A way to add additional tasks that require status updates to our list.
     */
    private List<ODMProcessingTask> pendingTasks = new ArrayList<ODMProcessingTask>();
    
    protected ODMStatusThread(String name)
    {
      super(name);
    }
    
    protected void addTask(ODMProcessingTask task)
    {
      synchronized(pendingTasks)
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
          Thread.sleep(ODM_STATUS_UPDATE_INTERVAL);
          
          runInRequest();
        }
        catch (InterruptedException e)
        {
          return;
        }
      }
    }
    
    @Request
    public void runInRequest()
    {
      runInTrans();
    }
    
    @Transaction
    public void runInTrans()
    {
      synchronized(pendingTasks)
      {
        activeTasks.addAll(pendingTasks);
        pendingTasks.clear();
      }
      
      Iterator<ODMProcessingTask> it = activeTasks.iterator();
      
      while(it.hasNext())
      {
        ODMProcessingTask task = it.next();
        
        InfoResponse resp = ODMFacade.taskInfo(task.getOdmUUID());
        
        if (resp.getHTTPResponse().isUnreachableHost())
        {
          String msg = "Unable to reach ODM server. code: " + resp.getHTTPResponse().getStatusCode() + " response: " + resp.getHTTPResponse().getResponse();
          logger.error(msg);
          UnreachableHostException ex = new UnreachableHostException(msg);
          
          task.appLock();
          task.setStatus(ODMStatus.FAILED.getLabel());
          task.setMessage(ex.getLocalizedMessage());
          task.apply();
          
          it.remove();
          
//          throw ex;
          continue;
        }
        else if (resp.hasError())
        {
          task.appLock();
          task.setStatus(ODMStatus.FAILED.getLabel());
          task.setMessage(resp.getError());
          task.apply();
          
          it.remove();
        }
        else if (!resp.hasError() && resp.getHTTPResponse().isError())
        {
          task.appLock();
          task.setStatus(ODMStatus.FAILED.getLabel());
          task.setMessage("The job encountered an unspecified error.");
          task.apply();
          
          it.remove();
        }
        else
        {
          if (resp.getStatus().equals(ODMStatus.RUNNING))
          {
            task.appLock();
            task.setStatus(resp.getStatus().getLabel());
            
            long millis = resp.getProcessingTime();
            
            String sProcessingTime = String.format("%d hours, %d minutes", 
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) - 
                TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(millis))
            );
            
            task.setMessage("Processing of " + resp.getImagesCount() + " images has been running for " + sProcessingTime);
            task.apply();
          }
          else if (resp.getStatus().equals(ODMStatus.QUEUED))
          {
            task.appLock();
            task.setStatus(resp.getStatus().getLabel());
            task.setMessage("Job is queued."); // TODO : Position in queue?
            task.apply();
          }
          else if (resp.getStatus().equals(ODMStatus.CANCELED))
          {
            task.appLock();
            task.setStatus(resp.getStatus().getLabel());
            task.setMessage("Job was canceled.");
            task.apply();
            
            it.remove();
          }
          else if (resp.getStatus().equals(ODMStatus.FAILED))
          {
            task.appLock();
            task.setStatus(resp.getStatus().getLabel());
            task.setMessage(resp.getStatusError());
            addOutputToTask(task);
            task.apply();
            
            it.remove();
          }
          else if (resp.getStatus().equals(ODMStatus.COMPLETED))
          {
            long millis = resp.getProcessingTime();
            
            String sProcessingTime = String.format("%d hours, %d minutes", 
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) - 
                TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(millis))
            );
            
            task.appLock();
            task.setStatus(resp.getStatus().getLabel());
            task.setMessage("Processing of " + resp.getImagesCount() + " images completed in " + sProcessingTime);
            addOutputToTask(task);
            task.apply();
            
            it.remove();
          }
        }
      }
    }
    
    private void addOutputToTask(ODMProcessingTask task)
    {
      TaskOutputResponse resp = ODMFacade.taskOutput(task.getOdmUUID());
      
      if (resp.hasOutput())
      {
        JSONArray output = resp.getOutput();
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < output.length(); ++i)
        {
          sb.append(output.getString(i));
          
          if (i > 0)
          {
            sb.append("&#13;&#10;");
          }
        }
        
        task.setOdmOutput(sb.toString());
      }
    }
  }
}
