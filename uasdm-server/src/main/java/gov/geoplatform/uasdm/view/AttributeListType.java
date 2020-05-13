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

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class AttributeListType extends AttributeType
{
  private List<Option> options;

  @Override
  protected String getType()
  {
    return "list";
  }

  public List<Option> getOptions()
  {
    return options;
  }

  public void setOptions(List<Option> options)
  {
    this.options = options;
  }

  @Override
  public JSONObject toJSON()
  {
    JSONArray array = new JSONArray();

    if (this.options != null)
    {
      for (Option option : this.options)
      {
        array.put(option.toJSON());
      }
    }

    JSONObject object = super.toJSON();
    object.put("options", array);

    return object;
  }
}
