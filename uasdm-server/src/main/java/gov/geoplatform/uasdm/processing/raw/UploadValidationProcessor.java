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
package gov.geoplatform.uasdm.processing.raw;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.runwaysdk.RunwayException;
import com.runwaysdk.resource.ApplicationFileResource;
import com.runwaysdk.resource.ArchiveFileResource;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.resource.FileResource;
import com.runwaysdk.resource.ResourceException;
import com.runwaysdk.session.Session;

import gov.geoplatform.uasdm.ImageryProcessingJob;
import gov.geoplatform.uasdm.InvalidZipException;
import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.bus.AbstractUploadTask;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask.TaskActionType;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask.WorkflowTaskStatus;
import gov.geoplatform.uasdm.bus.CollectionUploadEvent;
import gov.geoplatform.uasdm.bus.ImageryWorkflowTask;
import gov.geoplatform.uasdm.bus.Mission;
import gov.geoplatform.uasdm.bus.WorkflowTask;
import gov.geoplatform.uasdm.graph.UasComponent;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ProcessConfiguration;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.view.SiteObjectsResultSet;
import gov.geoplatform.uasdm.ws.MessageType;
import gov.geoplatform.uasdm.ws.NotificationFacade;
import gov.geoplatform.uasdm.ws.UserNotificationMessage;

public class UploadValidationProcessor
{
  
  protected HashSet<String> filenameSet = new HashSet<String>();
  
  protected boolean isMultispectral;
  
  protected boolean isRadiometric;
  
  protected boolean isVideo;
  
  protected boolean isValid = true;
  
  protected ApplicationFileResource downstreamFile = null;
  
  public static boolean isMultispectral(AbstractWorkflowTask task)
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
  
  public static boolean isRadiometric(AbstractWorkflowTask task)
  {
    if (task instanceof ImageryWorkflowTask)
    {
      return false;
    }
    else
    {
      WorkflowTask collectionWorkflowTask = (WorkflowTask) task;

      var uasc = collectionWorkflowTask.getComponentInstance();
      
      if (uasc instanceof CollectionIF)
      {
        var format = ( (CollectionIF) uasc ).getFormat();
        
        if (format != null) {
          return format.isRadiometric();
        }
      }

      return false;
    }
  }
  
  public static boolean isVideo(AbstractWorkflowTask task)
  {
    if (task instanceof ImageryWorkflowTask)
    {
      return false;
    }
    else
    {
      WorkflowTask collectionWorkflowTask = (WorkflowTask) task;

      var uasc = collectionWorkflowTask.getComponentInstance();
      
      if (uasc instanceof CollectionIF)
      {
        var format = ( (CollectionIF) uasc ).getFormat();
        
        if (format != null) {
          return format.isVideo();
        }
      }

      return false;
    }
  }
  
  public ApplicationFileResource getDownstreamFile() { return downstreamFile; }

  @SuppressWarnings("resource")
  public boolean process(ApplicationFileResource uploaded, AbstractUploadTask task, ProcessConfiguration configuration)
  {
    this.isMultispectral = isMultispectral(task);
    this.isRadiometric= isRadiometric(task);
    this.isVideo = isVideo(task);
    
    if (!UasComponentIF.isValidName(configuration.getProductName())) {
      task.lock();
      task.setStatus(WorkflowTaskStatus.ERROR.toString());
      task.setMessage("Invalid Product Name [" + configuration.getProductName() + "]. No spaces or special characters such as <, >, -, +, =, !, @, #, $, %, ^, &, *, ?,/, \\ or apostrophes are allowed.");
      task.apply();
      
      isValid = false;
      return false;
    }
    
    if (uploaded instanceof ArchiveFileResource)
    {
      validateArchive((ArchiveFileResource) uploaded, task, configuration);
      this.downstreamFile = uploaded;
    }
    else
    {
      if (!validateFile(uploaded, task, configuration))
      {
        isValid = false;
        task.lock();
        task.setStatus(WorkflowTaskStatus.ERROR.toString());
        task.setMessage("The uploaded file did not pass validation. Check the messages for more information.");
        task.apply();
      }
      
      if (downstreamFile == null) downstreamFile = uploaded;
    }
    
    return isValid;
  }
  
  private void validateArchive(ArchiveFileResource archive, AbstractUploadTask task, ProcessConfiguration configuration)
  {
    try
    {
      Queue<ApplicationFileResource> queue = new LinkedList<>();
      int count = 0;
      
      queue.add(archive);
      
      while(!queue.isEmpty())
      {
        var res = queue.poll();
        
        if (res.hasChildren())
        {
          if (res != archive)
            task.createAction("We will be processing all files located in the directory [" + res.getName() + "] inside the uploaded archive.", TaskActionType.INFO);
          
          for (var child : res.getChildrenFiles())
            queue.add(child);
          
          continue;
        }
        
        if (validateFile(res, task, configuration)) {
          count++;
        }
      }
      
      if (count > 0 && configuration.isLidar() && task.getUploadTarget().equals(ImageryComponent.RAW))
      {
        List<String> incomingFiles = filenameSet.stream().filter(f -> {
          return f.toUpperCase().endsWith(".LAZ") || f.toUpperCase().endsWith(".LAS");
        }).collect(Collectors.toList());

        if (incomingFiles.size() > 1)
        {
          task.lock();
          task.setStatus(WorkflowTaskStatus.ERROR.toString());
          task.setMessage("The zip file contains more than a single point cloud file.  A Lidar collection can only have one raw point cloud.");
          task.apply();

          if (Session.getCurrentSession() != null)
          {
            NotificationFacade.queue(new UserNotificationMessage(Session.getCurrentSession(), MessageType.UPLOAD_JOB_CHANGE, task.toJSON()));
          }

          isValid = false;
          return;
        }

        UasComponent component = (UasComponent) task.getImageryComponent();

        SiteObjectsResultSet resultSet = component.getSiteObjects(ImageryComponent.RAW, null, null);

        List<String> existingFiles = resultSet.getObjects().stream().map(o -> o.getName()).filter(f -> {
          return f.toUpperCase().endsWith(".LAZ") || f.toUpperCase().endsWith(".LAS");
        }).filter(f -> !incomingFiles.contains(f)).collect(Collectors.toList());

        if (existingFiles.size() > 0)
        {
          task.lock();
          task.setStatus(WorkflowTaskStatus.ERROR.toString());
          task.setMessage("The zip file contains a new point cloud file with a different name than an existing point cloud file.  A Lidar collection can only have one raw point cloud.");
          task.apply();

          if (Session.getCurrentSession() != null)
          {
            NotificationFacade.queue(new UserNotificationMessage(Session.getCurrentSession(), MessageType.UPLOAD_JOB_CHANGE, task.toJSON()));
          }

          isValid = false;
          return;
        }

      }
      else if (count == 0)
      {
        List<String> extensions = ImageryProcessingJob.getSupportedExtensions(task.getUploadTarget(), isMultispectral, isRadiometric, isVideo, configuration);
        
        String msg = "The zip did not contain any files to process. Files must follow proper naming conventions and end in one of the following file extensions: " + StringUtils.join(extensions, ", ");
        
        if (isMultispectral)
          msg = "You selected a multispectral sensor. " + msg;
        else if (isRadiometric)
          msg = "You selected a radiometric sensor. " + msg;
        
        task.lock();
        task.setStatus(WorkflowTaskStatus.ERROR.toString());
        task.setMessage(msg);
        task.apply();

        if (Session.getCurrentSession() != null)
        {
          NotificationFacade.queue(new UserNotificationMessage(Session.getCurrentSession(), MessageType.UPLOAD_JOB_CHANGE, task.toJSON()));
        }

        isValid = false;
        return;
      }
      
      if (Session.getCurrentSession() != null)
      {
        NotificationFacade.queue(new UserNotificationMessage(Session.getCurrentSession(), MessageType.UPLOAD_JOB_CHANGE, task.toJSON()));
      }
    }
    catch (Throwable t)
    {
      task.createAction(RunwayException.localizeThrowable(t, Session.getCurrentLocale()), TaskActionType.ERROR);

      throw new InvalidZipException(t);
    }
  }
  
  private boolean validateFile(ApplicationFileResource res, AbstractUploadTask task, ProcessConfiguration configuration)
  {
    String finalName = res.getName();
    final String uploadTarget = task.getUploadTarget();
    
    if (!Mission.ACCESSIBLE_SUPPORT.equals(uploadTarget))
    {
      if (Util.isImageFile(res.getName()))
      {
        try
        {
          ImageIO.read(res.getUnderlyingFile());
        }
        catch (Exception e)
        {
          task.lock();
          task.setStatus(WorkflowTaskStatus.ERROR.toString());
          task.setMessage("The file [" + res.getName() + " is not a valid image file]");
          task.apply();
          
          return false;
        }
      }
  
      if (!UasComponentIF.isValidName(res.getName()))
      {
        task.createAction("The filename [" + res.getName() + "] contains special characters which will be replaced with an underscore.", TaskActionType.ERROR);
  
        finalName = res.getName().replaceAll(UasComponentIF.DISALLOWED_FILENAME_REGEX, "_");
      }
  
      if (configuration.isODM() || configuration.isLidar())
      {
        final String ext = res.getNameExtension().toLowerCase();
        final List<String> extensions = ImageryProcessingJob.getSupportedExtensions(task.getUploadTarget(), isMultispectral, isRadiometric, isVideo, configuration);
        final boolean isGeo = configuration.isODM() && ( res.getName().equalsIgnoreCase(configuration.toODM().getGeoLocationFileName()) && configuration.toODM().isIncludeGeoLocationFile() );
        final boolean isGcp = configuration.isODM() && ( res.getName().equalsIgnoreCase(configuration.toODM().getGroundControlPointFileName()) && configuration.toODM().isIncludeGroundControlPointFile() );
  
        if (isGeo)
        {
          finalName = "geo.txt";
        }
        else if (isGcp)
        {
          finalName = "gcp_list.txt";
        }
        else if (!extensions.contains(ext))
        {
          String msg = "The file [" + res.getName() + "] is of an unsupported format and will be ignored. The following formats are supported: " + StringUtils.join(extensions, ", ");
          
          if (isMultispectral)
            msg = "You selected a multispectral sensor. " + msg;
          else if (isRadiometric)
            msg = "You selected a radiometric sensor. " + msg;
          
          task.createAction(msg, TaskActionType.ERROR);
          return false;
        }
      }
    }
    
    if (!filenameSet.add(finalName))
    {
      task.createAction("The filename [" + finalName + "] conflicts with another name in the uploaded archive. This conflict may be a result of inner directories or special characters which cannot be represented in the final collection. This will result in missing files.", TaskActionType.ERROR);
      return false;
    }
    
    if (!finalName.equals(res.getName()))
    {
      if (res.getParentFile().isPresent()) {
        var newFile = new File(res.getParentFile().orElseThrow().getUnderlyingFile(), finalName);
        res.getUnderlyingFile().renameTo(newFile);
        this.downstreamFile = new FileResource(new CloseableFile(newFile));
      } else {
        try
        {
          var parent = Files.createTempDirectory(res.getName()).toFile();
          var newFile = new File(parent, finalName);
          IOUtils.copy(res.openNewStream(), new FileOutputStream(newFile));
          this.downstreamFile = new FileResource(new CloseableFile(newFile));
        }
        catch (IOException e)
        {
          throw new ResourceException(e);
        }
      }
    }

    return true;
  }
  
}
