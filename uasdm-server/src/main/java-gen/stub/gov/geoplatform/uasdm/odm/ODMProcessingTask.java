package gov.geoplatform.uasdm.odm;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ODMProcessingTask extends ODMProcessingTaskBase implements ODMProcessingTaskIF
{
  private static final long serialVersionUID = -90821820;

  private static Logger     logger           = LoggerFactory.getLogger(ODMStatusServer.class);

  public ODMProcessingTask()
  {
    super();
  }

  public String getImageryComponentOid()
  {
    return this.getComponentOid();
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
      logger.error("Error occurred while initiating ODM Processing.", t);

      this.appLock();
      this.setStatus(ODMStatus.FAILED.getLabel());
      this.setMessage(t.getLocalizedMessage());
      this.apply();
    }
  }

  /**
   * Writes the ODM output to a log file on S3, if supported by the individual
   * task implementation.
   */
  public void writeODMtoS3(JSONArray odmOutput)
  {
    // do nothing, as this does not pertain to Collections
  }
}
