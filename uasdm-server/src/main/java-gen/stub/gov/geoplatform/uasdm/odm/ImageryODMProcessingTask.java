package gov.geoplatform.uasdm.odm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.resource.ApplicationResource;

import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.bus.Imagery;

public class ImageryODMProcessingTask extends ImageryODMProcessingTaskBase implements ODMProcessingTaskIF
{
  private static final long serialVersionUID = -897837399;

  final Logger              log              = LoggerFactory.getLogger(ImageryODMProcessingTask.class);

  public ImageryODMProcessingTask()
  {
    super();
  }

  public String getImageryComponentOid()
  {
    return this.getImagery();
  }

  @Override
  public JSONObject toJSON()
  {
    JSONObject obj = super.toJSON();
    obj.put("odmOutput", this.getOdmOutput());

    return obj;
  }

  @Override
  public List<String> getFileList()
  {
    return new LinkedList<String>();
  }

  public void initiate(ApplicationResource images)
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

  /**
   * Writes the ODM output to a log file on S3, if supported by the individual
   * task implementation.
   * 
   * @param odmOutput
   */
  public void writeODMtoS3(JSONArray odmOutput)
  {
    Imagery imagery = this.getImageryInstance();

    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < odmOutput.length(); ++i)
    {
      sb.append(odmOutput.getString(i));
      sb.append("\n");
    }

    try
    {
      File file = File.createTempFile(imagery.getName(), ".txt");
      try
      {
        BufferedWriter writer = null;
        try
        {
          writer = new BufferedWriter(new FileWriter(file));
          writer.write(sb.toString());
        }
        finally
        {
          if (writer != null)
          {
            writer.flush();
            writer.close();
          }
        }

        String geoRefLocation = imagery.buildGeoRefKey();

        Util.uploadFileToS3(file, geoRefLocation + imagery.getName() + ".txt", this);
      }
      finally
      {
        FileUtils.deleteQuietly(file);
      }
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }

  }
}
