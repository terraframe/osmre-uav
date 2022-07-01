package gov.geoplatform.uasdm.processing;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.CollectionIF;

public class HillshadeProcessor extends SystemProcessProcessor
{
  public HillshadeProcessor(String filename, AbstractWorkflowTask progressTask, Product product, CollectionIF collection, String s3FolderName, String prefix)
  {
    super(filename, progressTask, product, collection, s3FolderName, prefix, false);
  }

  @Override
  public void processFile(File file)
  {
    final String basename = FilenameUtils.getBaseName(file.getName());

    File hillshade = new File(file.getParent(), basename + "-gdal.tif");

    this.executeProcess(new String[] {
        "gdaldem", "hillshade", file.getAbsolutePath(), hillshade.getAbsolutePath()
    });

    if (hillshade.exists())
    {
      super.processFile(hillshade);
    }
  }
}