/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.geoplatform.uasdm.processing;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.resource.ApplicationFileResource;
import com.runwaysdk.resource.FileResource;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.UasComponentIF;

public class SilvimetricProcessor extends ManagedDocument implements Processor
{
  private Logger            logger = LoggerFactory.getLogger(SilvimetricProcessor.class);

  protected String          s3Path;

  protected StatusMonitorIF monitor;

  protected UasComponentIF component;

  protected Product         product;

  public SilvimetricProcessor(UasComponentIF component, StatusMonitorIF monitor)
  {
    super(null, null, component, monitor);
  }
  
  @Override
  protected ManagedDocumentTool getTool()
  {
    return ManagedDocumentTool.SILVIMETRIC;
  }

  @Override
  public boolean process(ApplicationFileResource res)
  {
    File input = res.getUnderlyingFile();

    final String basename = FilenameUtils.getBaseName(res.getName());

    File outputDirectory = new File(input.getParent(), basename + "-silvimetric");

    try
    {
      var cmd = AppProperties.getSilvimetricCommand();
      
      cmd.addAll(Arrays.asList(new String[] { input.getAbsolutePath(), outputDirectory.getAbsolutePath() }));

      boolean success = new SystemProcessExecutor(this.monitor).execute(cmd.toArray(new String[0]));

      if (success && outputDirectory.exists())
      {
        File[] files = outputDirectory.listFiles();

        if (files != null)
        {
          // Upload all of the generated files
          for (File outfile : files)
          {
            this.product = Product.createIfNotExistOrThrow(component, FilenameUtils.getBaseName(outfile.getName()));
            this.s3Path = this.product.getS3location() + "/" + ImageryComponent.ORTHO;
            
            super.process(new FileResource(outfile));
          }
        }
      }
      else
      {
        logger.info("Problem occurred generating Potree Converter Transform. Potree data directory did not exist at [" + outputDirectory.getAbsolutePath() + "].");
        monitor.addError("Problem occurred generating gdal transform. Hillshade file did not exist.");
      }
    }
    finally
    {
      FileUtils.deleteQuietly(input);
    }

    return false;
  }
}