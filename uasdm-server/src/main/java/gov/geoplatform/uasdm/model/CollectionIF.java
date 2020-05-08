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
package gov.geoplatform.uasdm.model;

import java.util.List;

import org.json.JSONObject;

import gov.geoplatform.uasdm.bus.AllPrivilegeType;
import gov.geoplatform.uasdm.bus.Sensor;

public interface CollectionIF extends UasComponentIF
{
  public void addPrivilegeType(AllPrivilegeType privilegeType);

  public List<AllPrivilegeType> getPrivilegeType();

  public Integer getImageWidth();

  public Integer getImageHeight();

  public void setMetadataUploaded(Boolean metadataUploaded);

  public Boolean getMetadataUploaded();

  public void apply();

  public Sensor getSensor();

  public JSONObject toMetadataMessage();

  public void appLock();

}
