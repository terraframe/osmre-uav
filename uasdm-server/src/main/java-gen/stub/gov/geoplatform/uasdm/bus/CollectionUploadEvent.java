package gov.geoplatform.uasdm.bus;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.geoplatform.uasdm.odm.ODMProcessingTask;
import gov.geoplatform.uasdm.odm.ODMStatus;
import gov.geoplatform.uasdm.view.RequestParser;
import net.geoprism.GeoprismUser;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class CollectionUploadEvent extends CollectionUploadEventBase
{
  private static final Logger logger = LoggerFactory.getLogger(CollectionUploadEvent.class);
  
  private static final long serialVersionUID = -285847093;
  
  public CollectionUploadEvent()
  {
    super();
  }

  public void handleUploadFinish(RequestParser parser, File infile)
  {
    WorkflowTask task = WorkflowTask.getTaskByUploadId(parser.getUuid());
    
    task.lock();
    task.setStatus("Processing");
    task.setMessage("Processing archived files");
    task.apply();

    Collection collection = task.getCollection();
    collection.uploadArchive(task, infile);

    task.lock();
    task.setStatus("Complete");
    task.setMessage("The upload successfully completed.  All files except those mentioned were archived.");
    task.apply();
    
    startODMProcessing(infile, task);
    
    calculateImageSize(infile, collection);
    
//    handleMetadataWorkflow(task);
  }
  
  private void calculateImageSize(File zip, Collection collection)
  {
    try
    {
      final String[] formats = new String[] {"jpeg", "jpg", "png", "gif", "bmp", "fits", "gray", "graya", "jng", "mono", "ico", "jbig", "tga", "tiff", "tif"};
      File parentFolder = new File(FileUtils.getTempDirectory(), zip.getName());
      
      try
      {
        new ZipFile(zip).extractAll(parentFolder.getAbsolutePath());
        
        File[] files = parentFolder.listFiles();
        
        for (File file : files)
        {
          String ext = FilenameUtils.getExtension(file.getName()).toLowerCase();
          if (ArrayUtils.contains(formats, ext))
          {
            BufferedImage bimg = ImageIO.read(file);
            int width          = bimg.getWidth();
            int height         = bimg.getHeight();
            
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
  
  private void startODMProcessing(File infile, WorkflowTask uploadTask)
  {
    ODMProcessingTask task = new ODMProcessingTask();
    task.setUpLoadId(uploadTask.getUpLoadId());
    task.setCollectionId(uploadTask.getCollectionOid());
    task.setGeoprismUser((GeoprismUser) GeoprismUser.getCurrentUser());
    task.setStatus(ODMStatus.RUNNING.getLabel());
    task.setTaskLabel("Orthorectification Processing (ODM) [" + task.getCollection().getName() + "]");
    task.setMessage("Your images are submitted for processing. Check back later for updates.");
    task.apply();
    
    task.initiate(infile);
  }
  
//  private void handleMetadataWorkflow(WorkflowTask uploadTask)
//  {
//    if (this.getCollection().getMetadataUploaded())
//    {
//      WorkflowTask task = new WorkflowTask();
//      task.setUpLoadId(uploadTask.getUpLoadId());
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
