package gov.geoplatform.uasdm.odm;

import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTaskIF;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTaskQuery;
import gov.geoplatform.uasdm.bus.Collection;
import gov.geoplatform.uasdm.bus.ImageryComponent;
import gov.geoplatform.uasdm.bus.UasComponent;
import gov.geoplatform.uasdm.service.SolrService;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.geoprism.EmailSetting;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

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
//    ODMUploadTaskQuery query = new ODMUploadTaskQuery(new QueryFactory());
//    
//    query.WHERE(query.getOid().EQ("d2aeebb8-e0fe-4486-b184-e07ebf0005d3"));
//    
//    OIterator<? extends ODMUploadTask> it = query.getIterator();
//    
//    ODMUploadTask task = it.next();
//    
//    S3ResultsUploadThread thread = new S3ResultsUploadThread("S3Uploader for " + task.getOdmUUID(), task, new File("/home/rich/dev/data/odm/aukerman/all-output-dem.zip"), true);
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
    statusThread.setDaemon(true);
    
    restartRunningJobs();
    
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
      if (t != null)
      {
        t.interrupt();
      }
    }
  }
  // ODMProcessingTaskIF
  // AbstractWorkflowTask
  private static void sendEmail(AbstractWorkflowTaskIF task)
  {
    try
    {
      if (task.getStatus().equals(ODMStatus.FAILED.getLabel()))
      {
        final String subject = "Orthorectification Processing Failed";
        
        final String body = "The orthorectification processing for [" + task.getComponentLabel() + "] has failed. " + task.getMessage();
        
        EmailSetting.sendEmail(subject, body, new String[]{task.getGeoprismUser().getEmail()});
      }
      else if (task.getStatus().equals(ODMStatus.COMPLETED.getLabel()))
      {
        final String subject = "Orthorectification Processing Completed";

        final String body = "The orthorectification processing for [" + task.getComponentLabel() + "] has completed. Log into the UASDM again to see the results.";
        
        EmailSetting.sendEmail(subject, body, new String[]{task.getGeoprismUser().getEmail()});
      }
      else
      {
        logger.error("Unable to send email of unknown task status [" + task.getStatus() + "].");
      }
    }
    catch (Throwable t)
    {
      logger.error("Problem sending email for task [" + task.getTaskLabel() + "] with status [" + task.getStatus() + "].", t);
      
      task.createAction("Problem occured while sending email. " + t.getLocalizedMessage(), "error");
    }
  }
  
  /**
   * Subscribes a task for polling updates from the ODM processing server.
   * 
   * @param task
   */
  public static void addTask(ODMProcessingTaskIF task)
  {
    statusThread.addTask(task);
  }
  
  private static void restartRunningJobs()
  {
    AbstractWorkflowTaskQuery query = new AbstractWorkflowTaskQuery(new QueryFactory());
    query.WHERE(query.getStatus().EQ(ODMStatus.RUNNING.getLabel()).OR(query.getStatus().EQ(ODMStatus.QUEUED.getLabel()).OR(query.getStatus().EQ(ODMStatus.NEW.getLabel())).OR(query.getStatus().EQ("Processing"))));

    OIterator<? extends AbstractWorkflowTask> it = query.getIterator();

    try
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
    finally
    {
      it.close();
    }
  }

  private static void removeFromOdm(AbstractWorkflowTaskIF task, String uuid)
  {
    try
    {
      TaskRemoveResponse resp = ODMFacade.taskRemove(uuid);
      
      if (!resp.isSuccess())
      {
        int code = resp.getHTTPResponse().getStatusCode();
        logger.error("Error occurred while removing task [" + uuid + "] [" + task.getTaskLabel() + "] from ODM. ODM returned status code [" + code + "]. " + code);
        task.createAction("Error occurred while cleaning up data from ODM. ODM returned status code [" + code + "].", "error");
      }
    }
    catch(Throwable t)
    {
      logger.error("Error occurred while removing task [" + uuid + "] [" + task.getTaskLabel() + "] from ODM.", t);
      
      task.createAction("Problem occured while cleaning up data from ODM. " + t.getLocalizedMessage(), "error");
    }
  }
  
  private static class ODMStatusThread extends Thread
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
        catch (Throwable t)
        {
          if (t instanceof InterruptedException)
          {
            return;
          }
          
          logger.error("ODM status server encountered an error!", t);
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
      
      Iterator<ODMProcessingTaskIF> it = activeTasks.iterator();
      
      while(it.hasNext())
      {
        if (Thread.interrupted())
        {
          Thread.currentThread().interrupt();
          return;
        }
        
        ODMProcessingTaskIF task = it.next();
        
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
          
          sendEmail(task);
          
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
          
          sendEmail(task);
        }
        else if (!resp.hasError() && resp.getHTTPResponse().isError())
        {
          task.appLock();
          task.setStatus(ODMStatus.FAILED.getLabel());
          task.setMessage("The job encountered an unspecified error.");
          task.apply();
          
          task.setOdmOutput("HTTP communication with ODM has failed [" + resp.getHTTPResponse().getStatusCode() + "]. " + resp.getHTTPResponse().getResponse());
          
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
            
            task.setMessage("Processing of " + resp.getImagesCount() + " images has been running for " + sProcessingTime + ". An email will be sent when the processing is complete");
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
            
            // TODO : Remove from ODM?
          }
          else if (resp.getStatus().equals(ODMStatus.FAILED))
          {
            task.appLock();
            task.setStatus(resp.getStatus().getLabel());
            task.setMessage(resp.getStatusError());
            addOutputToTask(task);
            task.apply();
            
            it.remove();
            
            sendEmail(task);
            
            removeFromOdm(task, task.getOdmUUID());
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
    
    private void uploadResultsToS3(ODMProcessingTaskIF task)
    {
      ODMUploadTaskIF uploadTask = null;

      if (task instanceof ImageryODMProcessingTask)
      {
        ImageryODMUploadTask imageryOdmUploadTask = new ImageryODMUploadTask();
        imageryOdmUploadTask.setUpLoadId(task.getUpLoadId());
        imageryOdmUploadTask.setImageryId(task.getImageryComponentOid());
        imageryOdmUploadTask.setGeoprismUser(task.getGeoprismUser());
        imageryOdmUploadTask.setOdmUUID(task.getOdmUUID());
        imageryOdmUploadTask.setStatus(ODMStatus.RUNNING.getLabel());
        imageryOdmUploadTask.setProcessingTask(task);
//        imageryOdmUploadTask.setTaskLabel("Uploading Orthorectification Artifacts for [" + task.getCollection().getName() + "].");
        imageryOdmUploadTask.setTaskLabel("UAV data orthorectification upload for collection [" + task.getImageryComponent().getName() + "]");
        imageryOdmUploadTask.setMessage("The results of the Orthorectification processing are being uploaded to S3. Currently uploading orthorectification artifacts for ['" + task.getImageryComponent().getName() + "']. Check back later for updates.");
        imageryOdmUploadTask.apply(); 
        
        uploadTask = imageryOdmUploadTask;
      }
      else
      {        
        ODMUploadTask odmUploadTask = new ODMUploadTask();
        odmUploadTask.setUpLoadId(task.getUpLoadId());
        odmUploadTask.setCollectionId(task.getImageryComponentOid());
        odmUploadTask.setGeoprismUser(task.getGeoprismUser());
        odmUploadTask.setOdmUUID(task.getOdmUUID());
        odmUploadTask.setStatus(ODMStatus.RUNNING.getLabel());
        odmUploadTask.setProcessingTask(task);
//        uploadTask.setTaskLabel("Uploading Orthorectification Artifacts for [" + task.getCollection().getName() + "].");
        odmUploadTask.setTaskLabel("UAV data orthorectification upload for collection [" + task.getImageryComponent().getName() + "]");
        odmUploadTask.setMessage("The results of the Orthorectification processing are being uploaded to S3. Currently uploading orthorectification artifacts for ['" + task.getImageryComponent().getName() + "']. Check back later for updates.");
        odmUploadTask.apply();
        
        uploadTask = odmUploadTask;
      }

      S3ResultsUploadThread thread = new S3ResultsUploadThread("S3Uploader for " + uploadTask.getOdmUUID(), uploadTask);
      uploadThreads.add(thread);
      thread.start();
    }
    
    private void addOutputToTask(ODMProcessingTaskIF task)
    {
      TaskOutputResponse resp = ODMFacade.taskOutput(task.getOdmUUID());
      
      if (resp.hasOutput())
      {
        JSONArray output = resp.getOutput();
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < output.length(); ++i)
        {
          sb.append(output.getString(i));
          sb.append("&#13;&#10;");
        }
        
        task.setOdmOutput(sb.toString());
      }
    }
  }
  
  private static class S3ResultsUploadThread extends Thread
  {
    private ODMUploadTaskIF uploadTask;
    
    private File zip;
    
    private File unzippedParentFolder;
    
    private Boolean isTest = false;
    
    protected S3ResultsUploadThread(String name, ODMUploadTaskIF uploadTask)
    {
      super(name);
      
      this.uploadTask = uploadTask;
      this.zip = ODMFacade.taskDownload(uploadTask.getOdmUUID());
      this.unzippedParentFolder = new File(FileUtils.getTempDirectory(), "odm-" + uploadTask.getOdmUUID());
    }
    
    protected S3ResultsUploadThread(String name, ODMUploadTask uploadTask, File zip, Boolean isTest)
    {
      super(name);
      
      this.uploadTask = uploadTask;
      this.zip = zip;
      this.isTest = isTest;
      unzippedParentFolder = new File(FileUtils.getTempDirectory(), "odm-" + uploadTask.getOdmUUID());
    }
    
    @Override
    public void run()
    {
      try
      {
        runInRequest();
      }
      finally
      {
        uploadThreads.remove(this);
      }
    }
    
    @Request
    public void runInRequest()
    {
      try
      {
        runInTrans();
        
        uploadTask.lock();
        uploadTask.setStatus(ODMStatus.COMPLETED.getLabel());
        uploadTask.setMessage("The upload successfully completed.  All files except those mentioned were archived.");
        uploadTask.apply();
        
        // Create image services
        uploadTask.getImageryComponent().createImageServices();
        
        ODMStatusServer.sendEmail(uploadTask);
      }
      catch (Throwable t)
      {
        logger.error("Error occurred while uploading S3 files for " + uploadTask.getOdmUUID(), t);
        
        uploadTask.lock();
        uploadTask.setStatus(ODMStatus.FAILED.getLabel());
        uploadTask.setMessage("The upload failed. " + t.getLocalizedMessage());
        uploadTask.apply();
      }
    }
    
    private List<ODMFolderProcessingConfig> buildProcessingConfig()
    {
      List<ODMFolderProcessingConfig> processingConfigs = new ArrayList<ODMFolderProcessingConfig>();
      
      processingConfigs.add(new ODMFolderProcessingConfig("odm_dem", Collection.DEM, new String[] {"dsm.tif", "dtm.tif"}));
      
      processingConfigs.add(new ODMFolderProcessingConfig("odm_georeferencing", Collection.PTCLOUD, new String[] {"odm_georeferenced_model.laz"}));
      
      processingConfigs.add(new ODMFolderProcessingConfig("odm_orthophoto", Collection.ORTHO, new String[] {"odm_orthophoto.png", "odm_orthophoto.tif"}));
      
      return processingConfigs;
    }
    
//    @Transaction
    public void runInTrans() throws ZipException
    {
      List<ODMFolderProcessingConfig> processingConfigs = buildProcessingConfig();
      
      /**
       * Upload the full all.zip file to S3 for archive purposes.
       */
      if (!isTest)
      {
        Util.uploadFileToS3(zip, uploadTask.getImageryComponent().getS3location() + "odm_all" + "/" + zip.getName(), uploadTask);
      }
      
      String filePrefix = this.uploadTask.getProcessingTask().getFilePrefix();
      
      /**
       * Unzip the ODM all.zip file and selectively upload files that interest us to S3.
       */
      try
      {
        new ZipFile(zip).extractAll(unzippedParentFolder.getAbsolutePath());
        
        for (ODMFolderProcessingConfig config : processingConfigs)
        {
          if (Thread.interrupted())
          {
            Thread.currentThread().interrupt();
            return;
          }
          
          File parentDir = new File(unzippedParentFolder, config.odmFolderName);
          
          if (parentDir.exists())
          {
            processChildren(parentDir, config.s3FolderName, config, filePrefix);
          }
          
          List<String> unprocessed = config.getUnprocessedFiles();
          if (unprocessed.size() > 0)
          {
            for (String name : unprocessed)
            {
              uploadTask.createAction("ODM did not produce an expected file [" + config.s3FolderName + "/" + name + "].", "error");
            }
          }
        }
      }
      finally
      {
        if (!isTest)
        {
          FileUtils.deleteQuietly(zip);
        }
        FileUtils.deleteQuietly(unzippedParentFolder);
        removeFromOdm(this.uploadTask, this.uploadTask.getOdmUUID());
      }
    }

    private void processChildren(File parentDir, String s3FolderPrefix, ODMFolderProcessingConfig config, String filePrefix)
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
        
        if (filePrefix != null && filePrefix.length() > 0)
        {
          name = filePrefix + "_" + name;
        }
        
        if (!child.isDirectory() && UasComponent.isValidName(name) && config.shouldProcessFile(child))
        {
          ImageryComponent ic = uploadTask.getImageryComponent();
          
          String key = ic.getS3location() + s3FolderPrefix + "/" + name;
  
          if (!isTest)
          {
            Util.uploadFileToS3(child, key, uploadTask);
          }
          
          SolrService.updateOrCreateDocument(ic.getAncestors(), (UasComponent)ic, key, name);
        }
        else if (child.isDirectory())
        {
          processChildren(child, s3FolderPrefix + "/" + child.getName(), config, filePrefix);
        }
      }
    }

    private class ODMFolderProcessingConfig
    {
      private String odmFolderName;
      
      private String s3FolderName;
      
      private String[] mandatoryFiles;
      
      private ArrayList<String> processedFiles;
      
      protected ODMFolderProcessingConfig(String odmFolderName, String s3FolderName, String[] mandatoryFiles)
      {
        this.odmFolderName = odmFolderName;
        this.s3FolderName = s3FolderName;
        this.mandatoryFiles = mandatoryFiles;
        this.processedFiles = new ArrayList<String>();
      }
      
      protected boolean shouldProcessFile(File file)
      {
        if (ArrayUtils.contains(mandatoryFiles, file.getName()))
        {
          processedFiles.add(file.getName());
          return true;
        }
        
        return false;
      }
      
      protected List<String> getUnprocessedFiles()
      {
        ArrayList<String> unprocessed = new ArrayList<String>();
        
        for (String file : mandatoryFiles)
        {
          if (!processedFiles.contains(file))
          {
            unprocessed.add(file);
          }
        }
        
        return unprocessed;
      }
    }
  }
}