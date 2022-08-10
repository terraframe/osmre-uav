package gov.geoplatform.uasdm.odm;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.RunwayException;
import com.runwaysdk.business.SmartException;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.Session;

import gov.geoplatform.uasdm.DevProperties;
import gov.geoplatform.uasdm.bus.CollectionReport;
import gov.geoplatform.uasdm.model.AbstractWorkflowTaskIF;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.processing.ODMZipPostProcessor;
import gov.geoplatform.uasdm.ws.GlobalNotificationMessage;
import gov.geoplatform.uasdm.ws.MessageType;
import gov.geoplatform.uasdm.ws.NotificationFacade;
import net.geoprism.EmailSetting;
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

        final String body = "The orthorectification processing for [" + task.getComponentLabel() + "] has failed. " + task.getMessage();

        EmailSetting.sendEmail(subject, body, new String[] {
            task.getGeoprismUserIF().getEmail()
        });
      }
      else if (task.getStatus().equals(ODMStatus.COMPLETED.getLabel()))
      {
        final String subject = "Orthorectification Processing Completed";

        final String body = "The orthorectification processing for [" + task.getComponentLabel() + "] has completed. Log into the UASDM again to see the results.";

        EmailSetting.sendEmail(subject, body, new String[] {
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

      CollectionReport.update(uploadTask.getImageryComponent().getOid(), ODMStatus.COMPLETED.getLabel());
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

      CollectionReport.update(uploadTask.getImageryComponent().getOid(), ODMStatus.FAILED.getLabel());
    }
  }

  // @Transaction
  private ProductIF handleUploadTaskInTrans(ODMUploadTaskIF uploadTask) throws ZipException, InterruptedException
  {
    ImageryComponent ic = uploadTask.getImageryComponent();
    CollectionIF collection = (CollectionIF) ic.getUasComponent();

    ODMZipPostProcessor processor = new ODMZipPostProcessor(collection, uploadTask, null);

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

        CollectionReport.update(task.getImageryComponentOid(), ODMStatus.FAILED.getLabel());
      }
      else if (resp.hasError())
      {
        task.appLock();
        task.setStatus(ODMStatus.FAILED.getLabel());
        task.setMessage(resp.getError());
        task.apply();

        result.setStatus(TaskStatus.ERROR);

        sendEmail(task);

        CollectionReport.update(task.getImageryComponentOid(), ODMStatus.FAILED.getLabel());
      }
      else if (!resp.hasError() && resp.getHTTPResponse().isError())
      {
        task.appLock();
        task.setStatus(ODMStatus.FAILED.getLabel());
        task.setMessage("The job encountered an unspecified error.");
        task.setOdmOutput("HTTP communication with ODM has failed [" + resp.getHTTPResponse().getStatusCode() + "]. " + resp.getHTTPResponse().getResponse());
        task.apply();

        result.setStatus(TaskStatus.ERROR);

        CollectionReport.update(task.getImageryComponentOid(), ODMStatus.FAILED.getLabel());
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

          CollectionReport.update(task.getImageryComponentOid(), ODMStatus.FAILED.getLabel());
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

          CollectionReport.update(task.getImageryComponentOid(), ODMStatus.FAILED.getLabel());
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

      CollectionReport.update(task.getImageryComponentOid(), ODMStatus.FAILED.getLabel());
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

      processODMOutputAndCreateTasks(output, task);
    }
  }

  /**
   * Read through the ODM output and attempt to identify issues that may have
   * occurred.
   */
  private void processODMOutputAndCreateTasks(JSONArray output, ODMProcessingTaskIF task)
  {
    Set<String> actions = new HashSet<String>();

    String sOutput = output.toString();

    for (int i = 0; i < output.length(); ++i)
    {
      String line = output.getString(i);

      if (line.contains("MICA-CODE:1"))
      {
        actions.add("Unable to find image panel. This may effect image color quality. Did you include a panel with naming convention IMG_0000_*.tif? Refer to the multispectral documentation for more information.");
      }
      else if (line.contains("MICA-CODE:2"))
      {
        actions.add("Alignment image not found. Did you include an alignment image with naming convention IMG_0001_*.tif? Refer to the multispectral documentation for more information.");
      }
      else if (line.contains("MICA-CODE:3"))
      {
        Pattern pattern = Pattern.compile(".*Image \\[(.*)\\] does not match the naming convention\\. Are you.*\\(MICA-CODE:3\\).*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(line);
        boolean matchFound = matcher.find();
        if (matchFound)
        {
          actions.add("Image [" + matcher.group(1) + "] does not match the naming convention. Are you following the proper naming convention for your images? Refer to the multispectral documentation for more information.");
        }
        else
        {
          actions.add("Image does not match the naming convention. Are you following the proper naming convention for your images? Refer to the multispectral documentation for more information.");
        }
      }
      else if (line.contains("MICA-CODE:4"))
      {
        actions.add("Image alignment has failed. Take the time to look through your image collection and weed out any bad images, i.e. pictures of the camera inside the case, or pictures which are entirely black, etc. Also make sure that if your collection includes panel images, that they are named as IMG_0000_*.tif. You can also try using a different alignment image (by renaming a better image set to IMG_0001_*.tif). Finally, make sure this collection is of a single flight and that there were no 'shock' events. Refer to the multispectral documentation for more information.");
      }
      else if (line.contains("OSError: Panels not detected in all images")) // Micasense
      {
        actions.add("Your upload includes images with naming convention IMG_0000_*.tif, however those images do not appear to be of a panel. Make sure that IMG_0000_*.tif, if included, is of a panel, and that there are no other panels anywhere else in your images. Refer to the multispectral documentation for more information.");
      }
      else if (line.contains("[Errno 2] No such file or directory: '/var/www/data/0182028a-c14f-40fe-bd6f-e98362ec48c7/opensfm/reconstruction.json'") // 0.9.1
                                                                                                                                                      // output
          || line.contains("The program could not process this dataset using the current settings. Check that the images have enough overlap") // 0.9.8
                                                                                                                                               // output
          || ( line.contains("IndexError: list index out of range") && output.getString(i - 1).contains("reconstruction = data[0]") && sOutput.contains("Traceback (most recent call last):") ) // 0.9.1
      )
      {
        actions.add("ODM failed to produce a reconstruction from the image matches. Check that the images have enough overlap, that there are enough recognizable features and that the images are in focus. (more info: https://github.com/OpenDroneMap/ODM/issues/524)");
      }
      else if (line.contains("Not enough supported images in") // 0.9.1
          || ( line.contains("numpy.AxisError: axis 1 is out of bounds for array of dimension 0") // 2.4.7
          ))
      {
        actions.add("Couldn't find enough usable images. The orthomosaic image data must contain at least two images with extensions '.jpg','.jpeg','.png'");
      }
      else if (line.contains("bad_alloc")) // Comes from ODM (and/or
                                           // sub-libraries)
      {
        actions.add("ODM ran out of memory during processing. Please contact your technical support.");
      }
    }

    for (String action : actions)
    {
      task.createAction(action, "error");
    }
  }

}
