package gov.geoplatform.uasdm.processing;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.CollectionIF;

public class CogTifProcessor extends SystemProcessProcessor
{
  public static final String COG_EXTENSION = ".cog.tif";
  
  private Logger logger = LoggerFactory.getLogger(CogTifProcessor.class);
  
  public CogTifProcessor(String s3path, Product product, CollectionIF collection, StatusMonitorIF monitor)
  {
    super(s3path, product, collection, monitor, false);
  }

  @Override
  public void process(File file)
  {
    final String basename = FilenameUtils.getBaseName(file.getName());

    File overview = new File(file.getParent(), basename + "-overview.tif");
    try
    {
      FileUtils.copyFile(file, overview);
    }
    catch (IOException e)
    {
      String msg = "Error copying file. Cog generation failed for [" + this.getS3Path() + "].";
      logger.info(msg, e);
      monitor.addError(msg);
      return;
    }

    try
    {
      if (!this.executeProcess(new String[] {
          "gdaladdo", "-r", "average", overview.getAbsolutePath(), "2", "4", "8", "16"
        }))
      {
        String msg = "Problem occurred generating overview file. Cog generation failed for [" + this.getS3Path() + "].";
        logger.info(msg);
        monitor.addError(msg);
        return;
      }
      
      // TODO : Re-run it if it fails?
      
      // If we're already a cog tif:
      // if (this.getStdErr().contains("Adding new overviews invalidates the LAYOUT=IFDS_BEFORE_DATA property"))
      
      File cog = new File(file.getParent(), basename + COG_EXTENSION);
      
      try
      {
        if (!this.executeProcess(new String[] {
            "gdal_translate", overview.getAbsolutePath(), cog.getAbsolutePath(), "-co", "COMPRESS=LZW", "-co", "TILED=YES", "-co", "COPY_SRC_OVERVIEWS=YES"
          }))
        {
          String msg = "Problem occurred generating cog file. Cog generation failed for [" + this.getS3Path() + "].";
          logger.info(msg);
          monitor.addError(msg);
          return;
        }
    
        if (overview.exists())
        {
          super.process(cog);
        }
        else
        {
          logger.info("Problem occurred generating cog file for [" + this.getS3Path() + "]. Overview file did not exist at [" + overview.getAbsolutePath() + "].");
          monitor.addError("Problem occurred generating cog file for [" + this.getS3Path() + "]. Overview file did not exist.");
        }
      }
      finally
      {
        FileUtils.deleteQuietly(cog);
      }
    }
    finally
    {
      FileUtils.deleteQuietly(overview);
    }
  }
}
