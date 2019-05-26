package gov.geoplatform.uasdm.odm;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.ImageryComponent;
import gov.geoplatform.uasdm.bus.ImageryWorkflowTaskIF;

public interface ODMUploadTaskIF extends ImageryWorkflowTaskIF
{ 
  public void apply();
 
  public String getOdmUUID();
  
  public void lock();
  
  /**
   * {@link AbstractWorkflowTask#setMessage(String)
   * 
   * @param value
   */
  public void setMessage(String value);
  
  /**
   * {@link AbstractWorkflowTask#setStatus(String)
   * 
   * @param value
   */
  public void setStatus(String value);
  
  
  public ImageryComponent getImageryComponent();
  
  public ODMProcessingTaskIF getProcessingTask();
  
  public void setProcessingTask(ODMProcessingTaskIF odmProcessingTask);
  
  public String getImageryComponentOid();
}
