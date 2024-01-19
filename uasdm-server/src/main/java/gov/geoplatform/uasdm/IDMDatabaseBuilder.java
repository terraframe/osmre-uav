package gov.geoplatform.uasdm;

import gov.geoplatform.uasdm.processing.report.QueuedCollectionReportProcessor;
import gov.geoplatform.uasdm.service.IndexService;
import net.geoprism.build.GeoprismDatabaseBuilder;
import net.geoprism.build.GeoprismDatabaseBuilderIF;

public class IDMDatabaseBuilder extends GeoprismDatabaseBuilder implements GeoprismDatabaseBuilderIF
{
  @Override
  public void run()
  {
    super.run();
    
    invokeShutdownHooks();
  }
  
  private void invokeShutdownHooks()
  {
    // Place mandatory shutdown hooks (without which will cause the patcher to hang) here.
    
    IndexService.shutdown();
    QueuedCollectionReportProcessor.shutdown();
  }
}
