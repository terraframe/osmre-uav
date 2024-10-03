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
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.resource.ApplicationFileResource;
import com.runwaysdk.resource.FileResource;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.UasComponentIF;

public class PotreeConverterProcessor implements Processor
{
  private Logger            logger = LoggerFactory.getLogger(PotreeConverterProcessor.class);

  protected String          s3Path;

  protected StatusMonitorIF monitor;

  protected UasComponentIF component;

  protected Product         product;

  public PotreeConverterProcessor(String s3Path, Product product, UasComponentIF component, StatusMonitorIF monitor)
  {
    this.s3Path = s3Path;
    this.monitor = monitor;
    this.product = product;
    this.component = component;
  }

  @Override
  public boolean process(ApplicationFileResource res)
  {
    File file = res.getUnderlyingFile();

    final String basename = FilenameUtils.getBaseName(file.getName());

    File outputDirectory = new File(file.getParent(), basename + "-potree");

    try
    {
      // Copy the underlying file to a filename with the proper
      File input = File.createTempFile("laz", "." + res.getNameExtension());

      try
      {
        FileUtils.copyFile(file, input);

        boolean success = new SystemProcessExecutor(this.monitor).execute(new String[] { AppProperties.getPotreeConverterPath(), input.getAbsolutePath(), "-o", outputDirectory.getAbsolutePath() });

        if (success && outputDirectory.exists())
        {
          File[] files = outputDirectory.listFiles();

          if (files != null)
          {
            // Upload all of the generated files
            for (File outfile : files)
            {
              new ManagedDocument(this.s3Path + "/" + outfile.getName(), product, component, this.monitor).process(new FileResource(outfile));
            }
          }
        }
        else
        {
          logger.info("Problem occurred generating Potree Converter Transform. Potree data directory did not exist at [" + outputDirectory.getAbsolutePath() + "].");
          monitor.addError("Problem occurred running Potree Converter. Output file did not exist.");
        }
      }
      finally
      {
        FileUtils.deleteQuietly(input);
      }

    }
    catch (IOException e)
    {
      logger.error("Problem occurred generating Potree Converter Transform.", e);
      monitor.addError("Problem occurred running Potree Converter.");
    }

    return false;
  }
}