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
package gov.geoplatform.uasdm.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class Page<T extends JSONSerializable>
{
  private Long                count;

  private Integer             pageNumber;

  private Integer             pageSize;

  private List<T>             results;

  private Map<String, Object> params;

  public Page()
  {
    this.params = new HashMap<String, Object>();
  }

  public Page(Integer count, Integer pageNumber, Integer pageSize, List<T> results)
  {
    this(count.longValue(), pageNumber, pageSize, results);
  }

  public Page(Long count, Integer pageNumber, Integer pageSize, List<T> results)
  {
    super();
    this.params = new HashMap<String, Object>();
    this.count = count;
    this.pageNumber = pageNumber;
    this.pageSize = pageSize;
    this.results = results;
  }

  public Long getCount()
  {
    return count;
  }

  public void setCount(Long count)
  {
    this.count = count;
  }

  public Integer getPageNumber()
  {
    return pageNumber;
  }

  public void setPageNumber(Integer pageNumber)
  {
    this.pageNumber = pageNumber;
  }

  public Integer getPageSize()
  {
    return pageSize;
  }

  public void setPageSize(Integer pageSize)
  {
    this.pageSize = pageSize;
  }

  public List<T> getResults()
  {
    return results;
  }

  public void setResults(List<T> results)
  {
    this.results = results;
  }

  public void addParam(String key, Object value)
  {
    this.params.put(key, value);
  }

  public JSONObject toJSON()
  {
    JSONArray array = new JSONArray();

    for (JSONSerializable result : results)
    {
      array.put(result.toJSON());
    }

    JSONObject object = new JSONObject();
    object.put("count", this.count);
    object.put("pageNumber", this.pageNumber);
    object.put("pageSize", this.pageSize);
    object.put("resultSet", array);

    Set<Entry<String, Object>> entries = this.params.entrySet();

    for (Entry<String, Object> entry : entries)
    {
      object.put(entry.getKey(), entry.getValue());
    }

    return object;
  }

}
