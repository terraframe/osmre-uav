package gov.geoplatform.uasdm.bus;

import gov.geoplatform.uasdm.odm.ImageryODMProcessingTask;
import gov.geoplatform.uasdm.odm.ODMStatus;
import gov.geoplatform.uasdm.view.RequestParser;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.geoprism.GeoprismUser;
import net.lingala.zip4j.core.ZipFile;

public class ImageryUploadEvent extends ImageryUploadEventBase
{
  public static final String[] formats = new String[] {"jpeg", "jpg", "png", "gif", "bmp", "fits", "gray", "graya", "jng", "mono", "ico", "jbig", "tga", "tiff", "tif"};
  
  private static final Logger logger = LoggerFactory.getLogger(ImageryUploadEvent.class);
  
  private static final long serialVersionUID = 510137801;
  
  public ImageryUploadEvent()
  {
    super();
  }
  
  public void handleUploadFinish(RequestParser parser, File infile)
  {
    ImageryWorkflowTask task = ImageryWorkflowTask.getTaskByUploadId(parser.getUuid());
    
    task.lock();
    task.setStatus("Processing");
    task.setMessage("Processing archived files");
    task.apply();

    Imagery imagery = task.getImagery();
    imagery.uploadArchive(task, infile);

    task.lock();
    task.setStatus("Complete");
    task.setMessage("The upload successfully completed.  All files except those mentioned were archived.");
    task.apply();
    
    startODMProcessing(infile, task, parser);
    
    calculateImageSize(infile, imagery);
  }
  
  private void startODMProcessing(File infile, ImageryWorkflowTask uploadTask, RequestParser parser)
  {
    ImageryODMProcessingTask task = new ImageryODMProcessingTask();
    task.setUpLoadId(uploadTask.getUpLoadId());
    task.setImageryId(uploadTask.getImageryOid());
    task.setGeoprismUser((GeoprismUser) GeoprismUser.getCurrentUser());
    task.setStatus(ODMStatus.RUNNING.getLabel());
//    task.setTaskLabel("Orthorectification Processing (ODM) [" + task.getCollection().getName() + "]");
    task.setTaskLabel("UAV data orthorectification for imagery [" + task.getImagery().getName() + "]");
    task.setMessage("The images uploaded to ['" + task.getImagery().getName() + "'] are submitted for orthorectification processing. Check back later for updates.");
    task.setFilePrefix(parser.getCustomParams().get("outFileName"));
    task.apply();
    
    task.initiate(infile);
  }
  
  private void calculateImageSize(File zip, Imagery imagery)
  {
    try
    {
      File parentFolder = new File(FileUtils.getTempDirectory(), zip.getName());
      
      try
      {
        new ZipFile(zip).extractAll(parentFolder.getAbsolutePath());
        
        File[] files = parentFolder.listFiles();
        
        for (File file : files)
        {
          String ext = FilenameUtils.getExtension(file.getName()).toLowerCase();
          if (ArrayUtils.contains(ImageryUploadEvent.formats, ext))
          {
            BufferedImage bimg = ImageIO.read(file);
            int width          = bimg.getWidth();
            int height         = bimg.getHeight();
            
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
      }
    }
    catch (Throwable e)
    {
      logger.error("Error occurred while calculating the image size.", e);
    }
  }
  
}
