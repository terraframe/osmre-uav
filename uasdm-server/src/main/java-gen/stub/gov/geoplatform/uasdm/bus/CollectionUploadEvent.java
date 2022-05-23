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
import java.io.FilenameFilter;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.session.Session;

import gov.geoplatform.uasdm.DevProperties;
import gov.geoplatform.uasdm.MetadataXMLGenerator;
import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask.WorkflowTaskStatus;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.odm.ODMProcessingTask;
import gov.geoplatform.uasdm.odm.ODMStatus;
import gov.geoplatform.uasdm.view.FlightMetadata;
import gov.geoplatform.uasdm.ws.MessageType;
import gov.geoplatform.uasdm.ws.NotificationFacade;
import gov.geoplatform.uasdm.ws.UserNotificationMessage;
import net.geoprism.GeoprismUser;
import net.lingala.zip4j.ZipFile;

public class CollectionUploadEvent extends CollectionUploadEventBase
{
  private static final Logger logger           = LoggerFactory.getLogger(CollectionUploadEvent.class);

  private static final long   serialVersionUID = -285847093;

  public CollectionUploadEvent()
  {
    super();
  }

  public void handleUploadFinish(WorkflowTask task, String uploadTarget, ApplicationResource infile, String outFileNamePrefix, Boolean processUpload)
  {
    task.lock();
    task.setStatus(WorkflowTaskStatus.PROCESSING.toString());
    task.setMessage("Processing archived files");
    task.apply();

    // if (Session.getCurrentSession() != null)
    // {
    // NotificationFacade.queue(new
    // NotificationMessage(Session.getCurrentSession(), task.toJSON()));
    // }

    UasComponentIF component = task.getComponentInstance();

    List<String> uploadedFiles = new LinkedList<String>();

    if (DevProperties.uploadRaw())
    {
      uploadedFiles = component.uploadArchive(task, infile, uploadTarget);
    }

    task.lock();
    task.setStatus(WorkflowTaskStatus.COMPLETE.toString());
    task.setMessage("The upload successfully completed.  All files except those mentioned were archived.");
    task.apply();

    if (Session.getCurrentSession() != null)
    {
      NotificationFacade.queue(new UserNotificationMessage(Session.getCurrentSession(), MessageType.UPLOAD_JOB_CHANGE, task.toJSON()));
    }

    // Only upload to ODM if there are valid image files which were successfully
    // uploaded to s3
    if (processUpload && uploadTarget.equals(ImageryComponent.RAW) && ( !DevProperties.uploadRaw() || Util.hasImages(uploadedFiles) ))
    {
      startODMProcessing(infile, task, outFileNamePrefix, isMultispectral(component));

      if (component instanceof CollectionIF)
      {
        calculateImageSize(infile, (CollectionIF) component);
      }
    }

    // handleMetadataWorkflow(task);
  }

  public static boolean isMultispectral(UasComponentIF uasc)
  {
    if (uasc instanceof CollectionIF)
    {
      return ( (CollectionIF) uasc ).isMultiSpectral();
    }

    return false;
  }

  private void startODMProcessing(ApplicationResource infile, WorkflowTask uploadTask, String outFileNamePrefix, boolean isMultispectral)
  {
    UasComponentIF component = uploadTask.getComponentInstance();

    ODMProcessingTask task = new ODMProcessingTask();
    task.setUploadId(uploadTask.getUploadId());
    task.setComponent(component.getOid());
    task.setGeoprismUser(GeoprismUser.getCurrentUser());
    task.setStatus(ODMStatus.RUNNING.getLabel());
    task.setTaskLabel("UAV data orthorectification for collection [" + component.getName() + "]");
    task.setMessage("The images uploaded to ['" + component.getName() + "'] are submitted for orthorectification processing. Check back later for updates.");
    task.setFilePrefix(outFileNamePrefix);
    task.apply();

    task.initiate(infile, isMultispectral);
  }

  private void calculateImageSize(ApplicationResource zip, CollectionIF collection)
  {
    try
    {
      File parentFolder = new File(FileUtils.getTempDirectory(), zip.getName());

      try
      {
        new ZipFile(zip.getUnderlyingFile()).extractAll(parentFolder.getAbsolutePath());

        File[] files = parentFolder.listFiles(new FilenameFilter()
        {
          @Override
          public boolean accept(File dir, String name)
          {
            String ext = FilenameUtils.getExtension(name).toLowerCase();

            return ArrayUtils.contains(ImageryUploadEvent.formats, ext);
          }
        });

        if (files.length > 0)
        {
          File file = files[0];
          BufferedImage bimg = ImageIO.read(file);

          int width = bimg.getWidth();
          int height = bimg.getHeight();

          collection.appLock();
          collection.setImageHeight(height);
          collection.setImageWidth(width);
          collection.apply();

          // Update the metadata
          FlightMetadata metadata = FlightMetadata.get(collection, Collection.RAW, collection.getFolderName() + MetadataXMLGenerator.FILENAME);

          if (metadata != null)
          {
            metadata.getSensor().setImageWidth(Integer.toString(width));
            metadata.getSensor().setImageHeight(Integer.toString(height));

            MetadataXMLGenerator generator = new MetadataXMLGenerator();
            generator.generateAndUpload(collection, metadata);
          }

        }
      }
      finally
      {
        FileUtils.deleteQuietly(parentFolder);
      }
    }
    catch (Throwable e)
    {
      logger.error("Error occurred while calculating the image size.", e);
    }
  }

  // private void handleMetadataWorkflow(WorkflowTask uploadTask)
  // {
  // if (this.getCollection().getMetadataUploaded())
  // {
  // WorkflowTask task = new WorkflowTask();
  // task.setUploadId(uploadTask.getUploadId());
  // task.setCollectionId(uploadTask.getCollectionOid());
  // task.setGeoprismUser(uploadTask.getGeoprismUser());
  // task.setWorkflowType(WorkflowTask.NEEDS_METADATA);
  // task.setStatus("Message");
  // task.setTaskLabel("Missing Metadata");
  // task.setMessage("Metadata is missing for Collection [" +
  // uploadTask.getCollection().getName() + "].");
  // task.apply();
  // }
  // }

}
