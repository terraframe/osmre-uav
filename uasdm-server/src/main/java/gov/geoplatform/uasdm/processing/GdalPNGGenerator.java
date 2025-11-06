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
import java.util.Arrays;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.resource.ApplicationFileResource;
import com.runwaysdk.resource.FileResource;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.UasComponentIF;

public class GdalPNGGenerator extends ManagedDocument
{
  private Logger logger = LoggerFactory.getLogger(GdalPNGGenerator.class);
  
  public GdalPNGGenerator(String s3Path, Product product, UasComponentIF component, StatusMonitorIF monitor)
  {
    super(s3Path, product, component, monitor, false);
  }
  
  @Override
  protected ManagedDocumentTool getTool()
  {
    return ManagedDocumentTool.GDAL;
  }
  
  @SuppressWarnings("resource")
  @Override
  public ProcessResult process(ApplicationFileResource res)
  {
    File file = res.getUnderlyingFile();
    
    final String basename = FilenameUtils.getBaseName(file.getName());

    File png = new File(file.getParent(), basename + ".png");
    
    var cmd = AppProperties.getCondaTool("gdal_translate");
    cmd.addAll(Arrays.asList(new String[] { "-of", "PNG", file.getAbsolutePath(), png.getAbsolutePath() }));
    
    boolean success = new SystemProcessExecutor(this.monitor)
        .setEnvironment("PROJ_DATA", AppProperties.getSilvimetricProjDataPath())
        .setCommandName("gdal_translate")
        .suppressError("Warning 6.*Defaulting to Byte")
        .execute(cmd.toArray(new String[0]));

    if (success && png.exists())
    {
      return super.process(new FileResource(png));
    }
    else
    {
      logger.info("Problem occurred generating gdal transform. PNG file did not exist at [" + png.getAbsolutePath() + "].");
      monitor.addError("Problem occurred generating gdal transform. PNG file did not exist.");
    }
    
    return ProcessResult.fail();
  }
}
