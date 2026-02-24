package gov.geoplatform.uasdm.processing;

import gov.geoplatform.uasdm.odm.ODMTaskProcessor.TaskResult;

public class FargateStoreTask extends FargateStoreTaskBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -1812069400;
  
  public FargateStoreTask()
  {
    super();
  }
  
  public void finalize(TaskResult result) {
    new FargateProcessingFinalizer(this, result).finalize();
  }
  
}
