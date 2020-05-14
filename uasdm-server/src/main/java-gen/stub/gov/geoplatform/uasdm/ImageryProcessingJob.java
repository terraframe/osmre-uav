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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.VaultFile;
import com.runwaysdk.system.scheduler.ExecutionContext;

import gov.geoplatform.uasdm.bus.AbstractUploadTask;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask.WorkflowTaskStatus;
import gov.geoplatform.uasdm.bus.CollectionUploadEvent;
import gov.geoplatform.uasdm.bus.CollectionUploadEventQuery;
import gov.geoplatform.uasdm.bus.ImageryUploadEvent;
import gov.geoplatform.uasdm.bus.ImageryUploadEventQuery;
import gov.geoplatform.uasdm.bus.ImageryWorkflowTask;
import gov.geoplatform.uasdm.bus.WorkflowTask;
import gov.geoplatform.uasdm.service.ProjectManagementService;
import gov.geoplatform.uasdm.view.RequestParser;
import gov.geoplatform.uasdm.ws.NotificationFacade;
import gov.geoplatform.uasdm.ws.NotificationMessage;

public class ImageryProcessingJob extends ImageryProcessingJobBase
{
  private static final Logger logger           = LoggerFactory.getLogger(ProjectManagementService.class);

  private static final long   serialVersionUID = -339555201;

  public ImageryProcessingJob()
  {
    super();
  }

  public static void processFiles(RequestParser parser, File imageryZip) throws FileNotFoundException
  {
    AbstractUploadTask task = ImageryWorkflowTask.getTaskByUploadId(parser.getUuid());

    try
    {
      String outFileNamePrefix = parser.getCustomParams().get("outFileName");
      String uploadTarget = parser.getUploadTarget();
      VaultFile vfImageryZip = VaultFile.createAndApply(parser.getFilename(), new FileInputStream(imageryZip));

      ImageryProcessingJob job = new ImageryProcessingJob();
      job.setRunAsUserId(Session.getCurrentSession().getUser().getOid());
      job.setWorkflowTask(task);
      job.setImageryFile(vfImageryZip.getOid());
      job.setUploadTarget(uploadTarget);
      job.setOutFileNamePrefix(outFileNamePrefix);
      job.apply();
      job.start();
    }
    catch (Throwable t)
    {
      task.lock();
      task.setStatus(WorkflowTaskStatus.ERROR.toString());
      task.setMessage("An error occurred while uploading the imagery to S3. " + t.getLocalizedMessage());
      task.apply();

      logger.error("An error occurred while uploading the imagery to S3.", t);
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
    this.uploadToS3(VaultFile.get(this.getImageryFile()), this.getUploadTarget(), this.getOutFileNamePrefix());
  }

  private void uploadToS3(VaultFile vfImageryZip, String uploadTarget, String outFileNamePrefix)
  {
    AbstractWorkflowTask task = this.getWorkflowTask();

    try
    {
      if (task instanceof ImageryWorkflowTask)
      {
        ImageryWorkflowTask imageryWorkflowTask = (ImageryWorkflowTask) task;

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

        event.setGeoprismUser(imageryWorkflowTask.getGeoprismUser());
        event.setUploadId(imageryWorkflowTask.getUploadId());
        event.setImagery(imageryWorkflowTask.getImagery());
        event.apply();

        event.handleUploadFinish(imageryWorkflowTask, uploadTarget, vfImageryZip, outFileNamePrefix);
      }
      else
      {
        WorkflowTask collectionWorkflowTask = (WorkflowTask) task;

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

        event.setGeoprismUser(collectionWorkflowTask.getGeoprismUser());
        event.setUploadId(collectionWorkflowTask.getUploadId());
        event.setComponent(collectionWorkflowTask.getComponent());
        event.apply();

        event.handleUploadFinish(collectionWorkflowTask, uploadTarget, vfImageryZip, outFileNamePrefix);
      }
    }
    catch (Throwable t)
    {
      task.lock();
      task.setStatus(WorkflowTaskStatus.ERROR.toString());
      task.setMessage("An error occurred while uploading the imagery to S3. " + t.getLocalizedMessage());
      task.apply();

      logger.error("An error occurred while uploading the imagery to S3.", t);

      if (Session.getCurrentSession() != null)
      {
        NotificationFacade.queue(new NotificationMessage(Session.getCurrentSession(), task.toJSON()));
      }
    }
    finally
    {
      vfImageryZip.delete();
    }
  }
}
