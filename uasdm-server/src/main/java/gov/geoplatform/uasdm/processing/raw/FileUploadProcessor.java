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
