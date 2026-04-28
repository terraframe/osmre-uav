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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.runwaysdk.RunwayException;
import com.runwaysdk.resource.ApplicationFileResource;
import com.runwaysdk.resource.ArchiveFileResource;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.resource.FileResource;
import com.runwaysdk.resource.ResourceException;
import com.runwaysdk.session.Session;

import gov.geoplatform.uasdm.GenericException;
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
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.graph.CollectionFormat;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.graph.UasComponent;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ProcessConfiguration;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.odm.GeoLocationFileMissingException;
import gov.geoplatform.uasdm.odm.ODMProcessConfiguration;
import gov.geoplatform.uasdm.odm.ODMProcessConfiguration.FileFormat;
import gov.geoplatform.uasdm.processing.gcp.GroundControlPointFileValidator;
import gov.geoplatform.uasdm.processing.geolocation.GeoLocationFileValidator;
import gov.geoplatform.uasdm.processing.geolocation.RX1R2GeoFileConverter;
import gov.geoplatform.uasdm.resource.EditableArchiveFileResource;
import gov.geoplatform.uasdm.service.ProjectManagementService;
import gov.geoplatform.uasdm.view.SiteObjectsResultSet;
import gov.geoplatform.uasdm.ws.MessageType;
import gov.geoplatform.uasdm.ws.NotificationFacade;
import gov.geoplatform.uasdm.ws.UserNotificationMessage;
import jakarta.inject.Inject;

@Service
public class UploadValidationProcessor
{
  @Inject
  private ProjectManagementService pms;
  
  private static class ValidationContext {
    private final Set<String> filenameSet = new HashSet<>();

    private boolean isMultispectral;
    private boolean isRadiometric;
    private boolean isVideo;
    private CollectionFormat format;

    private boolean isValid = true;

    private ApplicationFileResource rootFile;
    private ApplicationFileResource downstreamFile;
    private ApplicationFileResource detectedGeoLocationFile;
    private ApplicationFileResource detectedGCPFile;
  }
  
  public static class UploadValidationResult {
    private final boolean valid;
    private final ApplicationFileResource downstreamFile;
    private final ApplicationFileResource detectedGeoLocationFile;
    private final ApplicationFileResource detectedGCPFile;

    public UploadValidationResult(
        boolean valid,
        ApplicationFileResource downstreamFile,
        ApplicationFileResource detectedGeoLocationFile,
        ApplicationFileResource detectedGCPFile
    ) {
      this.valid = valid;
      this.downstreamFile = downstreamFile;
      this.detectedGeoLocationFile = detectedGeoLocationFile;
      this.detectedGCPFile = detectedGCPFile;
    }

    public boolean isValid() {
      return valid;
    }

    public ApplicationFileResource getDownstreamFile() {
      return downstreamFile;
    }

    public ApplicationFileResource getDetectedGeoLocationFile() {
      return detectedGeoLocationFile;
    }

    public ApplicationFileResource getDetectedGCPFile() {
      return detectedGCPFile;
    }
  }
  
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

      UasComponentIF uasc = collectionWorkflowTask.getComponentInstance();
      
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
  
  public static CollectionFormat getCollectionFormat(AbstractWorkflowTask task)
  {
    if (task instanceof ImageryWorkflowTask) return null;
    
    WorkflowTask collectionWorkflowTask = (WorkflowTask) task;

    var uasc = collectionWorkflowTask.getComponentInstance();
    
    if (uasc instanceof CollectionIF)
    {
      var format = ( (CollectionIF) uasc ).getFormat();
      
      return format;
    }

    return null;
  }
  
  @SuppressWarnings("resource")
  public UploadValidationResult process(ApplicationFileResource uploaded, AbstractUploadTask task, ProcessConfiguration configuration)
  {
    ValidationContext ctx = new ValidationContext();

    ctx.rootFile = uploaded;
    ctx.isMultispectral = isMultispectral(task);
    ctx.isRadiometric = isRadiometric(task);
    ctx.isVideo = isVideo(task);
    ctx.format = getCollectionFormat(task);
    
    if (!UasComponentIF.isValidName(configuration.getProductName())) {
      task.lock();
      task.setStatus(WorkflowTaskStatus.ERROR.toString());
      task.setMessage("Invalid Product Name [" + configuration.getProductName() + "]. No spaces or special characters such as <, >, -, +, =, !, @, #, $, %, ^, &, *, ?,/, \\ or apostrophes are allowed.");
      task.apply();
      
      ctx.isValid = false;
    }
    
    if (uploaded instanceof ArchiveFileResource)
    {
      validateArchive(ctx, (ArchiveFileResource) uploaded, task, configuration);
      ctx.downstreamFile = uploaded;
    }
    else
    {
      if (!validateFile(ctx, uploaded, task, configuration))
      {
        ctx.isValid = false;
        task.lock();
        task.setStatus(WorkflowTaskStatus.ERROR.toString());
        task.setMessage("The uploaded file did not pass validation. Check the messages for more information.");
        task.apply();
      }
      
      if (ctx.downstreamFile == null) ctx.downstreamFile = uploaded;
    }
    
    handleGeoLocationAndGcp(ctx, task, configuration);
    
    if (!ctx.isValid && !WorkflowTaskStatus.ERROR.toString().equals(task.getStatus())) {
      task.lock();
      task.setStatus(WorkflowTaskStatus.ERROR.toString());
      task.setMessage("The uploaded file did not pass validation. Check the messages for more information.");
      task.apply();
    }
    
    return new UploadValidationResult(
        ctx.isValid,
        ctx.downstreamFile,
        ctx.detectedGeoLocationFile,
        ctx.detectedGCPFile
    );
  }
  
  private void handleGeoLocationAndGcp(ValidationContext ctx, AbstractUploadTask task, ProcessConfiguration configuration) {
    try {
      ArchiveFileResource archive = null;
      
      if (configuration.isODM()) {
        // The validator requires all the files. So if they only upload the glf we'll have to validate on next upload.
        if (ctx.rootFile instanceof ArchiveFileResource) {
          archive = (ArchiveFileResource) ctx.rootFile;
        } else {
          archive = pms.downloadAllImagery(task.getImageryComponent().getUasComponent(), null);
          
          if (pms.fileNamesInArchive(archive).size() == 0) {
            archive = null;
          } else if (ctx.detectedGeoLocationFile == null) {
            ctx.detectedGeoLocationFile = findGlf(archive);
          }
        }
      
        var odmConfig = configuration.toODM();
        
        // GeoLocation (aka geologger) files
        if (ctx.detectedGeoLocationFile == null)
        {
          if (odmConfig.isIncludeGeoLocationFile()) {
            throw new GeoLocationFileMissingException(odmConfig.getGeoLocationFileName());
          } else if (sensorRequiresGLF(task)) {
            task.createAction("Your collection references a sensor which generates a geo location (geologger) file, but you did not select one on upload. Please upload a geo location file before processing.", TaskActionType.WARNING);
          }
        }
        else { // We actually can't check against the "isIncludeGeoLocationFile" flag here, because they could have uploaded on a previous run and now they're uploading imagery. 
          // Validate
          if (archive != null)
            GeoLocationFileValidator.validate(odmConfig.getGeoLocationFormat(), ctx.detectedGeoLocationFile, archive, task);
          
          // Convert
          if (odmConfig.getGeoLocationFormat().equals(FileFormat.RX1R2))
          {
            try (RX1R2GeoFileConverter reader = RX1R2GeoFileConverter.open(ctx.detectedGeoLocationFile.openNewStream()))
            {
              try (
                  FileInputStream in = new FileInputStream(reader.getOutput());
                  FileOutputStream out = new FileOutputStream(ctx.detectedGeoLocationFile.getUnderlyingFile())
              ) {
                IOUtils.copy(in, out);
              }
            }
          }
        }
        
        // Validate GCP
        if (odmConfig.isIncludeGroundControlPointFile()) {
          if (ctx.detectedGCPFile == null)
          {
            throw new GenericException("Ground control point file is required");
          }
          
          GroundControlPointFileValidator.validate(ODMProcessConfiguration.FileFormat.ODM, ctx.detectedGCPFile, archive, task);
        }
      }
    }
    catch (Throwable t) {
      ctx.isValid = false;
      task.createAction(RunwayException.localizeThrowable(t, Session.getCurrentLocale()), TaskActionType.ERROR);
    }
  }
    
  public ApplicationFileResource findGlf(ArchiveFileResource archive) {
    ApplicationFileResource result = null;
    
    Queue<ApplicationFileResource> queue = new LinkedList<>();
    queue.add(archive);
    while(!queue.isEmpty())
    {
      var res = queue.poll();
      
      if (res.hasChildren())
      {
        for (var child : res.getChildrenFiles())
          queue.add(child);
        
        continue;
      }
      
      if (res.getName().equals(Product.GEO_LOCATION_FILE))
        result = res;
    }
    
    return result;
  }
  
  private boolean sensorRequiresGLF(AbstractUploadTask task) {
    ImageryComponent component = task.getImageryComponent();
    
    if (component instanceof Collection) {
      return Boolean.TRUE.equals(( (Collection) component ).getSensor().getHasGeologger());
    }
    
    return false;
  }
  
  private void validateArchive(ValidationContext ctx, ArchiveFileResource archive, AbstractUploadTask task, ProcessConfiguration configuration)
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
        
        if (validateFile(ctx, res, task, configuration)) {
          count++;
        } else {
          removeFromProcessing(ctx, res);
        }
      }
      
      if (count > 0 && configuration.isLidar() && task.getUploadTarget().equals(ImageryComponent.RAW))
      {
        List<String> incomingFiles = ctx.filenameSet.stream().filter(f -> {
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

          ctx.isValid = false;
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

          ctx.isValid = false;
          return;
        }

      }
      else if (count == 0)
      {
        List<String> extensions = getSupportedExtensions(ctx, task, configuration);
        
        String msg = "The zip did not contain any files to process. Files must follow proper naming conventions and end in one of the following file extensions: " + StringUtils.join(extensions, ", ");
        
        if (ctx.isMultispectral)
          msg = "You selected a multispectral sensor. " + msg;
        else if (ctx.isRadiometric)
          msg = "You selected a radiometric sensor. " + msg;
        
        task.lock();
        task.setStatus(WorkflowTaskStatus.ERROR.toString());
        task.setMessage(msg);
        task.apply();

        if (Session.getCurrentSession() != null)
        {
          NotificationFacade.queue(new UserNotificationMessage(Session.getCurrentSession(), MessageType.UPLOAD_JOB_CHANGE, task.toJSON()));
        }

        ctx.isValid = false;
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
  
  protected List<String> getSupportedExtensions(ValidationContext ctx, AbstractUploadTask task, ProcessConfiguration configuration)
  {
    if (ctx.format != null)
      return ImageryProcessingJob.getSupportedExtensions(task.getUploadTarget(), ctx.format, configuration);
    else
      return ImageryProcessingJob.getSupportedExtensions(task.getUploadTarget(), ctx.isMultispectral, ctx.isRadiometric, ctx.isVideo, configuration);
  }
  
  private boolean validateFile(ValidationContext ctx, ApplicationFileResource res, AbstractUploadTask task, ProcessConfiguration configuration)
  {
    String finalName = res.getName();
    final String uploadTarget = task.getUploadTarget();
    
    boolean isGeo = false;
    boolean isGcp = false;
    
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
        final List<String> extensions = getSupportedExtensions(ctx, task, configuration);
        isGeo = res.getName().equals("geo.txt") || (configuration.isODM() && ( res.getName().equalsIgnoreCase(configuration.toODM().getGeoLocationFileName()) && configuration.toODM().isIncludeGeoLocationFile() ));
        isGcp = res.getName().equals("gcp_list.txt") || (configuration.isODM() && ( res.getName().equalsIgnoreCase(configuration.toODM().getGroundControlPointFileName()) && configuration.toODM().isIncludeGroundControlPointFile() ));
  
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
          
          if (ctx.isMultispectral)
            msg = "You selected a multispectral sensor. " + msg;
          else if (ctx.isRadiometric)
            msg = "You selected a radiometric sensor. " + msg;
          
          task.createAction(msg, TaskActionType.ERROR);
          return false;
        }
      }
    }
    
    if (!ctx.filenameSet.add(finalName))
    {
      task.createAction("The filename [" + finalName + "] conflicts with another name in the uploaded archive. This conflict may be a result of inner directories or special characters which cannot be represented in the final collection. This will result in missing files.", TaskActionType.ERROR);
      return false;
    }
    
    if (!finalName.equals(res.getName()))
    {
      if (res.getParentFile().isPresent()) {
        var newFile = new File(res.getParentFile().orElseThrow().getUnderlyingFile(), finalName);
        res.getUnderlyingFile().renameTo(newFile);
        res = new FileResource(new CloseableFile(newFile));
        ctx.downstreamFile = res;
      } else {
        try
        {
          var parent = Files.createTempDirectory(res.getName()).toFile();
          var newFile = new File(parent, finalName);
          try (var in = res.openNewStream(); var out = new FileOutputStream(newFile) ) { IOUtils.copy(in, out); }
          res = new FileResource(new CloseableFile(newFile));
          ctx.downstreamFile = res;
        }
        catch (IOException e)
        {
          throw new ResourceException(e);
        }
      }
    }
    
    if (isGeo) {
      ctx.detectedGeoLocationFile = res;
    } else if (isGcp) {
      ctx.detectedGCPFile = res;
    }

    return true;
  }
  
  protected void removeFromProcessing(ValidationContext ctx, ApplicationFileResource res)
  {
    if (ctx.rootFile instanceof EditableArchiveFileResource)
    {
      ( (EditableArchiveFileResource) ctx.rootFile ).exclude(res);
    }
  }
  
}
