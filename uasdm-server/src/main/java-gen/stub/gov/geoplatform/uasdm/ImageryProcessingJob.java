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
package gov.geoplatform.uasdm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.RunwayException;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.resource.ApplicationFileResource;
import com.runwaysdk.resource.ArchiveFileResource;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.VaultFile;
import com.runwaysdk.system.scheduler.ExecutionContext;
import com.runwaysdk.system.scheduler.JobHistory;
import com.runwaysdk.system.scheduler.JobHistoryRecord;

import gov.geoplatform.uasdm.bus.AbstractUploadTask;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask.WorkflowTaskStatus;
import gov.geoplatform.uasdm.bus.CollectionUploadEvent;
import gov.geoplatform.uasdm.bus.CollectionUploadEventQuery;
import gov.geoplatform.uasdm.bus.ImageryUploadEvent;
import gov.geoplatform.uasdm.bus.ImageryUploadEventQuery;
import gov.geoplatform.uasdm.bus.ImageryWorkflowTask;
import gov.geoplatform.uasdm.bus.WorkflowTask;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ProcessConfiguration;
import gov.geoplatform.uasdm.odm.ODMProcessConfiguration;
import gov.geoplatform.uasdm.processing.raw.UploadValidationProcessor;
import gov.geoplatform.uasdm.service.ProjectManagementService;
import gov.geoplatform.uasdm.view.RequestParserIF;
import gov.geoplatform.uasdm.ws.GlobalNotificationMessage;
import gov.geoplatform.uasdm.ws.MessageType;
import gov.geoplatform.uasdm.ws.NotificationFacade;
import gov.geoplatform.uasdm.ws.UserNotificationMessage;

public class ImageryProcessingJob extends ImageryProcessingJobBase
{
  private static final Logger logger           = LoggerFactory.getLogger(ProjectManagementService.class);

  private static final long   serialVersionUID = -339555201;

  public ImageryProcessingJob()
  {
    super();
  }

  public ProcessConfiguration getConfiguration()
  {
    String json = this.getConfigurationJson();

    if (!StringUtils.isEmpty(json))
    {
      return ProcessConfiguration.parse(json);
    }

    return new ODMProcessConfiguration();
  }

  public void setConfiguration(ProcessConfiguration configuration)
  {
    this.setConfigurationJson(configuration.toJson().toString());
  }
  
  public static List<String> getSupportedExtensions(String uploadTarget, boolean isMultispectral, ProcessConfiguration config)
  {
    if (isMultispectral || uploadTarget.equals(ImageryComponent.DEM))
    {
      return Arrays.asList("tif", "tiff");
    }
    else if (uploadTarget.equals(ImageryComponent.ORTHO))
    {
      return Arrays.asList("png", "tif", "tiff");
    }
    else if (uploadTarget.equals(ImageryComponent.PTCLOUD))
    {
      return Arrays.asList("laz", "las");
    }
    else if (uploadTarget.equals(ImageryComponent.VIDEO))
    {
      return Arrays.asList("mp4");
    }

    if (config.isLidar())
    {
      return Arrays.asList("las", "laz");
    }
    else
    {
      return Arrays.asList("jpg", "jpeg", "png", "tif", "tiff");
    }
  }

  /**
   * 
   * @param parser
   * @param inFile Can be either an archive (.zip or .tar.gz) or an individual file such as a ptcloud or .tif
   * @return
   * @throws FileNotFoundException
   */
  public static JobHistory processFiles(String runAsUserOid, RequestParserIF parser, File inFile) throws FileNotFoundException
  {
    VaultFile vfImageryZip = null;
    ProcessConfiguration configuration = ProcessConfiguration.parse(parser);
    
    AbstractUploadTask task = ImageryWorkflowTask.getTaskByUploadId(parser.getUuid());
    if (task instanceof WorkflowTask) {
      ((WorkflowTask)task).setProcessDem(parser.getProcessDem());
      ((WorkflowTask)task).setProcessOrtho(parser.getProcessOrtho());
      ((WorkflowTask)task).setProcessPtcloud(parser.getProcessPtcloud());
    }
    
    try (FileInputStream istream = new FileInputStream(inFile))
    {
      vfImageryZip = VaultFile.createAndApply(parser.getFilename(), istream);
      Boolean processUpload = parser.getProcessUpload();

      ImageryProcessingJob job = new ImageryProcessingJob();
      job.setRunAsUserId(runAsUserOid);
      job.setWorkflowTask(task);
      job.setImageryFile(vfImageryZip.getOid());
      job.setUploadTarget(task.getUploadTarget());
      job.setProcessUpload(processUpload);
      job.setConfiguration(configuration);
      // job.setOutFileNamePrefix(configuration.getOutFileNamePrefix());
      job.apply();

      JobHistory history = job.start();

      return history;
    }
    catch (Throwable t)
    {
      t.printStackTrace();
      
      try
      {
        vfImageryZip.delete();
      }
      catch (Throwable t2)
      {
      }

      task.lock();
      task.setStatus(WorkflowTaskStatus.ERROR.toString());
      task.setMessage("An error occurred while uploading the imagery to S3. " + RunwayException.localizeThrowable(t, CommonProperties.getDefaultLocale()));
      task.apply();

      logger.error("An error occurred while uploading the imagery to S3.", t);
    }

    return null;
  }

  @Override
  public boolean canResume(JobHistoryRecord record)
  {
    return true;
  }

  @Override
  public void execute(ExecutionContext executionContext)
  {
    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.JOB_CHANGE, null));
    
    final AbstractWorkflowTask task = this.getWorkflowTask();
    ApplicationFileResource res = VaultFile.get(this.getImageryFile());
    ApplicationFileResource validated = null;
    
    try
    {
      final String ext = res.getNameExtension().toLowerCase();
      
      if (ext.endsWith("gz") || ext.endsWith("zip"))
      {
        res = new ArchiveFileResource(res);
      }
      
      var validator = new UploadValidationProcessor();
      boolean isValid = validator.process(res, (AbstractUploadTask) this.getWorkflowTask(), getConfiguration());
      
      if (isValid)
        this.uploadToS3(validator.getDownstreamFile(), this.getUploadTarget(), this.getConfiguration());
    }
    catch (Throwable t)
    {
      task.lock();
      task.setStatus(WorkflowTaskStatus.ERROR.toString());
      task.setMessage("An error occurred while uploading the imagery to S3. " + RunwayException.localizeThrowable(t, Session.getCurrentLocale()));
      task.apply();

      logger.error("An error occurred while uploading the imagery to S3.", t);

      if (Session.getCurrentSession() != null)
      {
        NotificationFacade.queue(new UserNotificationMessage(Session.getCurrentSession(), MessageType.UPLOAD_JOB_CHANGE, task.toJSON()));
      }
    }
    finally
    {
      res.delete();
      
      if (validated != null)
        validated.delete();
    }
  }

  @Override
  public void afterJobExecute(JobHistory history)
  {
    super.afterJobExecute(history);

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.JOB_CHANGE, null));
  }

  private void uploadToS3(ApplicationFileResource res, String uploadTarget, ProcessConfiguration configuration)
  {
    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.JOB_CHANGE, null));

    AbstractWorkflowTask task = this.getWorkflowTask();

    try
    {
      if (task instanceof ImageryWorkflowTask)
      {
        ImageryWorkflowTask imageryWorkflowTask = (ImageryWorkflowTask) task;

        ImageryUploadEvent event = this.getOrCreateEvent(imageryWorkflowTask);
        event.setGeoprismUser(imageryWorkflowTask.getGeoprismUser());
        event.setUploadId(imageryWorkflowTask.getUploadId());
        event.setImagery(imageryWorkflowTask.getImagery());
        event.apply();

        event.handleUploadFinish(imageryWorkflowTask, uploadTarget, res, configuration, this.getProcessUpload());
      }
      else
      {
        WorkflowTask collectionWorkflowTask = (WorkflowTask) task;

        CollectionUploadEvent event = this.getOrCreateEvent(collectionWorkflowTask);
        event.setGeoprismUser(collectionWorkflowTask.getGeoprismUser());
        event.setUploadId(collectionWorkflowTask.getUploadId());
        event.setComponent(collectionWorkflowTask.getComponent());
        event.apply();

        event.handleUploadFinish(collectionWorkflowTask, uploadTarget, res, this.getProcessUpload(), configuration);
      }
    }
    catch (Throwable t)
    {
      task.lock();
      task.setStatus(WorkflowTaskStatus.ERROR.toString());
      task.setMessage("An error occurred while uploading the imagery to S3. " + RunwayException.localizeThrowable(t, Session.getCurrentLocale()));
      task.apply();

      logger.error("An error occurred while uploading the imagery to S3.", t);

      if (Session.getCurrentSession() != null)
      {
        NotificationFacade.queue(new UserNotificationMessage(Session.getCurrentSession(), MessageType.UPLOAD_JOB_CHANGE, task.toJSON()));
      }
    }
  }

  private CollectionUploadEvent getOrCreateEvent(WorkflowTask collectionWorkflowTask)
  {
    CollectionUploadEventQuery eq = new CollectionUploadEventQuery(new QueryFactory());
    eq.WHERE(eq.getUploadId().EQ(collectionWorkflowTask.getUploadId()));
    CollectionUploadEvent event;
    if (eq.getCount() > 0)
    {
      OIterator<? extends CollectionUploadEvent> it = eq.getIterator();
      try
      {
        event = it.next();
        event.lock();
      }
      finally
      {
        it.close();
      }
    }
    else
    {
      event = new CollectionUploadEvent();
    }
    return event;
  }

  private ImageryUploadEvent getOrCreateEvent(ImageryWorkflowTask imageryWorkflowTask)
  {
    ImageryUploadEventQuery eq = new ImageryUploadEventQuery(new QueryFactory());
    eq.WHERE(eq.getUploadId().EQ(imageryWorkflowTask.getUploadId()));
    ImageryUploadEvent event;
    if (eq.getCount() > 0)
    {
      OIterator<? extends ImageryUploadEvent> it = eq.getIterator();
      try
      {
        event = it.next();
        event.lock();
      }
      finally
      {
        it.close();
      }
    }
    else
    {
      event = new ImageryUploadEvent();
    }
    return event;
  }
}
