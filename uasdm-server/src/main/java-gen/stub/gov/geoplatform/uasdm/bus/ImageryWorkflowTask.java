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

import java.text.DateFormat;
import java.util.Locale;

import org.json.JSONObject;

import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.model.ComponentFacade;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ImageryIF;
import gov.geoplatform.uasdm.model.ImageryWorkflowTaskIF;

public class ImageryWorkflowTask extends ImageryWorkflowTaskBase implements ImageryWorkflowTaskIF
{
  private static final long serialVersionUID = 214749939;

  public ImageryWorkflowTask()
  {
    super();
  }

  public ImageryComponent getImageryComponent()
  {
    return this.getImageryInstance();
  }

  public ImageryIF getImageryInstance()
  {
    return ComponentFacade.getImagery(this.getImagery());
  }

  public JSONObject toJSON()
  {
    JSONObject obj = super.toJSON();
    obj.put("uploadId", this.getUploadId());
    obj.put("imagery", this.getImagery());
    obj.put("message", this.getMessage());
    obj.put("status", this.getStatus());
    obj.put("lastUpdateDate", Util.formatIso8601(this.getLastUpdateDate(), true));
    obj.put("createDate", Util.formatIso8601(this.getCreateDate(), true));

    return obj;
  }

  /**
   * Returns a label of a component associated with this task.
   * 
   * @return label of a component associated with this task.
   */
  public String getComponentLabel()
  {
    return this.getImageryInstance().getName();
  }
}
