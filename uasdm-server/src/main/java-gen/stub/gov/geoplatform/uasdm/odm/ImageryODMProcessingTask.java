/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.geoplatform.uasdm.odm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.RunwayException;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.resource.ArchiveFileResource;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.session.Session;

import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ImageryIF;
import gov.geoplatform.uasdm.processing.WorkflowTaskMonitor;
import gov.geoplatform.uasdm.processing.report.CollectionReportFacade;
import gov.geoplatform.uasdm.ws.GlobalNotificationMessage;
import gov.geoplatform.uasdm.ws.MessageType;
import gov.geoplatform.uasdm.ws.NotificationFacade;

public class ImageryODMProcessingTask extends ImageryODMProcessingTaskBase implements ODMProcessingTaskIF
{
  private static final long serialVersionUID = -897837399;

  final Logger              log              = LoggerFactory.getLogger(ImageryODMProcessingTask.class);

  public ImageryODMProcessingTask()
  {
    super();
  }

  public ODMProcessConfiguration getConfiguration()
  {
    String json = this.getConfigurationJson();

    if (!StringUtils.isEmpty(json))
    {
      return ODMProcessConfiguration.parse(json);
    }

    return new ODMProcessConfiguration();
  }

  public void setConfiguration(ODMProcessConfiguration configuration)
  {
    this.setConfigurationJson(configuration.toJson().toString());
  }

  public String getImageryComponentOid()
  {
    return this.getImagery();
  }

  @Override
  public Boolean getProcessDem()
  {
    return false;
  }

  @Override
  public Boolean getProcessOrtho()
  {
    return false;
  }

  @Override
  public Boolean getProcessPtcloud()
  {
    return false;
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

  public void initiate(ArchiveFileResource archive)
  {
    try
    {
      NewResponse resp = ODMFacade.taskNew(archive, false, this.getConfiguration(), (Collection) this.getImageryComponent(), this);

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
        this.setMessage("The job encountered an unspecified error. [" + resp.getHTTPResponse().getStatusCode() + "]. " + resp.getHTTPResponse().getResponse() + ".");
        this.apply();
      }
      else
      {
        this.appLock();
        this.setStatus(ODMStatus.RUNNING.getLabel());
        this.setOdmUUID(resp.getUUID());
        this.setMessage("Your images are being processed. Check back later for updates.");
        this.apply();

        NotificationFacade.queue(new GlobalNotificationMessage(MessageType.JOB_CHANGE, null));

        ODMStatusServer.addTask(this);
      }
    }
    catch (Throwable t)
    {
      log.error("Error occurred while initiating ODM Processing.", t);

      this.appLock();
      this.setStatus(ODMStatus.FAILED.getLabel());
      this.setMessage(RunwayException.localizeThrowable(t, Session.getCurrentLocale()));
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
    ImageryIF imagery = this.getImageryInstance();

    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < odmOutput.length(); ++i)
    {
      sb.append(odmOutput.getString(i));
      sb.append("\n");
    }

    try (CloseableFile file = new CloseableFile(File.createTempFile(imagery.getName(), ".txt")))
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

      Util.uploadFileToS3(file, geoRefLocation + imagery.getName() + ".txt", new WorkflowTaskMonitor(this));

      ImageryComponent component = this.getImageryComponent();

      if (component instanceof CollectionIF)
      {
        CollectionReportFacade.updateSize((CollectionIF) component).doIt();
      }
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }
}
