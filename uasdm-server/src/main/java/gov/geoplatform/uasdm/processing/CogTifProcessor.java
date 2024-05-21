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
package gov.geoplatform.uasdm.processing;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.resource.ApplicationFileResource;
import com.runwaysdk.resource.FileResource;

import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.CollectionIF;

public class CogTifProcessor extends ManagedDocument
{
  public static final String COG_EXTENSION = ".cog.tif";
  
  private Logger logger = LoggerFactory.getLogger(CogTifProcessor.class);
  
  private Processor downstream = null;
  
  public CogTifProcessor(String s3path, Product product, CollectionIF collection, StatusMonitorIF monitor)
  {
    super(s3path, product, collection, monitor, false);
  }
  
  @Override
  protected ManagedDocumentTool getTool()
  {
    return ManagedDocumentTool.GDAL;
  }
  
  public CogTifProcessor addDownstream(Processor downstreamProcessor)
  {
    this.downstream = downstreamProcessor;
    return this;
  }

  @Override
  public boolean process(ApplicationFileResource res)
  {
    File file = res.getUnderlyingFile();
    
    final String basename = FilenameUtils.getBaseName(file.getName());

    File overview = new File(file.getParent(), basename + "-overview.tif");
    try
    {
      FileUtils.copyFile(file, overview);
    }
    catch (IOException e)
    {
      String msg = "Error copying file. Cog generation failed for [" + this.getS3Path() + "].";
      logger.error(msg, e);
      monitor.addError(msg);
      return false;
    }

    try
    {
      if (!new SystemProcessExecutor(this.monitor).execute(new String[] {
          "gdaladdo", "-r", "average", overview.getAbsolutePath(), "2", "4", "8", "16"
        }))
      {
        String msg = "Problem occurred generating overview file. Cog generation failed for [" + this.getS3Path() + "].";
        logger.error(msg);
        monitor.addError(msg);
        return false;
      }
      
      File cog = new File(file.getParent(), basename + COG_EXTENSION);
      
      try
      {
        // https://www.cogeo.org/developers-guide.html
        
        // for GDAL versions below 3.1
        //"gdal_translate", overview.getAbsolutePath(), cog.getAbsolutePath(), "-co", "COMPRESS=LZW", "-co", "TILED=YES", "-co", "COPY_SRC_OVERVIEWS=YES"
        List<String> args = new LinkedList<>(Arrays.asList("gdal_translate", overview.getAbsolutePath(), cog.getAbsolutePath(), "-of", "COG", "-co", "COMPRESS=LZW"));
        
        // If the tif is bigger than 4GB we must include the BIGTIFF flag
        if(overview.length() > (1024 * 1024 * 1024 * 4) ) {
          args.add("-co");
          args.add("BIGTIFF=YES");
        }        
        
        if (!new SystemProcessExecutor(this.monitor).execute(args.toArray(new String[args.size()])))
        {
          String msg = "Problem occurred generating cog file. Cog generation failed for [" + this.getS3Path() + "].";
          logger.error(msg);
          monitor.addError(msg);
          return false;
        }
        
        if (cog.exists())
        {
          FileResource cogRes = new FileResource(cog);
          
          if (new CogTifValidator(this.monitor).isValidCog(cogRes))
          {
            if (this.downstream == null)
            {
              return super.process(cogRes);
            }
            else
            {
              if (super.process(cogRes))
              {
                return this.downstream.process(cogRes);
              }
            }
          }
          else
          {
            logger.warn("Problem occurred validating cog file for [" + this.getS3Path() + "].");
          }
        }
        else
        {
          logger.error("Problem occurred generating cog file for [" + this.getS3Path() + "]. Overview file did not exist at [" + overview.getAbsolutePath() + "].");
          monitor.addError("Problem occurred generating cog file for [" + this.getS3Path() + "]. Overview file did not exist.");
        }
      }
      finally
      {
        FileUtils.deleteQuietly(cog);
      }
    }
    finally
    {
      FileUtils.deleteQuietly(overview);
    }
    
    return false;
  }
}
