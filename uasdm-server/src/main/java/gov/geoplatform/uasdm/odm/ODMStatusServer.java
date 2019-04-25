package gov.geoplatform.uasdm.odm;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.bus.Collection;
import gov.geoplatform.uasdm.bus.UasComponent;
import net.geoprism.GeoprismUser;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class ODMStatusServer
{
  private static final Integer ODM_STATUS_UPDATE_INTERVAL = 5000; // in miliseconds
  
  private static ODMStatusThread statusThread;
  
  private static List<S3ResultsUploadThread> uploadThreads = new ArrayList<S3ResultsUploadThread>();
  
  private static Logger logger = LoggerFactory.getLogger(ODMStatusServer.class);
  
  /**
   * !Test code!
   */
//  public static void main(String[] args)
//  {
//    mainInReq();
//  }
//  
//  @Request
//  private static void mainInReq()
//  {
//    ODMProcessingTaskQuery query = new ODMProcessingTaskQuery(new QueryFactory());
//    
//    query.WHERE(query.getOdmUUID().EQ("68719e1d-0be3-4e6f-8063-878a83335eb3"));
//    
//    OIterator<? extends ODMProcessingTask> it = query.getIterator();
//    
//    ODMProcessingTask task = it.next();
//    
//    S3ResultsUploadThread thread = new S3ResultsUploadThread("S3Uploader for " + task.getOdmUUID(), task);
//    thread.setfolder(new File("/home/rich/dev/data/odm/aukerman/all-dem-reduced"));
//    thread.start();
//    
//    while (!Thread.interrupted())
//    {
//      try
//      {
//        Thread.sleep(1000);
//      }
//      catch (InterruptedException e)
//      {
//        return;
//      }
//    }
//  }
  
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
    
    for (Thread t : uploadThreads)
    {
      t.interrupt();
    }
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
    /**
     * Queue processing tasks
     */
    ODMProcessingTaskQuery query = new ODMProcessingTaskQuery(new QueryFactory());
    query.WHERE(query.getStatus().EQ(ODMStatus.RUNNING.getLabel()).OR(query.getStatus().EQ(ODMStatus.QUEUED.getLabel()).OR(query.getStatus().EQ(ODMStatus.NEW.getLabel()))));

    OIterator<? extends ODMProcessingTask> it = query.getIterator();

    try
    {
      while (it.hasNext())
      {
        statusThread.addTask(it.next());
      }
    }
    finally
    {
      it.close();
    }
    
    /**
     * Restart any uploads that were in progress
     */
    ODMUploadTaskQuery uploadQuery = new ODMUploadTaskQuery(new QueryFactory());
    uploadQuery.WHERE(uploadQuery.getStatus().EQ(ODMStatus.RUNNING.getLabel()).OR(uploadQuery.getStatus().EQ(ODMStatus.QUEUED.getLabel()).OR(uploadQuery.getStatus().EQ(ODMStatus.NEW.getLabel()))));

    OIterator<? extends ODMUploadTask> it2 = uploadQuery.getIterator();

    try
    {
      while (it2.hasNext())
      {
        ODMUploadTask uploadTask = it2.next();
        
        S3ResultsUploadThread thread = new S3ResultsUploadThread("S3Uploader for " + uploadTask.getOdmUUID(), uploadTask);
        uploadThreads.add(thread);
        thread.start();
      }
    }
    finally
    {
      it2.close();
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
    
//    @Transaction
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
        if (Thread.interrupted())
        {
          Thread.currentThread().interrupt();
          return;
        }
        
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
            
            uploadResultsToS3(task);
          }
        }
      }
    }
    
    private void uploadResultsToS3(ODMProcessingTask task)
    {
      ODMUploadTask uploadTask = new ODMUploadTask();
      uploadTask.setUpLoadId(task.getUpLoadId());
      uploadTask.setCollectionId(task.getCollectionOid());
      uploadTask.setGeoprismUser(task.getGeoprismUser());
      uploadTask.setOdmUUID(task.getOdmUUID());
      uploadTask.setStatus(ODMStatus.RUNNING.getLabel());
      uploadTask.setTaskLabel("Uploading Orthorectification Artifacts");
      uploadTask.setMessage("The results of the Orthorectification processing are being uploaded to S3. Check back later for updates.");
      uploadTask.apply();
      
      S3ResultsUploadThread thread = new S3ResultsUploadThread("S3Uploader for " + uploadTask.getOdmUUID(), uploadTask);
      uploadThreads.add(thread);
      thread.start();
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
  
  private static class S3ResultsUploadThread extends Thread
  {
    private ODMUploadTask task;
    
    private File zip;
    
    private File unzippedParentFolder;
    
    protected S3ResultsUploadThread(String name, ODMUploadTask uploadTask)
    {
      super(name);
      
      this.task = uploadTask;
      zip = ODMFacade.taskDownload(uploadTask.getOdmUUID());
      unzippedParentFolder = new File(FileUtils.getTempDirectory(), "odm-" + uploadTask.getOdmUUID());
    }
    
    /**
     * Useful for testing with a zip not directly fetched from ODM.
     */
    private void setfolder(File folder)
    {
      this.unzippedParentFolder = folder;
    }
    
    @Override
    public void run()
    {
      runInRequest();
    }
    
    @Request
    public void runInRequest()
    {
      try
      {
        runInTrans();
        
        task.lock();
        task.setStatus(ODMStatus.COMPLETED.getLabel());
        task.setMessage("The upload successfully completed.  All files except those mentioned were archived.");
        task.apply();
      }
      catch (Throwable t)
      {
        t.printStackTrace();
        
        task.lock();
        task.setStatus(ODMStatus.FAILED.getLabel());
        task.setMessage("The upload failed. " + t.getLocalizedMessage());
        task.apply();
      }
    }
    
//    @Transaction
    public void runInTrans() throws ZipException
    {
      HashMap<String, String> destMap = new HashMap<String, String>();
      destMap.put("potree_pointcloud", Collection.PTCLOUD);
      destMap.put("odm_orthophoto", Collection.ORTHO);
      destMap.put("odm_dem", Collection.DEM);
      
      try
      {
        new ZipFile(zip).extractAll(unzippedParentFolder.getAbsolutePath());
        
        for (String zipSubFolder : destMap.keySet())
        {
          if (Thread.interrupted())
          {
            Thread.currentThread().interrupt();
            return;
          }
          
          String collectionSubFolderName = destMap.get(zipSubFolder);
          
          File parentDir = new File(unzippedParentFolder, zipSubFolder);
          
          if (parentDir.exists())
          {
            uploadAllChildren(parentDir, collectionSubFolderName);
          }
          else
          {
            task.createAction("ODM did not produce any " + collectionSubFolderName + " files.", "error");
          }
        }
      }
      finally
      {
        FileUtils.deleteQuietly(zip);
        FileUtils.deleteQuietly(unzippedParentFolder);
      }
    }

    private void uploadAllChildren(File parentDir, String s3KeyPrefix)
    {
      File[] children = parentDir.listFiles();
      for (File child : children)
      {
        if (Thread.interrupted())
        {
          Thread.currentThread().interrupt();
          return;
        }
        
        String name = child.getName();
        
        if (!child.isDirectory() && UasComponent.isValidName(name))
        {
          String key = task.getCollection().getS3location() + s3KeyPrefix + "/" + name;
  
          try
          {
            TransferManager tx = new TransferManager(new ClasspathPropertiesFileCredentialsProvider());
  
            try
            {
              Upload myUpload = tx.upload(AppProperties.getBucketName(), key, child);
  
              if (myUpload.isDone() == false)
              {
                logger.info("Source: " + child.getAbsolutePath());
                logger.info("Destination: " + myUpload.getDescription());
                
                task.lock();
                task.setMessage(myUpload.getDescription());
                task.apply();
              }
  
              myUpload.addProgressListener(new ProgressListener()
              {
                int count = 0;
  
                @Override
                public void progressChanged(ProgressEvent progressEvent)
                {
                  if (count % 2000 == 0)
                  {
                    long total = myUpload.getProgress().getTotalBytesToTransfer();
                    long current = myUpload.getProgress().getBytesTransferred();
  
                    logger.info(current + "/" + total + "-" + ( (int) ( (double) current / total * 100 ) ) + "%");
  
                    count = 0;
                  }
  
                  count++;
                }
              });
  
              myUpload.waitForCompletion();
            }
            finally
            {
              tx.shutdownNow();
            }
  
            // TODO : Solr?
//                SolrService.updateOrCreateDocument(ancestors, this, key, name);
          }
          catch (InterruptedException e)
          {
            return;
          }
          catch (Exception e)
          {
            task.createAction(e.getMessage(), "error");
            logger.error(e.getMessage());
          }
        }
        else if (child.isDirectory())
        {
          uploadAllChildren(child, s3KeyPrefix + "/" + child.getName());
        }
        else
        {
          task.createAction("The filename [" + name + "] is invalid", "error");
        }
      }
    }
  }
}
