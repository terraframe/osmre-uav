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
package gov.geoplatform.uasdm.mock;

import java.util.Date;
import java.util.UUID;

import org.json.JSONObject;

import gov.geoplatform.uasdm.odm.InfoResponse;
import gov.geoplatform.uasdm.odm.ODMStatus;

public class MockInfoResponse extends MockODMResponse implements InfoResponse
{
  private String uuid;

  public MockInfoResponse()
  {
    this.uuid = UUID.randomUUID().toString();
  }

  @Override
  public String getUUID()
  {
    return this.uuid;
  }

  @Override
  public Date getDateCreated()
  {
    return new Date();
  }

  @Override
  public Long getProcessingTime()
  {
    return 1L;
  }

  @Override
  public Long getImagesCount()
  {
    return 1L;
  }

  @Override
  public JSONObject getOptions()
  {
    return new JSONObject();
  }

  @Override
  public ODMStatus getStatus()
  {
    return ODMStatus.COMPLETED;
  }

  @Override
  public String getStatusError()
  {
    return "Mock Error message";
  }
}
