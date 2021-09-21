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
package gov.geoplatform.uasdm.graph;

import java.util.List;

import org.json.JSONObject;

import com.runwaysdk.business.graph.GraphQuery;

import gov.geoplatform.uasdm.model.JSONSerializable;

public class GraphPageQuery<T extends JSONSerializable> extends AbstractGraphPageQuery<T, T>
{
  public GraphPageQuery(String type)
  {
    super(type);
  }

  public GraphPageQuery(String type, JSONObject criteria)
  {
    super(type, criteria);
  }

  protected List<T> getResults(final GraphQuery<T> query)
  {
    return query.getResults();
  }

  protected String getColumnName(String attributeName)
  {
    return attributeName;
  }
}
