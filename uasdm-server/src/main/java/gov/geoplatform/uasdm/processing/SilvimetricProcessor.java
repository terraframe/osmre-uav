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
import gov.geoplatform.uasdm.model.UasComponentIF;

/**
 * Generates metrics for an input copc pointcloud. For each metric, a new product will be created and the output raster
 * will be uploaded to the "odm" folder of the new product.
 */
public class SilvimetricProcessor extends ManagedDocument
{
  private Logger            logger = LoggerFactory.getLogger(SilvimetricProcessor.class);
  
  private LidarProcessConfiguration config;
  
  public static enum Metric {
    TREE_CANOPY_COVER("tree_canopy_cover"),
    GROUND_SURFACE_MODEL("ground_surface_model"),
    TREE_STRUCTURE("tree_structure"),
    TERRAIN_MODEL("terrain_model");
    
    private String name;
    
    private Metric(String name) {
      this.name = name;
    }
    
    public String getName() {
      return this.name;
    }
    
    public static Metric fromSilvimetric(String silvimetric) {
      for (Metric m : Metric.values()) {
        if (m.matches(silvimetric)) {
          return m;
        }
      }
      return null;
    }
    
    public boolean matches(String metricName) {
      if (this.equals(GROUND_SURFACE_MODEL) && metricName.toLowerCase().equals("m_z_max")) {
        return true;
      } else if (this.equals(TERRAIN_MODEL) && metricName.toLowerCase().equals("m_z_min")) {
        return true;
      } else if (this.equals(TREE_STRUCTURE) && metricName.toLowerCase().equals("m_z_diff")) {
        return true;
      } else if (this.equals(TREE_CANOPY_COVER) && metricName.toLowerCase().equals("m_classification_veg_density")) {
        return true;
      }
      
      return false;
    }
    
    public boolean shouldGenerate(LidarProcessConfiguration config) {
      if (this.equals(GROUND_SURFACE_MODEL) && config.isGenerateGSM()) {
        return true;
      } else if (this.equals(TERRAIN_MODEL) && config.isGenerateTerrainModel()) {
        return true;
      } else if (this.equals(TREE_STRUCTURE) && config.isGenerateTreeStructure()) {
        return true;
      } else if (this.equals(TREE_CANOPY_COVER) && config.isGenerateTreeCanopyCover()) {
        return true;
      }
      
      return false;
    }
  }

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
  public ProcessResult process(ApplicationFileResource res)
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
          .setEnvironment("PROJ_DATA", AppProperties.getSilvimetricProjDataPath())
          .setCommandName("silvimetric")
          .suppressError("2\\d\\d\\d\\-\\d\\d-\\d\\d.*?distributed.worker.memory.*?Unmanaged memory use is high.*?Worker memory limit: [\\d|\\.]* (GiB|MiB|TiB)?")
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
            final Metric metric = Metric.fromSilvimetric(metricName);
            
            if (metric != null && metric.shouldGenerate(config)) {
              this.product = (Product) component.createProductIfNotExist(this.config.getProductName() + "_" + metric.getName());
              
              this.s3Path = ImageryComponent.ORTHO + "/" + metric.getName() + ".tif";
              
              if (this.downstream != null && this.downstream instanceof S3FileUpload) {
                ((S3FileUpload)this.downstream).setProduct(product);
                ((S3FileUpload)this.downstream).setS3Path(ImageryComponent.ORTHO + "/" + metric.getName() + ".cog.tif");
              }
              
              super.process(new FileResource(outfile));
            }
          }
        }
      }
    }
    finally
    {
      FileUtils.deleteQuietly(input);
    }

    return ProcessResult.fail();
  }
}