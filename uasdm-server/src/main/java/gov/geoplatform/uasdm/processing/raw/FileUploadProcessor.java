package gov.geoplatform.uasdm.processing.raw;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.runwaysdk.RunwayException;
import com.runwaysdk.resource.ApplicationFileResource;
import com.runwaysdk.session.Session;

import gov.geoplatform.uasdm.InvalidZipException;
import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask.TaskActionType;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.UasComponentIF;

public class FileUploadProcessor
{
  
  /**
   * 
   * @param task
   * @param fileIn Can be a file, an archive, a directory, whatever. We'll upload everything inside it if it's a collection.
   * @param imageryComponent
   * @param uploadTarget
   * @param product
   * @return
   */
  @SuppressWarnings("resource")
  public List<String> process(AbstractWorkflowTask task, ApplicationFileResource fileIn, ImageryComponent imageryComponent, String uploadTarget, ProductIF product)
  {
    if (!ImageryComponent.isValidTarget(uploadTarget))
    {
      throw new UnsupportedOperationException("Unknown upload target [" + uploadTarget + "]");
    }
    
    try
    {
      List<UasComponentIF> ancestors = imageryComponent.getAncestors();
      List<String> filenames = new LinkedList<String>();
      Queue<ApplicationFileResource> queue = new LinkedList<>();
      
      queue.add(fileIn);
      
      while(!queue.isEmpty())
      {
        var res = queue.poll();
        
        if (res.hasChildren())
        {
          for (var child : res.getChildrenFiles())
            queue.add(child);
          
          continue;
        }
        
        String folder = uploadTarget;

        if (uploadTarget.equals(ImageryComponent.RAW) && Util.isVideoFile(res.getName()))
        {
          folder = ImageryComponent.VIDEO;
        }

        boolean success = Util.uploadFile(task, ancestors, imageryComponent.getS3location(product, folder), res.getName(), res.getUnderlyingFile(), imageryComponent);

        if (success)
        {
          filenames.add(res.getName());
        }
      }
      
      return filenames;
    }
    catch (Throwable t)
    {
      task.createAction(RunwayException.localizeThrowable(t, Session.getCurrentLocale()), TaskActionType.ERROR);

      throw new InvalidZipException(t);
    }
  }
  
}
