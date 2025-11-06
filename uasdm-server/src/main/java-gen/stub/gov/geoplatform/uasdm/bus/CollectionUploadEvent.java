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

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.RunwayException;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.resource.ApplicationFileResource;
import com.runwaysdk.resource.ArchiveFileResource;
import com.runwaysdk.resource.FileResource;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.SingleActor;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.DevProperties;
import gov.geoplatform.uasdm.GenericException;
import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask.TaskActionType;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask.WorkflowTaskStatus;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.lidar.LidarProcessConfiguration;
import gov.geoplatform.uasdm.lidar.LidarProcessingTask;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ProcessConfiguration;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.odm.ODMProcessConfiguration;
import gov.geoplatform.uasdm.odm.ODMProcessingTask;
import gov.geoplatform.uasdm.odm.ODMStatus;
import gov.geoplatform.uasdm.processing.CogTifProcessor;
import gov.geoplatform.uasdm.processing.CogTifValidator;
import gov.geoplatform.uasdm.processing.raw.CollectionImageSizeCalculationProcessor;
import gov.geoplatform.uasdm.processing.raw.FileUploadProcessor;
import gov.geoplatform.uasdm.ws.MessageType;
import gov.geoplatform.uasdm.ws.NotificationFacade;
import gov.geoplatform.uasdm.ws.UserNotificationMessage;
import net.geoprism.GeoprismUser;

public class CollectionUploadEvent extends CollectionUploadEventBase
{
  private static final Logger logger           = LoggerFactory.getLogger(CollectionUploadEvent.class);

  private static final long   serialVersionUID = -285847093;

  public CollectionUploadEvent()
  {
    super();
  }

  public void handleUploadFinish(WorkflowTask task, String uploadTarget, ApplicationFileResource infile, Boolean processUpload, ProcessConfiguration configuration)
  {
    // if (Session.getCurrentSession() != null)
    // {
    // NotificationFacade.queue(new
    // NotificationMessage(Session.getCurrentSession(), task.toJSON()));
    // }

    UasComponentIF component = task.getComponentInstance();

    ProductIF product = null;

    task.lock();

    if (!uploadTarget.equals(ImageryComponent.RAW) && !uploadTarget.equals(ImageryComponent.VIDEO) && !uploadTarget.equals(ImageryComponent.GEOREF))
    {
      Optional<ProductIF> optional = component.getProduct(configuration.getProductName());

      // Delete the existing artifact if it exists
      optional.ifPresent(p -> {
        component.removeArtifacts(p, uploadTarget, true);
      });

      // If the product doesn't exist, then create it
      product = optional.orElseGet(() -> component.createProductIfNotExist(configuration.getProductName()));

      task.setProductId(product.getOid());
    }

    task.setStatus(WorkflowTaskStatus.PROCESSING.toString());
    task.setMessage("Processing archived files");
    task.apply();

    List<String> uploadedFiles = new LinkedList<String>();

    try
    {
      if (DevProperties.uploadRaw())
      {
        // TODO : processUpload is currently always set to true in
        // upload-modal.component.ts
        if (processUpload && ( uploadTarget.equals(ImageryComponent.ORTHO) || uploadTarget.equals(ImageryComponent.DEM) ) && ( infile.getNameExtension().equals("tif") || infile.getNameExtension().equals("tiff") ))
        {
          boolean isCog = new CogTifValidator().isValidCog(infile);

          if (isCog && !infile.getName().endsWith(CogTifProcessor.COG_EXTENSION))
          {
            File temp = new File(AppProperties.getTempDirectory(), Long.valueOf(new Random().nextInt()).toString());
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

            File temp = new File(AppProperties.getTempDirectory(), Long.valueOf(new Random().nextInt()).toString());
            temp.mkdir();

            File newfile = new File(temp, infile.getBaseName() + ".tif");

            FileUtils.copyFile(infile.getUnderlyingFile(), newfile);

            infile = new FileResource(newfile);
          }
        }

        uploadedFiles = new FileUploadProcessor().process(task, infile, (ImageryComponent) component, uploadTarget, product);
      }
    }
    catch (IOException ex)
    {
      logger.error("Error while uploading file", ex);

      task.lock();
      task.setStatus(WorkflowTaskStatus.ERROR.toString());
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
      if (configuration.isODM())
      {
        if (task.getProcessDem() || task.getProcessOrtho() || task.getProcessPtcloud())
        {
          var archive = (ArchiveFileResource) infile;
          
          startODMProcessing(archive, task, isMultispectral(component), configuration.toODM());

          new CollectionImageSizeCalculationProcessor().process(archive, (ImageryComponent) component);
        }
      }
      else if (configuration.isLidar())
      {
        if (configuration.toLidar().hasProcess())
        {
          startLidarProcessing(infile, task, configuration.toLidar());
        }
      }
    }
    else if (processUpload && ! ( uploadTarget.equals(ImageryComponent.RAW) || uploadTarget.equals(ImageryComponent.VIDEO) ))
    {
      this.startArtifactProcessing(task, infile, configuration.getProductName());
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

  private void startODMProcessing(ArchiveFileResource archive, WorkflowTask uploadTask, boolean isMultispectral, ODMProcessConfiguration configuration)
  {
    UasComponentIF component = uploadTask.getComponentInstance();

    gov.geoplatform.uasdm.graph.Product product = Product.find(component, configuration.getProductName());

    if (product != null && product.isLocked())
    {
      GenericException exception = new GenericException();
      exception.setUserMessage("The collection can not be processed because its product is locked.");
      throw exception;
    }

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
    task.setConfiguration(configuration);
    task.apply();

    task.initiate(archive, isMultispectral);
  }

  private void startLidarProcessing(ApplicationFileResource infile, WorkflowTask uploadTask, LidarProcessConfiguration configuration)
  {
    UasComponentIF component = uploadTask.getComponentInstance();
    
    LidarProcessingTask task = new LidarProcessingTask();
    task.setUploadId(uploadTask.getUploadId());
    task.setComponent(component.getOid());
    task.setGeoprismUser(this.getEventUser());
    task.setStatus(ODMStatus.RUNNING.getLabel());
    task.setProcessDem(uploadTask.getProcessDem());
    task.setProcessOrtho(uploadTask.getProcessOrtho());
    task.setProcessPtcloud(uploadTask.getProcessPtcloud());
    task.setTaskLabel("Lidar processing for collection [" + component.getName() + "]");
    task.setMessage("The point clouds uploaded to ['" + component.getName() + "'] are submitted for processing. Check back later for updates.");
    task.setConfiguration(configuration);
    task.apply();
    
    task.initiate(infile);
  }

  public void startArtifactProcessing(WorkflowTask uploadTask, ApplicationFileResource infile, String productName)
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
    task.setProductName(productName);

    component.getProduct(productName).ifPresent(product -> task.setProductName(product.getProductName()));

    task.apply();

    try
    {
      task.initiate(infile);
    }
    catch (Throwable t)
    {
      task.appLock();
      task.setStatus(ODMStatus.FAILED.getLabel());
      task.setMessage("An error was encountered while processing the imagery. " + RunwayException.localizeThrowable(t, CommonProperties.getDefaultLocale()));
      task.apply();
    }
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
