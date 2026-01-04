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
package gov.geoplatform.uasdm.controller.body;

import javax.validation.constraints.NotNull;

import org.json.JSONArray;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import gov.geoplatform.uasdm.serialization.JSONArrayDeserializer;

public class CollectionBody
{
  @NotNull
  @JsonDeserialize(using = JSONArrayDeserializer.class)
  private JSONArray selections;

  public JSONArray getSelections()
  {
    return selections;
  }

  public void setSelections(JSONArray selections)
  {
    this.selections = selections;
  }
}
