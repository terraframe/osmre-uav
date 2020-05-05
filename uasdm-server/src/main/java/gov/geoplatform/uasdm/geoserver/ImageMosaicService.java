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
import it.geosolutions.geoserver.rest.encoder.coverage.GSImageMosaicEncoder;
import net.geoprism.gis.geoserver.GeoserverFacade;
import net.geoprism.gis.geoserver.GeoserverProperties;

public class ImageMosaicService
{
  private final Logger        logger     = LoggerFactory.getLogger(ImageMosaicService.class);

  private static final String STORE_NAME = "image-public";

  public void create()
  {
    try
    {
      final String workspace = AppProperties.getPublicWorkspace();
      final File baseDir = this.getWorkspaceDirectory();
      final File indexer = new File(baseDir, "indexer.properties");

      if (!indexer.exists())
      {
        deleteImageMosaic(workspace, baseDir);

        try (InputStream istream = this.getClass().getResourceAsStream("/indexer.properties"))
        {
          try (FileOutputStream ostream = new FileOutputStream(indexer))
          {
            IOUtils.copy(istream, ostream);
          }
        }
      }

      if (this.hasPublishedLayers(baseDir))
      {
        // Setup the public image mosaic data store if it doesn't exist
        final GeoServerRESTPublisher publisher = GeoserverProperties.getPublisher();

        if (!GeoserverFacade.layerExists(workspace, STORE_NAME))
        {
          publish(workspace, baseDir, publisher);
        }
        else
        {
          logger.info("Layer already exists: " + STORE_NAME);
        }
      }
    }
    catch (IOException e)
    {
      logger.error("Error creating the new store: " + STORE_NAME, e);
    }
  }

  public void deleteImageMosaic(final String workspace, final File baseDir)
  {
    if (GeoserverFacade.layerExists(workspace, STORE_NAME))
    {
      this.removeImageMosaic(workspace, STORE_NAME);

      // Delete the existing store files
      final File[] files = baseDir.listFiles();

      for (File file : files)
      {
        if (!file.isDirectory() && ( file.getName().startsWith(STORE_NAME) || file.getName().equals("sample_image.dat") ))
        {
          FileUtils.deleteQuietly(file);
        }
      }
    }
  }

  public File getWorkspaceDirectory()
  {
    String geoserverData = System.getProperty("GEOSERVER_DATA_DIR");

    if (geoserverData == null)
    {
      throw new ProgrammingErrorException("Unable to find geoserver data directory: Please set the JVM arg GEOSERVER_DATA_DIR");
    }

    return new File(geoserverData + "/data/" + AppProperties.getPublicWorkspace());
  }

  public void refresh()
  {
    try
    {
      final String workspace = AppProperties.getPublicWorkspace();
      final File baseDir = this.getWorkspaceDirectory();

      // Setup the public image mosaic data store if it doesn't exist
      final GeoServerRESTPublisher publisher = GeoserverProperties.getPublisher();

      if (GeoserverFacade.layerExists(workspace, STORE_NAME))
      {
        this.deleteImageMosaic(workspace, baseDir);
      }

      if (this.hasPublishedLayers(baseDir) && !GeoserverFacade.layerExists(workspace, STORE_NAME))
      {
        this.publish(workspace, baseDir, publisher);
      }
      else
      {
        logger.info("Not creating image mosaic [" + STORE_NAME + "] because no published layers exist");
      }
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public void publish(final String workspace, final File baseDir, final GeoServerRESTPublisher publisher) throws FileNotFoundException
  {
    final GSLayerEncoder layerEnc = new GSLayerEncoder();
    layerEnc.setDefaultStyle("raster");

    // coverage encoder
    final GSImageMosaicEncoder coverageEnc = new GSImageMosaicEncoder();
    coverageEnc.setName(STORE_NAME);
    coverageEnc.setTitle(STORE_NAME);
    // coverageEnc.setMaxAllowedTiles(Integer.MAX_VALUE);

    // ... many other options are supported

    // create a new ImageMosaic layer...
    final boolean published = publisher.publishExternalMosaic(workspace, STORE_NAME, baseDir, coverageEnc, layerEnc);

    // check the results
    if (!published)
    {
      logger.error("Error creating the new store: " + STORE_NAME);
    }
  }

  public void removeImageMosaic(final String workspace, final String storeName)
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

  private boolean hasPublishedLayers(File baseDir)
  {
    final File[] files = baseDir.listFiles();

    for (File file : files)
    {
      if (file.isDirectory())
      {
        return true;
      }
    }

    return false;
  }
}
