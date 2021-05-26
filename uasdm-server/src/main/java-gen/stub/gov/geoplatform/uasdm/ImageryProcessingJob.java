/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.geoplatform.uasdm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.RunwayException;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.VaultFile;
import com.runwaysdk.system.scheduler.ExecutionContext;
import com.runwaysdk.system.scheduler.JobHistory;

import gov.geoplatform.uasdm.bus.AbstractUploadTask;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask.WorkflowTaskStatus;
import gov.geoplatform.uasdm.bus.CollectionUploadEvent;
import gov.geoplatform.uasdm.bus.CollectionUploadEventQuery;
import gov.geoplatform.uasdm.bus.ImageryUploadEvent;
import gov.geoplatform.uasdm.bus.ImageryUploadEventQuery;
import gov.geoplatform.uasdm.bus.ImageryWorkflowTask;
import gov.geoplatform.uasdm.bus.WorkflowTask;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.service.ProjectManagementService;
import gov.geoplatform.uasdm.view.RequestParser;
import gov.geoplatform.uasdm.ws.GlobalNotificationMessage;
import gov.geoplatform.uasdm.ws.MessageType;
import gov.geoplatform.uasdm.ws.NotificationFacade;
import gov.geoplatform.uasdm.ws.UserNotificationMessage;

public class ImageryProcessingJob extends ImageryProcessingJobBase
{
  private static final Logger  logger               = LoggerFactory.getLogger(ProjectManagementService.class);

  private static final long    serialVersionUID     = -339555201;

  // 0.9.8 supports tif and tiff, but we're on 0.9.1 right now.
  // https://github.com/OpenDroneMap/ODM/blob/master/opendm/context.py
  public static final String[] SUPPORTED_EXTENSIONS = new String[] { "jpg", "jpeg", "png" };

  public ImageryProcessingJob()
  {
    super();
  }

  public static void processFiles(RequestParser parser, File archive) throws FileNotFoundException
  {
    AbstractUploadTask task = ImageryWorkflowTask.getTaskByUploadId(parser.getUuid());

    if (!validateArchive(archive, task))
    {
      return;
    }

    try
    {
      String outFileNamePrefix = parser.getCustomParams().get("outFileName");
      String uploadTarget = parser.getUploadTarget();
      VaultFile vfImageryZip = VaultFile.createAndApply(parser.getFilename(), new FileInputStream(archive));
      Boolean processUpload = parser.getProcessUpload();

      ImageryProcessingJob job = new ImageryProcessingJob();
      job.setRunAsUserId(Session.getCurrentSession().getUser().getOid());
      job.setWorkflowTask(task);
      job.setImageryFile(vfImageryZip.getOid());
      job.setUploadTarget(uploadTarget);
      job.setOutFileNamePrefix(outFileNamePrefix);
      job.setProcessUpload(processUpload);
      job.apply();
      job.start();
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

  @Override
  public boolean canResume()
  {
    return true;
  }

  @Override
  public void execute(ExecutionContext executionContext)
  {
    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.JOB_CHANGE, null));

    this.uploadToS3(VaultFile.get(this.getImageryFile()), this.getUploadTarget(), this.getOutFileNamePrefix());
  }

  @Override
  public void afterJobExecute(JobHistory history)
  {
    super.afterJobExecute(history);

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.JOB_CHANGE, null));
  }

  private static boolean validateArchive(File archive, AbstractUploadTask task)
  {
    final String ext = FilenameUtils.getExtension(archive.getName()).toLowerCase();
    final boolean isMultispectral = isMultispectral(task);

    boolean hasFiles = false;

    try
    {
      if (ext.equalsIgnoreCase("zip"))
      {
        try (ZipFile zipFile = new ZipFile(archive))
        {
          Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();

          while (entries.hasMoreElements())
          {
            ZipArchiveEntry entry = entries.nextElement();

            String filename = entry.getName();

            boolean isValid = validateFile(filename, entry.isDirectory(), isMultispectral, task);

            hasFiles = hasFiles || isValid;
          }
        }
      }
      else if (ext.equalsIgnoreCase("gz"))
      {
        try (GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(new FileInputStream(archive)))
        {
          try (TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn))
          {
            TarArchiveEntry entry;

            while ( ( entry = (TarArchiveEntry) tarIn.getNextEntry() ) != null)
            {
              final String filename = entry.getName();

              boolean isValid = validateFile(filename, entry.isDirectory(), isMultispectral, task);

              hasFiles = hasFiles || isValid;
            }
          }
        }
      }
    }
    catch (IOException e)
    {
      task.createAction(RunwayException.localizeThrowable(e, Session.getCurrentLocale()), "error");

      throw new InvalidZipException();
    }

    if (!hasFiles)
    {
      if (!isMultispectral)
      {
        task.lock();
        task.setStatus(WorkflowTaskStatus.ERROR.toString());
        task.setMessage("The zip did not contain any files to process. Files must be at the top-most level of the zip (not in a sub-directory), they must follow proper naming conventions and end in one of the following file extensions: " + StringUtils.join(SUPPORTED_EXTENSIONS, ", "));
        task.apply();
      }
      else
      {
        task.lock();
        task.setStatus(WorkflowTaskStatus.ERROR.toString());
        task.setMessage("Could not process files in upload. All files must be at the top-most level of the directory (not in a sub-directory) and must follow proper naming conventions. You selected a multispectral sensor, all files must be of .tif format to be processed.");
        task.apply();
      }

      if (Session.getCurrentSession() != null)
      {
        NotificationFacade.queue(new UserNotificationMessage(Session.getCurrentSession(), MessageType.UPLOAD_JOB_CHANGE, task.toJSON()));
      }

      return false;
    }

    return true;
  }

  private static boolean validateFile(String filename, boolean isDirectory, boolean isMultispectral, AbstractUploadTask task)
  {
    boolean isVideo = Util.isVideoFile(filename);
    boolean isValidName = UasComponentIF.isValidName(filename);
    String ext = FilenameUtils.getExtension(filename).toLowerCase();

    if (isDirectory)
    {
      task.createAction("The directory [" + filename + "] inside the uploaded zip will be ignored. Any files within it will not be processed.", "error");
      return false;
    }

    if (!isVideo)
    {
      if (!isValidName)
      {
        task.createAction("The filename [" + filename + "] is invalid. No spaces or special characters such as <, >, -, +, =, !, @, #, $, %, ^, &, *, ?,/, \\ or apostrophes are allowed.", "error");

        return false;
      }

      if (isMultispectral)
      {
        if (!filename.endsWith(".tif"))
        {
          task.createAction("Multispectral processing only supports .tif format. The file [" + filename + "] will be ignored.", "error");
          return false;
        }
      }
      else
      {
        if (!ArrayUtils.contains(SUPPORTED_EXTENSIONS, ext))
        {
          task.createAction("The file [" + filename + "] is of an unsupported format and will be ignored. The following formats are supported: " + StringUtils.join(SUPPORTED_EXTENSIONS, ", "), "error");
          return false;
        }
      }
    }

    return true;
  }

  private static boolean isMultispectral(AbstractWorkflowTask task)
  {
    if (task instanceof ImageryWorkflowTask)
    {
      return false;
    }
    else
    {
      WorkflowTask collectionWorkflowTask = (WorkflowTask) task;

      return CollectionUploadEvent.isMultispectral(collectionWorkflowTask.getComponentInstance());
    }
  }

  private void uploadToS3(VaultFile vfImageryZip, String uploadTarget, String outFileNamePrefix)
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

        event.handleUploadFinish(imageryWorkflowTask, uploadTarget, vfImageryZip, outFileNamePrefix, this.getProcessUpload());
      }
      else
      {
        WorkflowTask collectionWorkflowTask = (WorkflowTask) task;

        CollectionUploadEvent event = this.getOrCreateEvent(collectionWorkflowTask);
        event.setGeoprismUser(collectionWorkflowTask.getGeoprismUser());
        event.setUploadId(collectionWorkflowTask.getUploadId());
        event.setComponent(collectionWorkflowTask.getComponent());
        event.apply();

        event.handleUploadFinish(collectionWorkflowTask, uploadTarget, vfImageryZip, outFileNamePrefix, this.getProcessUpload());
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
    finally
    {
      vfImageryZip.delete();
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
