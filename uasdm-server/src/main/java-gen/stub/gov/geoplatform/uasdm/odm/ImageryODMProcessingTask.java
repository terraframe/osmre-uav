package gov.geoplatform.uasdm.odm;

import java.io.File;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageryODMProcessingTask extends ImageryODMProcessingTaskBase implements ODMProcessingTaskIF
{
  private static final long serialVersionUID = -897837399;
  
  final Logger               log              = LoggerFactory.getLogger(ImageryODMProcessingTask.class);
  
  public ImageryODMProcessingTask()
  {
    super();
  }
  
  public String getImageryComponentOid()
  {
    return this.getImageryOid();
  }
  
  @Override
  public JSONObject toJSON()
  {
    JSONObject obj = super.toJSON();
    obj.put("odmOutput", this.getOdmOutput());

    return obj;
  }

  public void initiate(File images)
  {
    try
    {
      NewResponse resp = ODMFacade.taskNew(images);
      
      if (resp.getHTTPResponse().isUnreachableHost())
      {
        String msg = "Unable to reach ODM server. code: " + resp.getHTTPResponse().getStatusCode() + " response: " + resp.getHTTPResponse().getResponse();
        throw new UnreachableHostException(msg);
      }
      else if (resp.hasError())
      {
        this.appLock();
        this.setStatus(ODMStatus.FAILED.getLabel());
        this.setMessage(resp.getError());
        this.apply();
      }
      else if (!resp.hasError() && resp.getHTTPResponse().isError())
      {
        this.appLock();
        this.setStatus(ODMStatus.FAILED.getLabel());
        this.setMessage("The job encountered an unspecified error.");
        this.apply();
      }
      else
      {
        this.appLock();
        this.setStatus(ODMStatus.RUNNING.getLabel());
        this.setOdmUUID(resp.getUUID());
        this.setMessage("Your images are being processed. Check back later for updates.");
        this.apply();
        
        ODMStatusServer.addTask(this);
      }
    }
    catch (Throwable t)
    {
      log.error("Error occurred while initiating ODM Processing.", t);
      
      this.appLock();
      this.setStatus(ODMStatus.FAILED.getLabel());
      this.setMessage(t.getLocalizedMessage());
      this.apply();
    }
  }
}
