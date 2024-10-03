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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.resource.ApplicationFileResource;
import com.runwaysdk.resource.FileResource;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.lidar.LidarProcessConfiguration;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.LayerClassification;
import gov.geoplatform.uasdm.model.UasComponentIF;

/**
 * Generates metrics for an input copc pointcloud. For each metric, a new product will be created and the output raster
 * will be uploaded to the "odm" folder of the new product.
 */
public class SilvimetricProcessor extends ManagedDocument
{
  private Logger            logger = LoggerFactory.getLogger(SilvimetricProcessor.class);
  
  private LidarProcessConfiguration config;

  public SilvimetricProcessor(LidarProcessConfiguration config, UasComponentIF component, StatusMonitorIF monitor)
  {
    super(null, null, component, monitor);
    this.config = config;
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

    final String basename = res.getName().substring(0, res.getName().lastIndexOf("."));

    File outputDirectory = new File(input.getParent(), basename + "-silvimetric");
    outputDirectory.mkdirs();

    try
    {
      var cmd = AppProperties.getSilvimetricCommand();
      
      cmd.addAll(Arrays.asList(new String[] { input.getAbsolutePath(), outputDirectory.getAbsolutePath() }));

      boolean success = new SystemProcessExecutor(this.monitor)
          .setEnvironment("PROJ_DATA", AppProperties.getProjDataPath())
          .execute(cmd.toArray(new String[0]));

      if (success && outputDirectory.exists())
      {
        File[] files = outputDirectory.listFiles();

        if (files != null)
        {
          // Upload all of the generated files
          for (File outfile : files)
          {
            final String metricName = FilenameUtils.getBaseName(outfile.getName()).replace("-", "_");
            
            this.product = Product.createIfNotExist(component, this.config.getProductName() + "_" + metricName);
            
            this.s3Path = ImageryComponent.ORTHO + "/" + metricName + ".tif";
            
            if (this.downstream != null && this.downstream instanceof S3FileUpload) {
              ((S3FileUpload)this.downstream).setProduct(product);
              ((S3FileUpload)this.downstream).setS3Path(ImageryComponent.ORTHO + "/" + metricName + ".cog.tif");
            }
            
            super.process(new FileResource(outfile));
          }
        }
      }
    }
    finally
    {
      FileUtils.deleteQuietly(input);
    }

    return false;
  }
}