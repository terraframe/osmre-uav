package gov.geoplatform.uasdm.index;

import gov.geoplatform.uasdm.service.IndexService;
import net.geoprism.context.ServerContextListener;

public class IndexContextListener implements ServerContextListener
{

  @Override
  public void initialize()
  {
  }

  @Override
  public void startup()
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void shutdown()
  {
    IndexService.shutdown();
  }

}
