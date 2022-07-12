package gov.geoplatform.uasdm.processing;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.CollectionIF;

public class GdalTransformProcessor extends ManagedDocument
{
  private Logger logger = LoggerFactory.getLogger(GdalTransformProcessor.class);
  
  public GdalTransformProcessor(String s3Path, Product product, CollectionIF collection, StatusMonitorIF monitor)
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

    File png = new File(file.getParent(), basename + ".png");

    // gdal_translate -of PNG odm_orthophoto.tif test.png

    boolean success = new SystemProcessExecutor(this.monitor).execute(new String[] {
        "gdal_translate", "-of", "PNG", file.getAbsolutePath(), png.getAbsolutePath()
    });

    if (success && png.exists())
    {
      return super.process(png);
    }
    else
    {
      logger.info("Problem occurred generating gdal transform. PNG file did not exist at [" + png.getAbsolutePath() + "].");
      monitor.addError("Problem occurred generating gdal transform. PNG file did not exist.");
    }
    
    return false;
  }
}
