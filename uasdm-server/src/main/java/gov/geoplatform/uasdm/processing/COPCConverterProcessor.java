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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.DuplicateDataException;
import com.runwaysdk.resource.ApplicationFileResource;
import com.runwaysdk.resource.FileResource;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.lidar.LidarProcessConfiguration;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.UasComponentIF;

/**
 * Responsible for converting a laz/las pointcloud to copc format
 */
public class COPCConverterProcessor extends ManagedDocument
{
  private Logger            logger = LoggerFactory.getLogger(COPCConverterProcessor.class);

  protected StatusMonitorIF monitor;

  private LidarProcessConfiguration config;

  public COPCConverterProcessor(LidarProcessConfiguration config, UasComponentIF component, StatusMonitorIF monitor)
  {
    super(null, null, component, monitor, false);
    this.config = config;
  }
  
  @Override
  protected ManagedDocumentTool getTool()
  {
    return ManagedDocumentTool.PDAL;
  }
  
  @Override
  public boolean process(ApplicationFileResource res)
  {
    this.product = Product.createIfNotExist(component, this.config.getProductName());
    this.s3Path = ImageryComponent.PTCLOUD + "/pointcloud.copc.laz";
    
    File out = new File(res.getUnderlyingFile().getParent(), res.getBaseName() + ".copc.laz");

    try
    {
      boolean success = new SystemProcessExecutor(this.monitor)
          .setEnvironment("PROJ_DATA", AppProperties.getSilvimetricProjDataPath())
          .execute(AppProperties.getPythonToolPath("pdal"), "translate", "-i", res.getAbsolutePath(), "-o", out.getAbsolutePath(), "-r", "readers.las", "-w", "writers.copc", "--overwrite");
  
      if (success && out.exists())
      {
        FileResource frout = new FileResource(out);
        
        return super.process(frout);
      }
    } finally {
      FileUtils.deleteQuietly(out);
    }

    return false;
  }
}