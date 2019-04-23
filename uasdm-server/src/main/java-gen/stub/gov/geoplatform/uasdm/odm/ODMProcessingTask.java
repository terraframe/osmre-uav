package gov.geoplatform.uasdm.odm;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ODMProcessingTask extends ODMProcessingTaskBase
{
  private static final long serialVersionUID = -90821820;
  
  private static Logger logger = LoggerFactory.getLogger(ODMStatusServer.class);
  
  public ODMProcessingTask()
  {
    super();
  }

  public void initiate(File images)
  {
    NewResponse resp = ODMFacade.taskNew(images);
    
    if (resp.getHTTPResponse().isUnreachableHost())
    {
      String msg = "Unable to reach ODM server. code: " + resp.getHTTPResponse().getStatusCode() + " response: " + resp.getHTTPResponse().getResponse();
      logger.error(msg);
      UnreachableHostException ex = new UnreachableHostException(msg);
      
      this.appLock();
      this.setStatus(ODMStatus.FAILED.getLabel());
      this.setMessage(ex.getLocalizedMessage());
      this.apply();
      
      throw ex;
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
}
