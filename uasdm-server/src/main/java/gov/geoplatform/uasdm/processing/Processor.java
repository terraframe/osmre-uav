package gov.geoplatform.uasdm.processing;

import java.io.File;

public interface Processor
{

  public void process(File file);

  public String getS3Path();

}