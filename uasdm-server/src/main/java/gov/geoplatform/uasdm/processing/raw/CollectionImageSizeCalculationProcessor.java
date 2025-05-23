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

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.Queue;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.resource.ApplicationFileResource;
import com.runwaysdk.resource.ArchiveFileResource;

import gov.geoplatform.uasdm.MetadataXMLGenerator;
import gov.geoplatform.uasdm.bus.Collection;
import gov.geoplatform.uasdm.bus.ImageryUploadEvent;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.view.FlightMetadata;

public class CollectionImageSizeCalculationProcessor
{
  private static final Logger logger = LoggerFactory.getLogger(CollectionImageSizeCalculationProcessor.class);
  
  @SuppressWarnings("resource")
  public boolean process(ArchiveFileResource archive, ImageryComponent component)
  {
    try
    {
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
        
        if (ArrayUtils.contains(ImageryUploadEvent.formats, res.getNameExtension().toLowerCase()))
        {
          BufferedImage bimg = ImageIO.read(res.getUnderlyingFile());

          int width = bimg.getWidth();
          int height = bimg.getHeight();

          if (component instanceof CollectionIF)
          {
            var collection = (CollectionIF) component;
            
            collection.appLock();
            collection.setImageHeight(height);
            collection.setImageWidth(width);
            collection.apply();
  
            FlightMetadata metadata = FlightMetadata.get(collection, Collection.RAW, collection.getFolderName() + MetadataXMLGenerator.FILENAME);
  
            if (metadata != null)
            {
              metadata.getSensor().setImageWidth(Integer.toString(collection.getImageWidth()));
              metadata.getSensor().setImageHeight(Integer.toString(collection.getImageHeight()));
  
              MetadataXMLGenerator generator = new MetadataXMLGenerator();
              generator.generateAndUpload(collection, null, metadata, collection.getMetadata().orElseThrow());
            }
          }
          
          return true;
        }
      }
    }
    catch (Throwable t)
    {
      logger.error("Error occurred while calculating the image size.", t);
    }
    
    return false;
  }
  
}
