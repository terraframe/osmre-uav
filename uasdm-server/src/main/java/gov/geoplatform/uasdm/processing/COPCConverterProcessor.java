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
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.resource.FileResource;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.UasComponentIF;

public class COPCConverterProcessor extends ManagedDocument implements Processor
{
  private Logger            logger = LoggerFactory.getLogger(COPCConverterProcessor.class);

  protected String          s3Path;

  protected StatusMonitorIF monitor;

  protected UasComponentIF component;

  protected Product         product;
  
  private Processor          downstream = null;

  public COPCConverterProcessor(String s3Path, Product product, UasComponentIF component, StatusMonitorIF monitor)
  {
    super(s3Path, product, component, monitor, false);
  }
  
  @Override
  protected ManagedDocumentTool getTool()
  {
    return ManagedDocumentTool.PDAL;
  }
  
  public COPCConverterProcessor addDownstream(Processor downstreamProcessor)
  {
    this.downstream = downstreamProcessor;
    return this;
  }

  @Override
  public boolean process(ApplicationFileResource res)
  {
    File out = new File(res.getUnderlyingFile().getParent(), res.getBaseName() + ".copc.laz");

    try
    {
      boolean success = new SystemProcessExecutor(this.monitor).execute(new String[] { AppProperties.getPdalPath(), "translate", " -i", res.getAbsolutePath(), "-o", out.getAbsolutePath(), "-r", "readers.las", "-w", "writers.copc", "--overwrite" });
  
      if (success && out.exists())
      {
        FileResource frout = new FileResource(out);
        
        if (this.downstream == null)
        {
          return super.process(frout);
        }
        else
        {
          if (super.process(frout))
          {
            return this.downstream.process(frout);
          }
        }
      }
      else
      {
        logger.info("Problem occurred generating COPC. PDAL translate ran, did not generate an output file at [" + out.getAbsolutePath() + "].");
        monitor.addError("Problem occurred running pdal translate. Output copc file did not exist.");
      }
    } finally {
      FileUtils.deleteQuietly(out);
    }

    return false;
  }
}