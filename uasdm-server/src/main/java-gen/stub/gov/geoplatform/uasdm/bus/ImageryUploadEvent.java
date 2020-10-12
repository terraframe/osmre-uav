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

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.resource.ApplicationResource;

import gov.geoplatform.uasdm.DevProperties;
import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask.WorkflowTaskStatus;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ImageryIF;
import gov.geoplatform.uasdm.odm.ImageryODMProcessingTask;
import gov.geoplatform.uasdm.odm.ODMStatus;
import gov.geoplatform.uasdm.ws.GlobalNotificationMessage;
import gov.geoplatform.uasdm.ws.MessageType;
import gov.geoplatform.uasdm.ws.NotificationFacade;
import net.geoprism.GeoprismUser;
import net.lingala.zip4j.ZipFile;

public class ImageryUploadEvent extends ImageryUploadEventBase
{
  public static final String[] formats          = new String[] { "jpeg", "jpg", "png", "gif", "bmp", "fits", "gray", "graya", "jng", "mono", "ico", "jbig", "tga", "tiff", "tif" };

  private static final Logger  logger           = LoggerFactory.getLogger(ImageryUploadEvent.class);

  private static final long    serialVersionUID = 510137801;

  public ImageryUploadEvent()
  {
    super();
  }

  public void handleUploadFinish(ImageryWorkflowTask task, String uploadTarget, ApplicationResource appRes, String outFileNamePrefix, Boolean processUpload)
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
      files = imagery.uploadArchive(task, appRes, uploadTarget);
    }

    if (!DevProperties.uploadRaw() || Util.hasImages(files))
    {
      calculateImageSize(appRes, imagery);

      task.lock();
      task.setStatus(WorkflowTaskStatus.COMPLETE.toString());
      task.setMessage("The upload successfully completed.  All files except those mentioned were archived.");
      task.apply();

      NotificationFacade.queue(new GlobalNotificationMessage(MessageType.JOB_CHANGE, null));

      // Only initialize an ortho job if imagery has been uploaded to the raw
      // folder.
      if (processUpload && ( uploadTarget != null && uploadTarget.equals(ImageryComponent.RAW) ))
      {
        startODMProcessing(appRes, task, outFileNamePrefix);
      }
    }
  }

  private void startODMProcessing(ApplicationResource appRes, ImageryWorkflowTask uploadTask, String outFileNamePrefix)
  {
    final ImageryIF imagery = uploadTask.getImageryInstance();

    ImageryODMProcessingTask task = new ImageryODMProcessingTask();
    task.setUploadId(uploadTask.getUploadId());
    task.setImagery(imagery.getOid());
    task.setGeoprismUser((GeoprismUser) GeoprismUser.getCurrentUser());
    task.setStatus(ODMStatus.RUNNING.getLabel());
    task.setTaskLabel("UAV data orthorectification for imagery [" + imagery.getName() + "]");
    task.setMessage("The images uploaded to ['" + imagery.getName() + "'] are submitted for orthorectification processing. Check back later for updates.");
    task.setFilePrefix(outFileNamePrefix);
    task.apply();

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.JOB_CHANGE, null));

    task.initiate(appRes);
  }

  private void calculateImageSize(ApplicationResource zip, ImageryIF imagery)
  {
    try
    {
      File parentFolder = new File(FileUtils.getTempDirectory(), zip.getName());

      try
      {
        new ZipFile(zip.getUnderlyingFile()).extractAll(parentFolder.getAbsolutePath());

        File[] files = parentFolder.listFiles();

        for (File file : files)
        {
          String ext = FilenameUtils.getExtension(file.getName()).toLowerCase();
          if (ArrayUtils.contains(ImageryUploadEvent.formats, ext))
          {
            BufferedImage bimg = ImageIO.read(file);
            int width = bimg.getWidth();
            int height = bimg.getHeight();

            imagery.appLock();
            imagery.setImageHeight(height);
            imagery.setImageWidth(width);
            imagery.apply();

            return;
          }
        }
      }
      finally
      {
        FileUtils.deleteQuietly(parentFolder);
        zip.close();
      }
    }
    catch (Throwable e)
    {
      logger.error("Error occurred while calculating the image size.", e);
    }
  }

}
