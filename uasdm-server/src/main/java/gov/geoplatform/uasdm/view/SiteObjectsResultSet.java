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

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import gov.geoplatform.uasdm.remote.RemoteFileFacade;

public class SiteObjectsResultSet
{
  private Long             total;

  private Long             pageNumber;

  private Long             pageSize;

  private List<SiteObject> objects;

  private String           folder;

  public SiteObjectsResultSet(Integer total, Long pageNumber, Long pageSize, List<SiteObject> objects, String folder)
  {
    this(Long.valueOf(total.longValue()), pageNumber, pageSize, objects, folder);
  }

  public SiteObjectsResultSet(Long total, Long pageNumber, Long pageSize, List<SiteObject> objects, String folder)
  {
    this.total = total;
    this.pageNumber = pageNumber;
    this.pageSize = pageSize;
    this.objects = objects;
    this.folder = folder;
  }

  public Long getTotalObjects()
  {
    return total;
  }

  public void setTotalObjects(Long maxObjects)
  {
    this.total = maxObjects;
  }

  public Long getPageNumber()
  {
    return pageNumber;
  }

  public void setPageNumber(Long pageNumber)
  {
    this.pageNumber = pageNumber;
  }

  public Long getPageSize()
  {
    return pageSize;
  }

  public void setPageSize(Long pageSize)
  {
    this.pageSize = pageSize;
  }

  public List<SiteObject> getObjects()
  {
    return objects;
  }

  public void setObjects(List<SiteObject> objects)
  {
    this.objects = objects;
  }

  public String getFolder()
  {
    return folder;
  }

  public void setFolder(String folder)
  {
    this.folder = folder;
  }
  
  public JSONObject toJSON()
  {
    return toJSON(false);
  }

  public JSONObject toJSON(boolean presignThumbnails)
  {
    JSONObject json = new JSONObject();

    json.put("count", total);
    json.put("pageNumber", pageNumber);
    json.put("pageSize", pageSize);

    List<TreeComponent> items = new LinkedList<TreeComponent>();
    items.addAll(objects);
    json.put("results", SiteItem.serialize(items));
    
    if (presignThumbnails)
    {
      this.presignThumbnails(json.getJSONArray("results"));
    }

    json.put("folder", folder);

    return json;
  }
  
  private void presignThumbnails(JSONArray ja)
  {
    if (ja == null) { return; }
    
    for (int i = 0; i < ja.length(); ++i)
    {
      try
      {
        JSONObject jo = ja.getJSONObject(i);
        
        if (jo.has(SiteObject.KEY))
        {
          String presigned = RemoteFileFacade.presignUrl(getThumbnailPath(jo.getString(SiteObject.KEY)), Duration.of(24, ChronoUnit.HOURS)).toString();
          jo.put(SiteObject.PRESIGNED_THUMBNAIL_DOWNLOAD, presigned);
        }
      }
      catch(RuntimeException e)
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

}
