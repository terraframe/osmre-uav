package gov.geoplatform.uasdm.processing;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.CollectionIF;

public class CogTifProcessor extends SystemProcessProcessor
{
  private Logger logger = LoggerFactory.getLogger(CogTifProcessor.class);
  
  public CogTifProcessor(String filename, AbstractWorkflowTask progressTask, Product product, CollectionIF collection, String s3FolderName, String prefix)
  {
    super(filename, progressTask, product, collection, s3FolderName, prefix, false);
  }

  @Override
  public void processFile(File file)
  {
    final String basename = FilenameUtils.getBaseName(file.getName());

    File overview = new File(file.getParent(), basename + "-overview.tif");
    try
    {
      FileUtils.copyFile(file, overview);
    }
    catch (IOException e)
    {
      logger.error("Error copying file. Cog generation failed for file [" + file.getAbsolutePath() + "].");
      return;
    }

    if (!this.executeProcess(new String[] {
        "gdaladdo", "-r", "average", overview.getAbsolutePath(), "2", "4", "8", "16"
      }))
    {
      logger.error("Problem occurred generating overview file. Cog generation failed for file [" + file.getAbsolutePath() + "].");
      return;
    }
    
    // TODO : Re-run it if it fails?
    
    File cog = new File(file.getParent(), basename + "-cog.tif");
    
    if (!this.executeProcess(new String[] {
        "gdal_translate", overview.getAbsolutePath(), cog.getAbsolutePath(), "-co", "COMPRESS=LZW", "-co", "TILED=YES", "-co", "COPY_SRC_OVERVIEWS=YES"
      }))
    {
      logger.error("Problem occurred generating cog file. Cog generation failed for file [" + file.getAbsolutePath() + "].");
      return;
    }

    if (overview.exists())
    {
      super.processFile(cog);
    }
  }
}
