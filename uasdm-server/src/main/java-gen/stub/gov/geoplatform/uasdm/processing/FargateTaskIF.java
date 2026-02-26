package gov.geoplatform.uasdm.processing;

import gov.geoplatform.uasdm.graph.ProcessingRun;
import gov.geoplatform.uasdm.model.AbstractWorkflowTaskIF;
import gov.geoplatform.uasdm.model.ProcessConfiguration;

public interface FargateTaskIF extends AbstractWorkflowTaskIF
{
  public ProcessingRun getProcessingRun();
  
  public String getTaskArn();
  
  public ProcessConfiguration getConfiguration();

  public String getProcessingJobId();
}
