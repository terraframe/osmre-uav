package gov.geoplatform.uasdm.odm;

import gov.geoplatform.uasdm.bus.ImageryWorkflowTaskIF;

public interface ODMProcessingTaskIF extends ImageryWorkflowTaskIF 
{ 
  public String getFilePrefix();
  
  public void setFilePrefix(String value);
  
  public String getOdmUUID();
  
  public String getUpLoadId();
  
  public void setOdmUUID(String value);
  
  public void setOdmOutput(String value);
  
  public String getImageryComponentOid();
}
