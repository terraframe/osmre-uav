package gov.geoplatform.uasdm.processing;

import java.io.File;

public interface Processor
{

  void process(File file);

  String getFileName();

  String getS3FolderName();

  void handleUnprocessed();

}