package gov.geoplatform.uasdm.processing;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.resource.FileResource;

import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.CollectionIF;

public class CogTifProcessor extends ManagedDocument
{
  public static final String COG_EXTENSION = ".cog.tif";
  
  private Logger logger = LoggerFactory.getLogger(CogTifProcessor.class);
  
  public CogTifProcessor(String s3path, Product product, CollectionIF collection, StatusMonitorIF monitor)
  {
    super(s3path, product, collection, monitor, false);
  }
  
  @Override
  protected ManagedDocumentTool getTool()
  {
    return ManagedDocumentTool.GDAL;
  }

  @Override
  public boolean process(File file)
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
      return false;
    }

    try
    {
      final SystemProcessExecutor exec = new SystemProcessExecutor(this.monitor);
      
      if (!exec.execute(new String[] {
          "gdaladdo", "-r", "average", overview.getAbsolutePath(), "2", "4", "8", "16"
        }))
      {
        String msg = "Problem occurred generating overview file. Cog generation failed for [" + this.getS3Path() + "].";
        logger.info(msg);
        monitor.addError(msg);
        return false;
      }
      
      File cog = new File(file.getParent(), basename + COG_EXTENSION);
      
      try
      {
        if (!exec.execute(new String[] {
            "gdal_translate", overview.getAbsolutePath(), cog.getAbsolutePath(), "-co", "COMPRESS=LZW", "-co", "TILED=YES", "-co", "COPY_SRC_OVERVIEWS=YES"
          }))
        {
          String msg = "Problem occurred generating cog file. Cog generation failed for [" + this.getS3Path() + "].";
          logger.info(msg);
          monitor.addError(msg);
          return false;
        }
        
        if (cog.exists())
        {
          if (new CogTifValidator(this.monitor).isValidCog(cog))
          {
            return super.process(cog);
          }
          else
          {
            logger.info("Problem occurred validating cog file for [" + this.getS3Path() + "].");
          }
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
    
    return false;
  }
}
