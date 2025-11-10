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

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.RunwayException;
import com.runwaysdk.business.SmartException;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.Session;

import gov.geoplatform.uasdm.DevProperties;
import gov.geoplatform.uasdm.graph.ODMRun;
import gov.geoplatform.uasdm.model.AbstractWorkflowTaskIF;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.processing.ODMZipPostProcessor;
import gov.geoplatform.uasdm.processing.report.CollectionReportFacade;
import gov.geoplatform.uasdm.remote.s3.S3RemoteFileService;
import gov.geoplatform.uasdm.ws.GlobalNotificationMessage;
import gov.geoplatform.uasdm.ws.MessageType;
import gov.geoplatform.uasdm.ws.NotificationFacade;
import net.geoprism.registry.service.business.EmailBusinessServiceIF;
import net.geoprism.spring.core.ApplicationContextHolder;
import net.lingala.zip4j.exception.ZipException;

public class ODMTaskProcessor
{
  public static enum TaskStatus {
    ERROR, ACTIVE, UPLOAD
  }

  public static class TaskResult
  {
    private TaskStatus status;

    private ODMUploadTaskIF downstreamTask;

    public TaskResult()
    {
      this.status = TaskStatus.ACTIVE;
      this.downstreamTask = null;
    }

    public TaskStatus getStatus()
    {
      return status;
    }

    public void setStatus(TaskStatus status)
    {
      this.status = status;
    }

    public ODMUploadTaskIF getDownstreamTask()
    {
      return downstreamTask;
    }

    public void setDownstreamTask(ODMUploadTaskIF downstreamTask)
    {
      this.downstreamTask = downstreamTask;
    }
  }

  private static Logger logger = LoggerFactory.getLogger(ODMStatusServer.class);

  // ODMProcessingTaskIF
  // AbstractWorkflowTask
  private void sendEmail(AbstractWorkflowTaskIF task)
  {
    try
    {
      if (task.getStatus().equals(ODMStatus.FAILED.getLabel()))
      {
        final String subject = "Orthorectification Processing Failed";
        
        final String body = "The orthorectification processing for [" + task.getDetailedComponentLabel() + "] has failed. " + task.getMessage();
        
        EmailBusinessServiceIF service = ApplicationContextHolder.getBean(EmailBusinessServiceIF.class);

        service.sendEmail(subject, body, new String[] {
            task.getGeoprismUserIF().getEmail()
        });
      }
      else if (task.getStatus().equals(ODMStatus.COMPLETED.getLabel()))
      {
        final String subject = "Orthorectification Processing Completed";

        final String body = "The orthorectification processing for [" + task.getDetailedComponentLabel() + "] has completed. Log into the UASDM again to see the results.";

        EmailBusinessServiceIF service = ApplicationContextHolder.getBean(EmailBusinessServiceIF.class);

        service.sendEmail(subject, body, new String[] {
            task.getGeoprismUserIF().getEmail()
        });
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

  @Request
  public void handleUploadTask(ODMUploadTaskIF uploadTask)
  {
    try
    {
      handleUploadTaskInTrans(uploadTask);

      uploadTask.lock();
      uploadTask.setStatus(ODMStatus.COMPLETED.getLabel());
      uploadTask.setMessage("The upload successfully completed.  All files except those mentioned were archived.");
      uploadTask.apply();

      NotificationFacade.queue(new GlobalNotificationMessage(MessageType.JOB_CHANGE, null));

      this.sendEmail(uploadTask);
    }
    catch (Throwable t)
    {
      logger.error("Error occurred while uploading S3 files for " + uploadTask.getOdmUUID(), t);

      String msg = RunwayException.localizeThrowable(t, Session.getCurrentLocale());

      uploadTask.lock();
      uploadTask.setStatus(ODMStatus.FAILED.getLabel());
      uploadTask.setMessage(msg);
      uploadTask.apply();

      NotificationFacade.queue(new GlobalNotificationMessage(MessageType.JOB_CHANGE, null));
    }
  }

  // @Transaction
  private ProductIF handleUploadTaskInTrans(ODMUploadTaskIF uploadTask) throws ZipException, InterruptedException
  {
    ImageryComponent ic = uploadTask.getImageryComponent();
    CollectionIF collection = (CollectionIF) ic.getUasComponent();
    
    ODMZipPostProcessor processor = new ODMZipPostProcessor(collection, uploadTask, null, uploadTask.getProcessingTask().getConfiguration());

    try
    {
      return processor.processAllZip();
    }
    finally
    {
      if (DevProperties.runOrtho())
      {
        removeFromOdm(uploadTask, uploadTask.getOdmUUID());
      }
    }
  }

  private void removeFromOdm(AbstractWorkflowTaskIF task, String uuid)
  {
    try
    {
      TaskRemoveResponse resp = ODMFacade.taskRemove(uuid);
      
      removeOdmWorkerAllZip(uuid);

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
  
  /**
   * When the ODM worker finishes, NodeODM automatically uploads the results to S3, and then kills the worker. This zip then gets proxy downloaded
   * in ClusterODM when we do a taskDownload. After we have uploaded the all zip at the end of processing, we should delete ODM's duplicate upload
   * 
   * @param uuid
   */
  public static void removeOdmWorkerAllZip(String uuid)
  {
    S3RemoteFileService s3 = new S3RemoteFileService();
    
    String key = uuid + "/all.zip";
    
    if (s3.objectExists(key))
    {
      s3.deleteObject(key);
    }
  }

  @Request
  public TaskResult handleProcessingTask(ODMProcessingTaskIF task)
  {
    TaskResult result = new TaskResult();

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

        result.setStatus(TaskStatus.ERROR);

        sendEmail(task);
      }
      else if (resp.hasError())
      {
        task.appLock();
        task.setStatus(ODMStatus.FAILED.getLabel());
        new ODMMessageConverter().process(task, resp.getError());
        task.apply();

        result.setStatus(TaskStatus.ERROR);

        sendEmail(task);
      }
      else if (!resp.hasError() && resp.getHTTPResponse().isError())
      {
        task.appLock();
        task.setStatus(ODMStatus.FAILED.getLabel());
        task.setMessage("The job encountered an unspecified error.");
        task.setOdmOutput("HTTP communication with ODM has failed [" + resp.getHTTPResponse().getStatusCode() + "]. " + resp.getHTTPResponse().getResponse());
        task.apply();
        
        result.setStatus(TaskStatus.ERROR);

        sendEmail(task);
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

          result.setStatus(TaskStatus.ERROR);

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

          result.setStatus(TaskStatus.ERROR);

          sendEmail(task);
          removeFromOdm(task, task.getOdmUUID());
        }
        else if (ODMStatus.RUNNING.equals(respStatus))
        {
          task.appLock();
          task.setStatus(resp.getStatus().getLabel());

          if (resp.getImagesCount() == 1) // This happens when not using
                                          // ODM's newer "chunk"
                                          // functionality, and it only
                                          // happens when a auto-scaling
                                          // node is spinning up.
          {
            task.setMessage("Your images are being processed. Check back later for updates. An email will be sent when the processing is complete.");
          }
          else
          {
            task.setMessage("Processing of " + resp.getImagesCount() + " images. An email will be sent when the processing is complete");
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

          result.setStatus(TaskStatus.ERROR);
          sendEmail(task);
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
          
          ODMUploadTaskIF uploadTask = this.createUploadTask(task);

          result.setStatus(TaskStatus.UPLOAD);
          result.setDownstreamTask(uploadTask);

          NotificationFacade.queue(new GlobalNotificationMessage(MessageType.JOB_CHANGE, null));
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
      else if (! ( t instanceof SmartException ))
      {
        task.setOdmOutput("HTTP communication with ODM has failed. " + errMsg);
      }

      task.apply();

      result.setStatus(TaskStatus.ERROR);
    }

    return result;

  }

  private ODMUploadTaskIF createUploadTask(ODMProcessingTaskIF task)
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

      // imageryOdmUploadTask.setTaskLabel("Uploading Orthorectification
      // Artifacts for [" + task.getCollection().getName() + "].");
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
      odmUploadTask.setProcessDem(task.getProcessDem());
      odmUploadTask.setProcessOrtho(task.getProcessOrtho());
      odmUploadTask.setProcessPtcloud(task.getProcessPtcloud());
      // uploadTask.setTaskLabel("Uploading Orthorectification Artifacts for
      // [" + task.getCollection().getName() + "].");
      odmUploadTask.setTaskLabel("UAV data orthorectification upload for collection [" + task.getImageryComponent().getName() + "]");
      odmUploadTask.setMessage("The results of the Orthorectification processing are being uploaded to S3. Currently uploading orthorectification artifacts for ['" + task.getImageryComponent().getName() + "']. Check back later for updates.");
      odmUploadTask.apply();

      uploadTask = odmUploadTask;
    }

    return uploadTask;
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

      new ODMMessageConverter().process(task, task.getMessage(), output);
      
      ODMRun run = ODMRun.getForTask(task.getOid());
      if (run != null)
      {
        run.setOutput(sb.toString());
        run.apply();
      }
    }
  }

}
