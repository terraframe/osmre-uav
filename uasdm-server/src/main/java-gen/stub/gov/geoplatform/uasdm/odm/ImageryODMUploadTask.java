package gov.geoplatform.uasdm.odm;


public class ImageryODMUploadTask extends ImageryODMUploadTaskBase implements ODMUploadTaskIF
{
  public static final long serialVersionUID = -1076802948;
  
  public ImageryODMUploadTask()
  {
    super();
  }
  
  public String getImageryComponentOid()
  {
    return this.getImagery();
  }
  
  public void setProcessingTask(ODMProcessingTaskIF odmProcessingTask)
  {
    this.setProcessingTask((ImageryODMProcessingTask)odmProcessingTask);
  }
}
