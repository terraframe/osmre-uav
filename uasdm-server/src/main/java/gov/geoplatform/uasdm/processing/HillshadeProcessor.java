package gov.geoplatform.uasdm.processing;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.CollectionIF;

public class HillshadeProcessor extends ManagedDocument
{
  private Logger logger = LoggerFactory.getLogger(HillshadeProcessor.class);
  
  public HillshadeProcessor(String s3Path, Product product, CollectionIF collection, StatusMonitorIF monitor)
  {
    super(s3Path, product, collection, monitor, false);
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

    File hillshade = new File(file.getParent(), basename + "-gdal" + CogTifProcessor.COG_EXTENSION);

    boolean success = new SystemProcessExecutor(this.monitor).execute(new String[] {
        "gdaldem", "hillshade", file.getAbsolutePath(), hillshade.getAbsolutePath()
    });

    if (success && hillshade.exists())
    {
      super.process(hillshade);
    }
    else
    {
      logger.info("Problem occurred generating gdal transform. Hillshade file did not exist at [" + hillshade.getAbsolutePath() + "].");
      monitor.addError("Problem occurred generating gdal transform. Hillshade file did not exist.");
    }
    
    return false;
  }
}