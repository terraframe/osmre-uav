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

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Artifact
{
  private List<SiteObject> objects;

  private boolean          report;

  private String           folder;

  private String[]         extensions;

  private Integer          ptEpsg;

  private String           projectionName;

  private String           orthoCorrectionModel;

  public Artifact(String folder, String... extensions)
  {
    this.folder = folder;
    this.extensions = extensions;
    this.report = false;
    this.objects = new LinkedList<SiteObject>();
  }

  public List<SiteObject> getObjects()
  {
    return objects;
  }

  public void setObjects(List<SiteObject> objects)
  {
    this.objects = objects;

    objects.stream().map(a -> a.getPtEpsg()).filter(a -> a != null).findAny().ifPresent(a -> this.setPtEpsg(a));
    objects.stream().map(a -> a.getProjectionName()).filter(a -> a != null).findAny().ifPresent(a -> this.setProjectionName(a));
    objects.stream().map(a -> a.getOrthoCorrectionModel()).filter(a -> a != null).findAny().ifPresent(a -> this.setOrthoCorrectionModel(a));
  }

  public boolean isReport()
  {
    return report;
  }

  public void setReport(boolean report)
  {
    this.report = report;
  }

  public String getFolder()
  {
    return folder;
  }

  public void setFolder(String folder)
  {
    this.folder = folder;
  }

  public String[] getExtensions()
  {
    return extensions;
  }

  public void setExtensions(String[] extensions)
  {
    this.extensions = extensions;
  }
  
  public Integer getPtEpsg()
  {
    return ptEpsg;
  }
  
  public void setPtEpsg(Integer ptEpsg)
  {
    this.ptEpsg = ptEpsg;
  }
  
  public String getProjectionName()
  {
    return projectionName;
  }
  
  public void setProjectionName(String projectionName)
  {
    this.projectionName = projectionName;
  }

  public String getOrthoCorrectionModel()
  {
    return orthoCorrectionModel;
  }

  public void setOrthoCorrectionModel(String orthoCorrectionModel)
  {
    this.orthoCorrectionModel = orthoCorrectionModel;
  }

  public void process(SiteObject object)
  {
    if (object.getKey().contains("/" + this.folder + "/"))
    {
      for (String extension : extensions)
      {
        if (object.getKey().toUpperCase().endsWith(extension))
        {
          this.objects.add(object);

          break;
        }
      }

      if (object.getKey().toUpperCase().endsWith("REPORT.PDF"))
      {
        this.report = true;
      }
    }

    objects.stream().map(a -> a.getPtEpsg()).filter(a -> a != null).findAny().ifPresent(a -> this.setPtEpsg(a));
    objects.stream().map(a -> a.getProjectionName()).filter(a -> a != null).findAny().ifPresent(a -> this.setProjectionName(a));
    objects.stream().map(a -> a.getOrthoCorrectionModel()).filter(a -> a != null).findAny().ifPresent(a -> this.setOrthoCorrectionModel(a));
  }

  public boolean hasObjects()
  {
    return this.objects.size() > 0;
  }

  public JSONObject toJSON()
  {
    return this.toJSON(true);
  }

  public JSONObject toJSON(boolean includeItems)
  {
    JSONArray items = new JSONArray();

    this.objects.forEach(object -> items.put(object.toJSON()));

    JSONObject object = new JSONObject();
    object.put("report", this.report);
    object.put("folder", this.folder);

    if (includeItems)
    {
      object.put("items", items);
    }

    if (ptEpsg != null)
    {
      object.put("ptEpsg", this.ptEpsg);
    }
    
    if (projectionName != null)
    {
      object.put("projectionName", this.projectionName);
    }

    if (orthoCorrectionModel != null)
    {
      object.put("orthoCorrectionModel", this.orthoCorrectionModel);
    }

    return object;
  }
}
