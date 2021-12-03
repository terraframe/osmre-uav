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
package gov.geoplatform.uasdm.view;

import java.util.Date;

import org.json.JSONObject;

import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.Page;

public class ProductDetailView extends ProductView
{
  private Page<DocumentIF> page;

  private String           pilotName;

  private Date             dateTime;
  
  private Date             collectionDate;

  private JSONObject       sensor;
  
  private JSONObject       platform;
  
  private JSONObject       UAV;

  public String getPilotName()
  {
    return pilotName;
  }

  public void setPilotName(String pilotName)
  {
    this.pilotName = pilotName;
  }

  public Date getDateTime()
  {
    return dateTime;
  }

  public void setDateTime(Date dateTime)
  {
    this.dateTime = dateTime;
  }
  
  public JSONObject getSensor()
  {
    return sensor;
  }

  public void setSensor(JSONObject sensor)
  {
    this.sensor = sensor;
  }

  public JSONObject getPlatform()
  {
    return platform;
  }

  public void setPlatform(JSONObject platform)
  {
    this.platform = platform;
  }

  public JSONObject getUAV()
  {
    return UAV;
  }

  public void setUAV(JSONObject uAV)
  {
    UAV = uAV;
  }
  
  public Date getCollectionDate()
  {
    return collectionDate;
  }

  public void setCollectionDate(Date collectionDate)
  {
    this.collectionDate = collectionDate;
  }

  public Page<DocumentIF> getPage()
  {
    return page;
  }

  public void setPage(Page<DocumentIF> page)
  {
    this.page = page;
  }

  public JSONObject toJSON()
  {
    JSONObject object = super.toJSON();
    object.put("pilotName", this.getPilotName());
    object.put("dateTime", Util.formatIso8601(this.getDateTime(), true));
    object.put("collectionDate", Util.formatIso8601(this.getCollectionDate(), false));
    
    object.put("sensor", this.getSensor());
    object.put("platform", this.getPlatform());
    object.put("uav", this.getUAV());
    
    object.put("page", page.toJSON());

    return object;
  }

}
