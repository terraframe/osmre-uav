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
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.resource.ApplicationFileResource;
import com.runwaysdk.resource.FileResource;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.UasComponentIF;

public class HillshadeProcessor extends ManagedDocument
{
  private Logger logger = LoggerFactory.getLogger(HillshadeProcessor.class);

  public HillshadeProcessor(String s3Path, Product product, UasComponentIF component, StatusMonitorIF monitor)
  {
    super(s3Path, product, component, monitor, false);
  }

  @Override
  protected ManagedDocumentTool getTool()
  {
    return ManagedDocumentTool.GDAL;
  }

  @Override
  public boolean process(ApplicationFileResource res)
  {
    File file = res.getUnderlyingFile();

    final String basename = FilenameUtils.getBaseName(file.getName());

    File hillshade = new File(file.getParent(), basename + "-gdal" + CogTifProcessor.COG_EXTENSION);

    var cmd = AppProperties.getCondaTool("gdaldem");
    cmd.addAll(Arrays.asList(new String[] { "hillshade", file.getAbsolutePath(), hillshade.getAbsolutePath(), "-co", "BIGTIFF=YES" }));

    boolean success = new SystemProcessExecutor(this.monitor)
        .setEnvironment("PROJ_DATA", AppProperties.getSilvimetricProjDataPath())
        .setCommandName("gdaldem")
        .execute(cmd.toArray(new String[0]));

    if (success && hillshade.exists())
    {
      super.process(new FileResource(hillshade));
    }
    else
    {
      logger.info("Problem occurred generating gdal transform. Hillshade file did not exist at [" + hillshade.getAbsolutePath() + "].");
      monitor.addError("Problem occurred generating gdal transform. Hillshade file did not exist.");
    }

    return false;
  }
}