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
package gov.geoplatform.uasdm.service.request;

import org.json.JSONArray;
import org.json.JSONObject;

public interface CrudService
{
  public JSONObject page(String sessionId, JSONObject criteria);

  public JSONArray getAll(String sessionId);

  public JSONObject apply(String sessionId, JSONObject json);

  public void remove(String sessionId, String oid);

  public JSONObject get(String sessionId, String oid);

  public JSONObject newInstance(String sessionId);
}