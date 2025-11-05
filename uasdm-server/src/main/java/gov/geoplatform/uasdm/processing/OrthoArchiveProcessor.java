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
import java.util.Arrays;
import java.util.List;

import org.locationtech.jts.geom.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.resource.ApplicationFileResource;
import com.runwaysdk.resource.FileResource;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.UasComponentIF;

public class OrthoArchiveProcessor extends ManagedDocument
{
  public static final String COG_EXTENSION = ".cog.tif";

  private Logger             logger        = LoggerFactory.getLogger(OrthoArchiveProcessor.class);

  public OrthoArchiveProcessor(String s3path, Product product, UasComponentIF component, StatusMonitorIF monitor)
  {
    super(s3path, product, component, monitor, false);
  }

  @Override
  protected ManagedDocumentTool getTool()
  {
    return ManagedDocumentTool.GDAL;
  }

  @Override
  public ProcessResult process(ApplicationFileResource archive)
  {
    ApplicationFileResource tif = null;
    ApplicationFileResource georeference = null;

    for (ApplicationFileResource child : archive.getChildrenFiles())
    {
      if (isTifFile(child))
      {
        tif = child;
      }

      if (isGeoreferenceFile(child))
      {
        georeference = child;
      }
    }

    if (tif != null && georeference != null)
    {
      File geotif = new File(tif.getUnderlyingFile().getParent(), tif + "_converted.tif");
      
      Envelope bounds = new Envelope();

      List<String> cmd = AppProperties.getCondaTool("gdal_translate");
      cmd.addAll(Arrays.asList(new String[] { //
          "-of", "GTiff", //
          "-a_srs", "EPSG:4326", //
          "-a_ullr", Double.toString(bounds.getMaxX()), Double.toString(bounds.getMaxY()), Double.toString(bounds.getMinX()), Double.toString(bounds.getMinY()), //
          tif.getAbsolutePath(), geotif.getAbsolutePath() //
      }));

      boolean success = new SystemProcessExecutor(this.monitor) //
          .setEnvironment("PROJ_DATA", AppProperties.getSilvimetricProjDataPath()) //
          .setCommandName("gdal_translate") //
          .suppressError("Warning 6.*Defaulting to Byte") //
          .execute(cmd.toArray(new String[0]));

      if (success && geotif.exists())
      {
        return super.process(new FileResource(geotif));
      }
      else
      {
        logger.info("Problem occurred generating gdal transform. PNG file did not exist at [" + geotif.getAbsolutePath() + "].");
        monitor.addError("Problem occurred generating gdal transform. PNG file did not exist.");
      }
    }
    else
    {
      monitor.addError("Unable to determine the tif file and the georeference file in the archive.");
    }

    return ProcessResult.fail();
  }

  private boolean isGeoreferenceFile(ApplicationFileResource child)
  {
    return child.exists() && child.getNameExtension().toLowerCase().endsWith(".xml");
  }

  private boolean isTifFile(ApplicationFileResource child)
  {
    return child.exists() && child.getNameExtension().toLowerCase().endsWith(".tiff");
  }
}
