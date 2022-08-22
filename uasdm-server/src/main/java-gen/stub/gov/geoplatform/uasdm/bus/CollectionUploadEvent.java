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
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.RunwayException;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.resource.ApplicationFileResource;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.resource.FileResource;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.SingleActor;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.DevProperties;
import gov.geoplatform.uasdm.MetadataXMLGenerator;
import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask.TaskActionType;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask.WorkflowTaskStatus;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.odm.ODMProcessingTask;
import gov.geoplatform.uasdm.odm.ODMStatus;
import gov.geoplatform.uasdm.processing.CogTifProcessor;
import gov.geoplatform.uasdm.processing.CogTifValidator;
import gov.geoplatform.uasdm.view.FlightMetadata;
import gov.geoplatform.uasdm.ws.MessageType;
import gov.geoplatform.uasdm.ws.NotificationFacade;
import gov.geoplatform.uasdm.ws.UserNotificationMessage;
import net.geoprism.GeoprismUser;
import net.lingala.zip4j.ZipFile;

public class CollectionUploadEvent extends CollectionUploadEventBase
{
  private static final Logger logger = LoggerFactory.getLogger(CollectionUploadEvent.class);

  private static final long serialVersionUID = -285847093;

  public CollectionUploadEvent()
  {
    super();
  }

  public void handleUploadFinish(WorkflowTask task, String uploadTarget, ApplicationFileResource infile, String outFileNamePrefix, Boolean processUpload)
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

    if (!uploadTarget.equals(ImageryComponent.RAW))
    {
      component.removeArtifacts(uploadTarget);
    }

    List<String> uploadedFiles = new LinkedList<String>();

    try
    {
      if (DevProperties.uploadRaw())
      {
        if (processUpload && ( uploadTarget.equals(ImageryComponent.ORTHO) || uploadTarget.equals(ImageryComponent.DEM) ) && ( infile.getNameExtension().equals("tif") || infile.getNameExtension().equals("tiff") ))
        {
          boolean isCog = new CogTifValidator().isValidCog(infile);
          
          if (isCog && !infile.getName().endsWith(CogTifProcessor.COG_EXTENSION))
          {
            File temp = new File(AppProperties.getTempDirectory(), new Long(new Random().nextInt()).toString());
            temp.mkdir();

            File newfile = new File(temp, infile.getBaseName() + CogTifProcessor.COG_EXTENSION);

            FileUtils.copyFile(infile.getUnderlyingFile(), newfile);

            infile = new FileResource(newfile);
          }
          else if (!isCog && infile.getName().endsWith(CogTifProcessor.COG_EXTENSION))
          {
            task.lock();
            task.createAction("Uploaded file ends with cog extension, but did not pass cog validation.", TaskActionType.ERROR.getType());
            task.apply();

            File temp = new File(AppProperties.getTempDirectory(), new Long(new Random().nextInt()).toString());
            temp.mkdir();

            File newfile = new File(temp, infile.getBaseName() + ".tif");

            FileUtils.copyFile(infile.getUnderlyingFile(), newfile);

            infile = new FileResource(newfile);
          }
        }

        uploadedFiles = component.uploadArchive(task, infile, uploadTarget);
      }
    }
    catch (IOException ex)
    {
      logger.error("Error while uploading file", ex);

      task.lock();
      task.createAction("Error while uploading file " + RunwayException.localizeThrowable(ex, Locale.US), TaskActionType.ERROR.getType());
      task.apply();

      return;
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
    else if (processUpload && ! ( uploadTarget.equals(ImageryComponent.RAW) || uploadTarget.equals(ImageryComponent.VIDEO) ))
    {
      this.startOrthoProcessing(task, infile);
    }

    if (uploadedFiles.size() > 0)
    {
      MissingUploadMessage.remove(component);
    }
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
    task.setGeoprismUser(this.getEventUser());
    task.setStatus(ODMStatus.RUNNING.getLabel());
    task.setProcessDem(uploadTask.getProcessDem());
    task.setProcessOrtho(uploadTask.getProcessOrtho());
    task.setProcessPtcloud(uploadTask.getProcessPtcloud());
    task.setTaskLabel("UAV data orthorectification for collection [" + component.getName() + "]");
    task.setMessage("The images uploaded to ['" + component.getName() + "'] are submitted for orthorectification processing. Check back later for updates.");
    task.setFilePrefix(outFileNamePrefix);
    task.apply();

    task.initiate(infile, isMultispectral);
  }

  private void calculateImageSize(ApplicationFileResource zip, CollectionIF collection)
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

  public void startOrthoProcessing(WorkflowTask uploadTask, ApplicationFileResource infile)
  {
    UasComponentIF component = uploadTask.getComponentInstance();

    OrthoProcessingTask task = new OrthoProcessingTask();
    task.setUploadId(uploadTask.getUploadId());
    task.setComponent(component.getOid());
    task.setGeoprismUser(this.getEventUser());
    task.setStatus(ODMStatus.RUNNING.getLabel());
    task.setProcessDem(uploadTask.getProcessDem());
    task.setProcessOrtho(uploadTask.getProcessOrtho());
    task.setProcessPtcloud(uploadTask.getProcessPtcloud());
    task.setUploadTarget(uploadTask.getUploadTarget());
    task.setTaskLabel("Ortho processesing task for collection [" + component.getName() + "]");
    task.setMessage("The ortho uploaded to ['" + component.getName() + "'] is being processed. Check back later for updates.");
    task.apply();

    task.initiate(infile);
  }

  private SingleActor getEventUser()
  {
    SingleActor user = GeoprismUser.getCurrentUser();

    if (user == null)
    {
      user = this.getGeoprismUser();
    }
    return user;
  }

  public static CollectionUploadEvent getByUploadId(String uploadId)
  {
    CollectionUploadEventQuery query = new CollectionUploadEventQuery(new QueryFactory());
    query.WHERE(query.getUploadId().EQ(uploadId));

    try (OIterator<? extends CollectionUploadEvent> iterator = query.getIterator())
    {
      if (iterator.hasNext())
      {
        return iterator.next();
      }
    }

    return null;
  }
}
