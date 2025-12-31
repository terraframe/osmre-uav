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
package gov.geoplatform.uasdm.bus;

import org.apache.commons.lang.StringUtils;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ProcessConfiguration;
import gov.geoplatform.uasdm.odm.ODMProcessConfiguration;
import gov.geoplatform.uasdm.processing.ProcessingInProgressException;

public abstract class AbstractUploadTask extends AbstractUploadTaskBase
{
  private static final long serialVersionUID = 1447500757;

  public AbstractUploadTask()
  {
    super();
  }

  public abstract String getUploadTarget();

  public ProcessConfiguration getConfiguration()
  {
    String json = this.getUploadConfigurationJson();

    if (!StringUtils.isEmpty(json))
    {
      return ProcessConfiguration.parse(json);
    }

    return new ODMProcessConfiguration();
  }

  public void setConfiguration(ProcessConfiguration configuration)
  {
    if (configuration != null)
    {
      this.setUploadConfigurationJson(configuration.toJson().toString());
    }
    else
    {
      this.setUploadConfigurationJson(null);
    }
  }

  public static AbstractUploadTask getTaskByUploadId(String uploadId)
  {
    AbstractUploadTaskQuery query = new AbstractUploadTaskQuery(new QueryFactory());
    query.WHERE(query.getUploadId().EQ(uploadId));

    try (OIterator<? extends AbstractUploadTask> it = query.getIterator())
    {
      if (it.hasNext())
      {
        AbstractUploadTask task = it.next();

        if (it.hasNext())
        {
          ProcessingInProgressException ex = new ProcessingInProgressException();
          throw ex;
        }

        return task;
      }
    }

    return null;
  }

  public abstract ImageryComponent getImageryComponent();
}
