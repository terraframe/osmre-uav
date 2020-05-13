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

import org.json.JSONObject;

public class EqCondition implements Condition
{
  private String name;

  private String value;

  public EqCondition(String name, String value)
  {
    super();
    this.name = name;
    this.value = value;
  }

  @Override
  public JSONObject toJSON()
  {
    JSONObject object = new JSONObject();
    object.put("name", name);
    object.put("value", value);
    object.put("type", "eq");

    return object;
  }

}
