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
package gov.geoplatform.uasdm.mock;

import java.io.IOException;
import java.net.URISyntaxException;

import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.resource.CloseableFile;

import gov.geoplatform.uasdm.odm.InfoResponse;
import gov.geoplatform.uasdm.odm.NewResponse;
import gov.geoplatform.uasdm.odm.ODMProcessConfiguration;
import gov.geoplatform.uasdm.odm.ODMResponse;
import gov.geoplatform.uasdm.odm.ODMServiceIF;
import gov.geoplatform.uasdm.odm.TaskOutputResponse;
import gov.geoplatform.uasdm.odm.TaskRemoveResponse;
import gov.geoplatform.uasdm.util.FileTestUtils;

public class MockODMService implements ODMServiceIF
{

  @Override
  public TaskOutputResponse taskOutput(String uuid)
  {
    return new MockTaskOutputResponse();
  }

  @Override
  public TaskRemoveResponse taskRemove(String uuid)
  {
    return new MockTaskRemoveResponse();
  }

  @Override
  public CloseableFile taskDownload(String uuid)
  {
    try
    {
      return new CloseableFile(FileTestUtils.createZip(this.getClass().getResource("/all").toURI(), "all.zip"), false);
    }
    catch (URISyntaxException | IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  public NewResponse taskNew(ApplicationResource images, boolean isMultispectral, ODMProcessConfiguration configuration)
  {
    return new MockNewResponse();
  }

  @Override
  public NewResponse taskNewInit(int imagesCount, boolean isMultispectral, ODMProcessConfiguration configuration)
  {
    return new MockNewResponse();
  }

  @Override
  public ODMResponse taskNewUpload(String uuid, ApplicationResource image)
  {
    return new MockODMResponse();
  }

  @Override
  public ODMResponse taskNewCommit(String uuid)
  {
    return new MockODMResponse();
  }

  @Override
  public InfoResponse taskInfo(String uuid)
  {
    return new MockInfoResponse();
  }

}
