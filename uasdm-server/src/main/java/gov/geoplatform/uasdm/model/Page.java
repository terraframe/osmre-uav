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

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.amazonaws.HttpMethod;

import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.view.SiteObject;

public class Page<T extends JSONSerializable>
{
  private Long                count;

  private Integer             pageNumber;

  private Integer             pageSize;

  private List<T>             results;

  private Boolean             presignThumnails = false;

  private Map<String, Object> params;

  public Page()
  {
    this.params = new HashMap<String, Object>();
    this.results = new LinkedList<>();
    this.count = 0L;
    this.pageSize = 20;
    this.pageNumber = 1;
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

  public Boolean getPresignThumnails()
  {
    return presignThumnails;
  }

  public void setPresignThumnails(Boolean presignThumnails)
  {
    this.presignThumnails = presignThumnails;
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

    if (Boolean.TRUE.equals(presignThumnails))
    {
      presignThumbnails(object.getJSONArray("resultSet"));
    }

    return object;
  }

  private void presignThumbnails(JSONArray ja)
  {
    if (ja == null)
    {
      return;
    }

    for (int i = 0; i < ja.length(); ++i)
    {
      try
      {
        JSONObject jo = ja.getJSONObject(i);

        if (jo.has(SiteObject.KEY))
        {
          Calendar cal = Calendar.getInstance();
          cal.add(Calendar.HOUR, 24);
          String presigned = RemoteFileFacade.presignUrl(getThumbnailPath(jo.getString(SiteObject.KEY)), cal.getTime(), HttpMethod.GET).toString();
          jo.put(SiteObject.PRESIGNED_THUMBNAIL_DOWNLOAD, presigned);
        }
      }
      catch (RuntimeException e)
      {
        // Do nothing. This object doesn't have to exist.
      }
    }
  }

  private String getThumbnailPath(String key)
  {
    String rootPath = key.substring(0, key.lastIndexOf("/"));
    String fileName = FilenameUtils.getBaseName(key);

    return rootPath + "/thumbnails/" + fileName + ".png";
  }

  public <K extends JSONSerializable> Page<K> map(Function<T, K> function)
  {
    Page<K> page = new Page<K>(this.count, this.pageNumber, this.pageSize, this.results.stream().map(function).collect(Collectors.toList()));
    page.setPresignThumnails(this.presignThumnails);

    return page;
  }

}
