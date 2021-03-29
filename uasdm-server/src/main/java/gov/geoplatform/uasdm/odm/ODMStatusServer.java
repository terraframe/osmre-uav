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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.RunwayException;
import com.runwaysdk.business.SmartException;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.Session;

import gov.geoplatform.uasdm.DevProperties;
import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTaskQuery;
import gov.geoplatform.uasdm.model.AbstractWorkflowTaskIF;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.service.SolrService;
import gov.geoplatform.uasdm.ws.GlobalNotificationMessage;
import gov.geoplatform.uasdm.ws.MessageType;
import gov.geoplatform.uasdm.ws.NotificationFacade;
import net.geoprism.EmailSetting;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import gov.geoplatform.uasdm.odm.AllZipS3Uploader.BasicODMFile;
import gov.geoplatform.uasdm.odm.AllZipS3Uploader.SpecialException;

public class ODMStatusServer
{
  private static final Integer               ODM_STATUS_UPDATE_INTERVAL = 5000;                                          // in
                                                                                                                         // miliseconds

  private static ODMStatusThread             statusThread;

  private static List<S3ResultsUploadThread> uploadThreads              = new ArrayList<S3ResultsUploadThread>();

  private static Logger                      logger                     = LoggerFactory.getLogger(ODMStatusServer.class);

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

        EmailSetting.sendEmail(subject, body, new String[] { task.getGeoprismUser().getEmail() });
      }
      else if (task.getStatus().equals(ODMStatus.COMPLETED.getLabel()))
      {
        final String subject = "Orthorectification Processing Completed";

        final String body = "The orthorectification processing for [" + task.getComponentLabel() + "] has completed. Log into the UASDM again to see the results.";

        EmailSetting.sendEmail(subject, body, new String[] { task.getGeoprismUser().getEmail() });
      }
      else
      {
        logger.error("Unable to send email of unknown task status [" + task.getStatus() + "].");
      }
    }
    catch (Throwable t)
    {
      logger.error("Problem sending email for task [" + task.getTaskLabel() + "] with status [" + task.getStatus() + "].", t);

      task.createAction("Problem occured while sending email. " + RunwayException.localizeThrowable(t, Session.getCurrentLocale()), "error");
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
    catch (Throwable t)
    {
      logger.error("Error occurred while removing task [" + uuid + "] [" + task.getTaskLabel() + "] from ODM.", t);

      task.createAction("Problem occured while cleaning up data from ODM. " + RunwayException.localizeThrowable(t, Session.getCurrentLocale()), "error");
    }
  }

  private static class ODMStatusThread extends Thread
  {
    /**
     * A list of tasks that require status updates.
     */
    private List<ODMProcessingTaskIF> activeTasks  = new ArrayList<ODMProcessingTaskIF>();

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
          return;
        }

        ODMProcessingTaskIF task = it.next();
  
        InfoResponse resp = null;
        try
        {
          if (DevProperties.runOrtho())
          {
            resp = ODMFacade.taskInfo(task.getOdmUUID());
          }
          else
          {
            resp = DevProperties.getMockOdmTaskInfo();
          }
  
          if (resp.getHTTPResponse().isUnreachableHost())
          {
            String msg = "Unable to reach ODM server. code: " + resp.getHTTPResponse().getStatusCode() + " response: " + resp.getHTTPResponse().getResponse();
            logger.error(msg);
            UnreachableHostException ex = new UnreachableHostException(msg);
  
            task.appLock();
            task.setStatus(ODMStatus.FAILED.getLabel());
            task.setMessage(RunwayException.localizeThrowable(ex, Session.getCurrentLocale()));
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
            task.setOdmOutput("HTTP communication with ODM has failed [" + resp.getHTTPResponse().getStatusCode() + "]. " + resp.getHTTPResponse().getResponse());
            task.apply();
  
            it.remove();
          }
          else
          {
            ODMStatus respStatus = resp.getStatus();
            
            if (respStatus == null)
            {
              task.appLock();
              task.setStatus(ODMStatus.FAILED.getLabel());
              addOutputToTask(task);
              task.apply();
  
              it.remove();
  
              sendEmail(task);
  
              removeFromOdm(task, task.getOdmUUID());
            }
            else if (ODMStatus.FAILED.equals(respStatus))
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
            else if (ODMStatus.RUNNING.equals(respStatus))
            {
              task.appLock();
              task.setStatus(resp.getStatus().getLabel());
  
//              long millis = resp.getProcessingTime();
//  
//              String sProcessingTime = String.format("%d hours, %d minutes", TimeUnit.MILLISECONDS.toHours(millis), TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)));
  
              if (resp.getImagesCount() == 1) // This happens when not using ODM's newer "chunk" functionality, and it only happens when a auto-scaling node is spinning up.
              {
                task.setMessage("Your images are being processed. Check back later for updates. An email will be sent when the processing is complete.");
              }
              else
              {
                task.setMessage("Processing of " + resp.getImagesCount() + " images. An email will be sent when the processing is complete");
//                task.setMessage("Processing of " + resp.getImagesCount() + " images has been running for " + sProcessingTime + ". An email will be sent when the processing is complete");
              }
              
              task.apply();
            }
            else if (ODMStatus.QUEUED.equals(respStatus))
            {
              task.appLock();
              task.setStatus(resp.getStatus().getLabel());
              task.setMessage("Job is queued."); // TODO : Position in queue?
              task.apply();
            }
            else if (ODMStatus.CANCELED.equals(respStatus))
            {
              task.appLock();
              task.setStatus(resp.getStatus().getLabel());
              task.setMessage("Job was canceled.");
              task.apply();
  
              it.remove();
  
              // TODO : Remove from ODM?
            }
            else if (ODMStatus.COMPLETED.equals(respStatus))
            {
              long millis = resp.getProcessingTime();
  
              String sProcessingTime = String.format("%d hours, %d minutes", TimeUnit.MILLISECONDS.toHours(millis), TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)));
  
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
        catch (Throwable t)
        {
          logger.error("Error encountered in ODMStatusServer", t);
          
          task.appLock();
          task.setStatus(ODMStatus.FAILED.getLabel());
          
          String errMsg = RunwayException.localizeThrowable(t, CommonProperties.getDefaultLocale());
          
          if (t instanceof SmartException)
          {
            task.setMessage(errMsg);
          }
          else
          {
            task.setMessage("The job encountered an unspecified error.");
          }
          
          if (resp != null && resp.getHTTPResponse() != null)
          {
            task.setOdmOutput("HTTP communication with ODM has failed [" + resp.getHTTPResponse().getStatusCode() + "]. " + resp.getHTTPResponse().getResponse() + ". " + errMsg);
          }
          else if (!(t instanceof SmartException))
          {
            task.setOdmOutput("HTTP communication with ODM has failed. " + errMsg);
          }
          
          task.apply();

          it.remove();
        }
      }
    }

    private void uploadResultsToS3(ODMProcessingTaskIF task)
    {
      ODMUploadTaskIF uploadTask = null;

      if (task instanceof ImageryODMProcessingTask)
      {
        ImageryODMUploadTask imageryOdmUploadTask = new ImageryODMUploadTask();
        imageryOdmUploadTask.setUploadId(task.getUploadId());
        imageryOdmUploadTask.setImagery(task.getImageryComponent().getOid());
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
        odmUploadTask.setUploadId(task.getUploadId());
        odmUploadTask.setComponent(task.getImageryComponent().getOid());
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
      
      NotificationFacade.queue(new GlobalNotificationMessage(MessageType.JOB_CHANGE, null));

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

        task.writeODMtoS3(output);
        
        processODMOutputAndCreateTasks(output, task);
      }
    }
    
    /**
     * Read through the ODM output and attempt to identify issues that may have occurred.
     */
    private void processODMOutputAndCreateTasks(JSONArray output, ODMProcessingTaskIF task)
    {
      String sOutput = output.toString();
      
      for (int i = 0; i < output.length(); ++i)
      {
        String line = output.getString(i);
        
        if (line.contains("MICA-CODE:1"))
        {
          task.createAction("Unable to find image panel. This may effect image color quality. Did you include a panel with naming convention IMG_0000_*.tif? Refer to the multispectral documentation for more information.", "error");
        }
        else if (line.contains("MICA-CODE:2"))
        {
          task.createAction("Alignment image not found. Did you include an alignment image with naming convention IMG_0001_*.tif? Refer to the multispectral documentation for more information.", "error");
        }
        else if (line.contains("MICA-CODE:3"))
        {
          task.createAction("Image [\" + fileName + \"] does not match the naming convention. Are you following the proper naming convention for your images? Refer to the multispectral documentation for more information.", "error");
        }
        else if (line.contains("MICA-CODE:4"))
        {
          task.createAction("Image alignment has failed. Take the time to look through your image collection and weed out any bad images, i.e. pictures of the camera inside the case, or pictures which are entirely black, etc. Also make sure that if your collection includes panel images, that they are named as IMG_0000_*.tif. You can also try using a different alignment image (by renaming a better image set to IMG_0001_*.tif). Finally, make sure this collection is of a single flight and that there were no 'shock' events. Refer to the multispectral documentation for more information.", "error");
        }
        else if (line.contains("OSError: Panels not detected in all images")) // Micasense
        {
          task.createAction("Your upload includes images with naming convention IMG_0000_*.tif, however those images do not appear to be of a panel. Make sure that IMG_0000_*.tif, if included, is of a panel, and that there are no other panels anywhere else in your images. Refer to the multispectral documentation for more information.", "error");
        }
        else if (
            line.contains("[Errno 2] No such file or directory: '/var/www/data/0182028a-c14f-40fe-bd6f-e98362ec48c7/opensfm/reconstruction.json'") // 0.9.1 output
            || line.contains("The program could not process this dataset using the current settings. Check that the images have enough overlap") // 0.9.8 output
            || (line.contains("IndexError: list index out of range")
                && output.getString(i-1).contains("reconstruction = data[0]")
                && sOutput.contains("Traceback (most recent call last):")) // 0.9.1
            )
        {
          task.createAction("ODM failed to produce a reconstruction from the image matches. Check that the images have enough overlap, that there are enough recognizable features and that the images are in focus. (more info: https://github.com/OpenDroneMap/ODM/issues/524)", "error");
        }
        else if (line.contains("Not enough supported images in")) // 0.9.1
        {
          task.createAction("Couldn't find any usable images. The orthomosaic image data must contain at least two images with extensions '.jpg','.jpeg','.png'", "error");
        }
        else if (line.contains("bad_alloc")) // Comes from ODM (and/or sub-libraries)
        {
          task.createAction("ODM ran out of memory during processing. Please contact your technical support.", "error");
        }
      }
    }
  }

  private static class S3ResultsUploadThread extends Thread
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
        ProductIF product = runInTrans();

        uploadTask.lock();
        uploadTask.setStatus(ODMStatus.COMPLETED.getLabel());
        uploadTask.setMessage("The upload successfully completed.  All files except those mentioned were archived.");
        uploadTask.apply();

        NotificationFacade.queue(new GlobalNotificationMessage(MessageType.JOB_CHANGE, null));

        // Create image services
        product.createImageService();

        // Calculate bounding boxes
        product.updateBoundingBox();

        ODMStatusServer.sendEmail(uploadTask);
      }
      catch (Throwable t)
      {
        logger.error("Error occurred while uploading S3 files for " + uploadTask.getOdmUUID(), t);

        String msg;
        if (t instanceof SpecialException)
        {
          msg = t.getLocalizedMessage();
        }
        else
        {
          msg = "The upload failed. " + RunwayException.localizeThrowable(t, Session.getCurrentLocale());
        }

        uploadTask.lock();
        uploadTask.setStatus(ODMStatus.FAILED.getLabel());
        uploadTask.setMessage(msg);
        uploadTask.apply();
        
        NotificationFacade.queue(new GlobalNotificationMessage(MessageType.JOB_CHANGE, null));
      }
    }

//    @Transaction
    public ProductIF runInTrans() throws ZipException, SpecialException, InterruptedException
    {
      ImageryComponent ic = uploadTask.getImageryComponent();
      UasComponentIF component = ic.getUasComponent();
      
      AllZipS3Uploader processor = new AllZipS3Uploader(component, uploadTask);
      
      try
      {
        return processor.processAllZip();
      }
      finally
      {
        if (DevProperties.runOrtho())
        {
          removeFromOdm(this.uploadTask, this.uploadTask.getOdmUUID());
        }
      }
    }
  }
}
