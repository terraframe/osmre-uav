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
package gov.geoplatform.uasdm.bus;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.resource.ApplicationFileResource;
import com.runwaysdk.resource.ArchiveFileResource;

import gov.geoplatform.uasdm.DevProperties;
import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask.WorkflowTaskStatus;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ImageryIF;
import gov.geoplatform.uasdm.model.ProcessConfiguration;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.odm.ImageryODMProcessingTask;
import gov.geoplatform.uasdm.odm.ODMProcessConfiguration;
import gov.geoplatform.uasdm.odm.ODMStatus;
import gov.geoplatform.uasdm.processing.raw.CollectionImageSizeCalculationProcessor;
import gov.geoplatform.uasdm.processing.raw.FileUploadProcessor;
import gov.geoplatform.uasdm.ws.GlobalNotificationMessage;
import gov.geoplatform.uasdm.ws.MessageType;
import gov.geoplatform.uasdm.ws.NotificationFacade;
import net.geoprism.GeoprismUser;

public class ImageryUploadEvent extends ImageryUploadEventBase
{
  public static final String[] formats          = new String[] { "jpeg", "jpg", "png", "gif", "bmp", "fits", "gray", "graya", "jng", "mono", "ico", "jbig", "tga", "tiff", "tif" };

  private static final Logger  logger           = LoggerFactory.getLogger(ImageryUploadEvent.class);

  private static final long    serialVersionUID = 510137801;

  public ImageryUploadEvent()
  {
    super();
  }

  /**
   * TODO : This code isn't even used anywhere yet we're still maintaining it.
   * 
   * @param task
   * @param uploadTarget
   * @param fileRes
   * @param configuration
   * @param processUpload
   */
  public void handleUploadFinish(ImageryWorkflowTask task, String uploadTarget, ApplicationFileResource fileRes, ProcessConfiguration configuration, Boolean processUpload)
  {
    task.lock();
    task.setStatus(WorkflowTaskStatus.PROCESSING.toString());
    task.setMessage("Processing archived files");
    task.apply();

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.JOB_CHANGE, null));

    ImageryIF imagery = task.getImageryInstance();

    List<String> files = new LinkedList<String>();

    if (DevProperties.uploadRaw())
    {
      files = new FileUploadProcessor().process(task, fileRes, imagery, uploadTarget, null);
    }

    if (fileRes instanceof ArchiveFileResource && (!DevProperties.uploadRaw() || Util.hasImages(files)))
    {
      var archive = (ArchiveFileResource) fileRes;
      
      new CollectionImageSizeCalculationProcessor().process(archive, imagery);

      task.lock();
      task.setStatus(WorkflowTaskStatus.COMPLETE.toString());
      task.setMessage("The upload successfully completed.  All files except those mentioned were archived.");
      task.apply();

      NotificationFacade.queue(new GlobalNotificationMessage(MessageType.JOB_CHANGE, null));

      // Only initialize an ortho job if imagery has been uploaded to the raw
      // folder.
      if (processUpload && ( uploadTarget != null && uploadTarget.equals(ImageryComponent.RAW) ))
      {
        if (configuration.isODM())
        {
          startODMProcessing(archive, task, configuration.toODM());
        }
        else if (configuration.isLidar())
        {
          // TODO : HEADS UP - HANDLE LIDAR PROCESSING
        }
      }
    }
  }

  private void startODMProcessing(ArchiveFileResource archive, ImageryWorkflowTask uploadTask, ODMProcessConfiguration configuration)
  {
    final ImageryIF imagery = uploadTask.getImageryInstance();

    ImageryODMProcessingTask task = new ImageryODMProcessingTask();
    task.setUploadId(uploadTask.getUploadId());
    task.setImagery(imagery.getOid());
    task.setGeoprismUser(GeoprismUser.getCurrentUser());
    task.setStatus(ODMStatus.RUNNING.getLabel());
    task.setTaskLabel("UAV data orthorectification for imagery [" + imagery.getName() + "]");
    task.setMessage("The images uploaded to ['" + imagery.getName() + "'] are submitted for orthorectification processing. Check back later for updates.");
    task.setConfiguration(configuration);
    task.apply();

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.JOB_CHANGE, null));

    task.initiate(archive);
  }

}
