package gov.geoplatform.uasdm.processing;

import org.apache.commons.lang3.StringUtils;

import gov.geoplatform.uasdm.lidar.LidarProcessConfiguration;
import gov.geoplatform.uasdm.model.ProcessConfiguration;
import gov.geoplatform.uasdm.odm.ODMTaskProcessor.TaskResult;

public class FargateStoreTask extends FargateStoreTaskBase implements FargateTaskIF
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -1812069400;
  
  public FargateStoreTask()
  {
    super();
  }
  
  public void finalize(TaskResult result) {
    FargateProcessingFinalizer.factory(this, result).finalize();
  }
  
  public ProcessConfiguration getConfiguration()
  {
    String json = this.getUploadConfigurationJson();
    
    if (!StringUtils.isEmpty(json))
    {
      return ProcessConfiguration.parse(json);
    }

    return null;
  }

  public void setConfiguration(ProcessConfiguration configuration)
  {
    this.setUploadConfigurationJson(configuration.toJson().toString());
  }
  
}
