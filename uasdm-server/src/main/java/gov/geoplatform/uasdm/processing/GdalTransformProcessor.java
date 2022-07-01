package gov.geoplatform.uasdm.processing;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.CollectionIF;

public class GdalTransformProcessor extends SystemProcessProcessor
{
  public GdalTransformProcessor(String filename, AbstractWorkflowTask progressTask, Product product, CollectionIF collection, String s3FolderName)
  {
    super(filename, progressTask, product, collection, s3FolderName, false);
  }
  
  @Override
  public void processFile(File file, String key)
  {
    final String basename = FilenameUtils.getBaseName(file.getName());

    File png = new File(file.getParent(), basename + ".png");

    // gdal_translate -of PNG odm_orthophoto.tif test.png

    this.executeProcess(new String[] {
        "gdal_translate", "-of", "PNG", file.getAbsolutePath(), png.getAbsolutePath()
    });

    if (png.exists())
    {
      super.processFile(png, key);
    }
  }
}
