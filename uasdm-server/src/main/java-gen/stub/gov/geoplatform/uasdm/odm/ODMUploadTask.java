package gov.geoplatform.uasdm.odm;


public class ODMUploadTask extends ODMUploadTaskBase implements ODMUploadTaskIF
{
  private static final long serialVersionUID = 1872276502;
  
  public ODMUploadTask()
  {
    super();
  }
  
  public String getImageryComponentOid()
  {
    return this.getComponentOid();
  }
  
  public void setProcessingTask(ODMProcessingTaskIF odmProcessingTask)
  {
    this.setProcessingTask((ODMProcessingTask)odmProcessingTask);
  }
}
