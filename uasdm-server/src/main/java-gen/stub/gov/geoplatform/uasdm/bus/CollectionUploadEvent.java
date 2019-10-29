package gov.geoplatform.uasdm.bus;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.resource.ApplicationResource;

import gov.geoplatform.uasdm.DevProperties;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask.WorkflowTaskStatus;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.odm.ODMProcessingTask;
import gov.geoplatform.uasdm.odm.ODMStatus;
import net.geoprism.GeoprismUser;
import net.lingala.zip4j.core.ZipFile;

public class CollectionUploadEvent extends CollectionUploadEventBase
{
  private static final Logger logger           = LoggerFactory.getLogger(CollectionUploadEvent.class);

  private static final long   serialVersionUID = -285847093;

  public CollectionUploadEvent()
  {
    super();
  }

  public void handleUploadFinish(WorkflowTask task, String uploadTarget, ApplicationResource infile, String outFileNamePrefix)
  {
    task.lock();
    task.setStatus(WorkflowTaskStatus.PROCESSING.toString());
    task.setMessage("Processing archived files");
    task.apply();

    UasComponentIF component = task.getComponentInstance();

    if (DevProperties.uploadRaw())
    {
      component.uploadArchive(task, infile, uploadTarget);
    }

    task.lock();
    task.setStatus(WorkflowTaskStatus.COMPLETE.toString());
    task.setMessage("The upload successfully completed.  All files except those mentioned were archived.");
    task.apply();

    startODMProcessing(infile, task, outFileNamePrefix, isMultispectral(component));

    if (component instanceof Collection)
    {
      calculateImageSize(infile, (Collection) component);
    }

//    handleMetadataWorkflow(task);
  }
  
  public boolean isMultispectral(UasComponentIF uasc)
  {
    if (uasc instanceof CollectionIF)
    {
      return ((CollectionIF) uasc).getSensor().isMultiSpectral();
    }
    
    return false;
  }

  private void startODMProcessing(ApplicationResource infile, WorkflowTask uploadTask, String outFileNamePrefix, boolean isMultispectral)
  {
    UasComponentIF component = uploadTask.getComponentInstance();

    ODMProcessingTask task = new ODMProcessingTask();
    task.setUploadId(uploadTask.getUploadId());
    task.setComponent(component.getOid());
    task.setGeoprismUser((GeoprismUser) GeoprismUser.getCurrentUser());
    task.setStatus(ODMStatus.RUNNING.getLabel());
    task.setTaskLabel("UAV data orthorectification for collection [" + component.getName() + "]");
    task.setMessage("The images uploaded to ['" + component.getName() + "'] are submitted for orthorectification processing. Check back later for updates.");
    task.setFilePrefix(outFileNamePrefix);
    task.apply();

    task.initiate(infile, isMultispectral);
  }

  private void calculateImageSize(ApplicationResource zip, Collection collection)
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

            collection.appLock();
            collection.setImageHeight(height);
            collection.setImageWidth(width);
            collection.apply();

            return;
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

//  private void handleMetadataWorkflow(WorkflowTask uploadTask)
//  {
//    if (this.getCollection().getMetadataUploaded())
//    {
//      WorkflowTask task = new WorkflowTask();
//      task.setUploadId(uploadTask.getUploadId());
//      task.setCollectionId(uploadTask.getCollectionOid());
//      task.setGeoprismUser(uploadTask.getGeoprismUser());
//      task.setWorkflowType(WorkflowTask.NEEDS_METADATA);
//      task.setStatus("Message");
//      task.setTaskLabel("Missing Metadata");
//      task.setMessage("Metadata is missing for Collection [" + uploadTask.getCollection().getName() + "].");
//      task.apply();
//    }
//  }

}
