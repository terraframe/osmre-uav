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
package gov.geoplatform.uasdm.graph;

import org.json.JSONObject;

import gov.geoplatform.uasdm.model.JSONSerializable;

public class UAVPageView implements JSONSerializable
{
  private UAV uav;

  public UAVPageView(UAV uav)
  {
    super();

    this.uav = uav;
  }

  public UAV getUav()
  {
    return uav;
  }

  @Override
  public Object toJSON()
  {
    JSONObject object = new JSONObject();
    object.put(UAV.OID, this.uav.getOid());
    object.put(UAV.SERIALNUMBER, this.uav.getSerialNumber());
    object.put(UAV.FAANUMBER, this.uav.getFaaNumber());
    object.put(UAV.DESCRIPTION, this.uav.getDescription());
    object.put(UAV.BUREAU, this.uav.getBureau().getDisplayLabel());
    object.put(UAV.PLATFORM, this.uav.getPlatform().getName());

    if (this.uav.getSeq() != null)
    {
      object.put(UAV.SEQ, this.uav.getSeq());
    }

    return object;
  }

}
