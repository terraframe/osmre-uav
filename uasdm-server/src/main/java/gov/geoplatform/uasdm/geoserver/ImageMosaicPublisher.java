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
package gov.geoplatform.uasdm.geoserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.ProgrammingErrorException;

import gov.geoplatform.uasdm.AppProperties;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.encoder.GSLayerEncoder;
import it.geosolutions.geoserver.rest.encoder.GSResourceEncoder.ProjectionPolicy;
import it.geosolutions.geoserver.rest.encoder.coverage.GSImageMosaicEncoder;
import net.geoprism.gis.geoserver.GeoserverFacade;
import net.geoprism.gis.geoserver.GeoserverProperties;

public class ImageMosaicPublisher
{
  private static final Logger        logger     = LoggerFactory.getLogger(ImageMosaicPublisher.class);

  protected static final String PUBLIC_WORKSPACE = AppProperties.getPublicWorkspace();
  
  protected static final String HILLSHADE_WORKSPACE = AppProperties.getPublicHillshadeWorkspace();
  
  protected File workspaceDir;
  
  protected String workspaceName;
  
  public ImageMosaicPublisher(String workspaceName) // AppProperties.getPublicWorkspace()
  {
    this.init(workspaceName);
  }
  
  public static void initializeAll()
  {
    new ImageMosaicPublisher(PUBLIC_WORKSPACE).create();
    new ImageMosaicPublisher(HILLSHADE_WORKSPACE).create();
  }
  
  public static void refreshAll()
  {
    new ImageMosaicPublisher(PUBLIC_WORKSPACE).refresh();
    new ImageMosaicPublisher(HILLSHADE_WORKSPACE).refresh();
  }
  
  protected void init(String workspaceName)
  {
    this.workspaceName = workspaceName;
    
    String geoserverData = System.getProperty("GEOSERVER_DATA_DIR");

    if (geoserverData == null)
    {
      throw new ProgrammingErrorException("Unable to find geoserver data directory: Please set the JVM arg GEOSERVER_DATA_DIR");
    }

    this.workspaceDir = new File(geoserverData + "/data/" + this.workspaceName);
    
    try
    {
      FileUtils.forceMkdir(this.workspaceDir);
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  protected void create()
  {
    try
    {
      final File indexer = new File(this.workspaceDir, "indexer.properties");

      if (!indexer.exists())
      {
        deleteMosaic(this.workspaceName);

        try (InputStream istream = this.getClass().getResourceAsStream("/indexer.properties"))
        {
          try (FileOutputStream ostream = new FileOutputStream(indexer))
          {
            IOUtils.copy(istream, ostream);
          }
        }
      }

      if (this.hasPublishedLayers())
      {
        publishMosaic(this.workspaceName);
      }
    }
    catch (IOException e)
    {
      logger.error("Error creating the new store: " + this.workspaceName, e);
    }
  }

  protected void refresh()
  {
    try
    {
      this.deleteMosaic(this.workspaceName);

      if (this.hasPublishedLayers())
      {
        this.publishMosaic(this.workspaceName);
      }
      else
      {
        logger.info("Not creating image mosaic [" + this.workspaceName + "] because no published layers exist");
      }
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }
  
  protected void deleteMosaic(String name)
  {
    if (GeoserverFacade.layerExists(this.workspaceName, name))
    {
      this.geoserverRemove(this.workspaceName, this.workspaceName);

      // Delete the existing store files
      final File[] files = this.workspaceDir.listFiles();

      if (files != null)
      {
        for (File file : files)
        {
          if (!file.isDirectory() && ( file.getName().startsWith(this.workspaceName) || file.getName().equals("sample_image.dat") ))
          {
            FileUtils.deleteQuietly(file);
          }
        }
      }
    }
  }

  protected void publishMosaic(final String mosaicName) throws FileNotFoundException
  {
    if (!GeoserverFacade.layerExists(this.workspaceName, mosaicName))
    {
      final GeoServerRESTPublisher publisher = GeoserverProperties.getPublisher();
      
      final GSLayerEncoder layerEnc = new GSLayerEncoder();
      layerEnc.setDefaultStyle("raster");

      // coverage encoder
      final GSImageMosaicEncoder coverageEnc = new GSImageMosaicEncoder();
      coverageEnc.setName(mosaicName);
      coverageEnc.setTitle(mosaicName);
      coverageEnc.setProjectionPolicy(ProjectionPolicy.REPROJECT_TO_DECLARED);
      // coverageEnc.setMaxAllowedTiles(Integer.MAX_VALUE);

      // ... many other options are supported

      // create a new ImageMosaic layer...
      final boolean published = publisher.publishExternalMosaic(this.workspaceName, this.workspaceName, this.workspaceDir, coverageEnc, layerEnc);

      // check the results
      if (!published)
      {
        logger.error("Error creating the new store: " + this.workspaceName);
      }
    }
    else
    {
      logger.info("Not creating image mosaic [" + this.workspaceName + "] because the layer already exists.");
    }
  }

  protected void geoserverRemove(final String workspace, final String storeName)
  {
    if (GeoserverProperties.getPublisher().removeCoverageStore(workspace, storeName, true, GeoServerRESTPublisher.Purge.NONE))
    {
      logger.info("Removed the coverage store [" + GeoserverProperties.getStore() + "].");
    }
    else
    {
      logger.warn("Failed to remove the coverage store [" + storeName + "].");
    }
  }

  protected boolean hasPublishedLayers()
  {
    final File[] files = this.workspaceDir.listFiles();

    if (files != null)
    {
      for (File file : files)
      {
        if (file.isDirectory())
        {
          return true;
        }
      }
    }

    return false;
  }
}
