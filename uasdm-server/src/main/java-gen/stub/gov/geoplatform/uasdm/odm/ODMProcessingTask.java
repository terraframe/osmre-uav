/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.geoplatform.uasdm.odm;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.RunwayException;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.session.Session;

import gov.geoplatform.uasdm.DevProperties;
import gov.geoplatform.uasdm.bus.CollectionReport;

public class ODMProcessingTask extends ODMProcessingTaskBase implements ODMProcessingTaskIF
{
  private static final long serialVersionUID = -90821820;

  private static Logger logger = LoggerFactory.getLogger(ODMStatusServer.class);

  public ODMProcessingTask()
  {
    super();
  }

  public String getImageryComponentOid()
  {
    return this.getComponent();
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
    List<String> list = new LinkedList<String>();
    String filenames = this.getProcessFilenameArray();

    if (filenames != null && filenames.length() > 0)
    {
      JSONArray array = new JSONArray(filenames);

      for (int i = 0; i < array.length(); i++)
      {
        list.add(array.getString(i));
      }
    }

    return list;
  }

  public void initiate(ApplicationResource images, boolean isMultispectral)
  {
    try
    {
      NewResponse resp;

      if (DevProperties.runOrtho())
      {
        resp = ODMFacade.taskNew(images, isMultispectral);
      }
      else
      {
        resp = new HttpNewResponse(new HttpResponse("{\"uuid\":\"TEST\"}", 200));
      }

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

        CollectionReport.update(this.getImageryComponentOid(), ODMStatus.FAILED.getLabel());
      }
      else if (!resp.hasError() && resp.getHTTPResponse().isError())
      {
        this.appLock();
        this.setStatus(ODMStatus.FAILED.getLabel());
        this.setMessage("The job encountered an unspecified error. [" + resp.getHTTPResponse().getStatusCode() + "]. " + resp.getHTTPResponse().getResponse() + ".");
        this.apply();

        CollectionReport.update(this.getImageryComponentOid(), ODMStatus.FAILED.getLabel());
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
    catch (EmptyFileSetException e)
    {
      this.appLock();
      this.setStatus(ODMStatus.COMPLETED.getLabel());
      this.setMessage("No image files to be processed.");
      this.apply();
    }
    catch (Throwable t)
    {
      logger.error("Error occurred while initiating ODM Processing.", t);

      this.appLock();
      this.setStatus(ODMStatus.FAILED.getLabel());
      this.setMessage(RunwayException.localizeThrowable(t, Session.getCurrentLocale()));
      this.apply();

      CollectionReport.update(this.getImageryComponentOid(), ODMStatus.FAILED.getLabel());
    }
    finally
    {
      images.close();
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

  public static ODMProcessingTask getByUploadId(String uploadId)
  {
    ODMProcessingTaskQuery query = new ODMProcessingTaskQuery(new QueryFactory());
    query.WHERE(query.getUploadId().EQ(uploadId));

    try (OIterator<? extends ODMProcessingTask> iterator = query.getIterator())
    {
      if (iterator.hasNext())
      {
        return iterator.next();
      }
    }

    return null;
  }

}
