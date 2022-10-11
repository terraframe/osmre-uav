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
package gov.geoplatform.uasdm.odm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public interface Response
{

  boolean isJSONArray();

  boolean isJSONObject();

  JSONObject getJSONObject() throws JSONException;

  JSONArray getJSONArray() throws JSONException;

  String getResponse();

  void setResponse(String response);

  int getStatusCode();

  void setStatusCode(int statusCode);

  boolean isError();

  boolean isUnreachableHost();

}