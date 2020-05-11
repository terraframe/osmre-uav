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

public class WorkflowAction extends WorkflowActionBase
{
  private static final long serialVersionUID = -893446595;

  public WorkflowAction()
  {
    super();
  }

  public JSONObject toJSON()
  {
    DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.US);

    JSONObject obj = new JSONObject();
    obj.put("createDate", format.format(this.getCreateDate()));
    obj.put("lastUpdatedDate", format.format(this.getLastUpdateDate()));
    obj.put("type", this.getActionType());
    obj.put("description", this.getDescription());

    return obj;
  }
}
