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

import java.util.Date;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.runwaysdk.ComponentIF;

import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.UasComponentIF;

public class SiteObject implements TreeComponent
{
  public static final String KEY                    = "key";

  public static final String NAME                   = "name";

  public static final String COMPONENT              = "component";

  public static final String FOLDER                 = "folder";

  public static final String OBJECT                 = "object";

  public static final String IMAGE_KEY              = "imageKey";

  public static final String LAST_MODIFIED_KEY      = "lastModified";

  public static final String EXCLUDE                = "exclude";

  public static final String DESCRIPTION            = "description";

  public static final String TOOL                   = "tool";

  public static final String PT_EPSG                = "ptEpsg";

  public static final String PROJECTION_NAME        = "projectionName";

  public static final String ORTHO_CORRECTION_MODEL = "orthoCorrectionModel";
  
  public static final String FILE_SIZE              = "fileSize";

  private String             id;

  private String             name;

  private String             componentId;

  private String             key;

  private String             type;

  private String             imageKey;

  private Date               lastModified;

  private Boolean            exclude;

  private String             description;

  private String             tool;

  private Integer            ptEpsg;

  private String             projectionName;

  private String             orthoCorrectionModel;
  
  private long               fileSize; // The size of the file in bytes

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getComponentId()
  {
    return componentId;
  }

  public void setComponentId(String componentId)
  {
    this.componentId = componentId;
  }

  public String getKey()
  {
    return key;
  }

  public void setKey(String key)
  {
    this.key = key;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String getImageKey()
  {
    return imageKey;
  }

  public void setImageKey(String imageKey)
  {
    this.imageKey = imageKey;
  }

  public Date getLastModified()
  {
    return lastModified;
  }

  public void setLastModified(Date lastModified)
  {
    this.lastModified = lastModified;
  }

  public Boolean getExclude()
  {
    return exclude;
  }

  public void setExclude(Boolean exclude)
  {
    this.exclude = exclude;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getTool()
  {
    return tool;
  }

  public void setTool(String tool)
  {
    this.tool = tool;
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
  
  public long getFileSize()
  {
    return fileSize;
  }

  public void setFileSize(long fileSize)
  {
    this.fileSize = fileSize;
  }

  @Override
  public void addChild(TreeComponent child)
  {
    throw new UnsupportedOperationException();
  }

  public JSONObject toJSON()
  {
    JSONObject json = new JSONObject();
    json.put(SiteItem.ID, this.id);
    json.put(SiteItem.TYPE, this.type);
    json.put(SiteObject.NAME, this.name);
    json.put(SiteObject.KEY, this.key);
    json.put(SiteObject.COMPONENT, this.componentId);
    json.put(SiteObject.LAST_MODIFIED_KEY, this.lastModified);
    json.put(SiteObject.EXCLUDE, this.exclude);
    json.put(SiteObject.DESCRIPTION, this.description);
    json.put(SiteObject.TOOL, this.tool);
    json.put(SiteObject.PT_EPSG, this.ptEpsg);
    json.put(SiteObject.PROJECTION_NAME, this.projectionName);
    json.put(SiteObject.ORTHO_CORRECTION_MODEL, this.orthoCorrectionModel);
    json.put(SiteObject.FILE_SIZE, this.fileSize);

    // if (this.type.equals(SiteObject.FOLDER))
    // {
    // json.put(SiteItem.HAS_CHILDREN, true);
    // }

    if (this.imageKey != null)
    {
      json.put(SiteObject.IMAGE_KEY, this.imageKey);
    }

    return json;
  }

  public static SiteObject create(UasComponentIF component, String prefix, S3ObjectSummary summary)
  {
    String key = summary.getKey();
    String name = FilenameUtils.getName(key);

    SiteObject object = new SiteObject();
    object.setId(component.getOid() + "-" + key);
    object.setName(name);
    object.setComponentId(component.getOid());
    object.setKey(key);
    object.setType(SiteObject.OBJECT);
    object.setLastModified(summary.getLastModified());
    object.setExclude(false);
    object.setFileSize(summary.getSize());

    return object;
  }

  public static SiteObject create(ComponentIF component, DocumentIF document)
  {
    String key = document.getS3location();
    String name = FilenameUtils.getName(key);

    SiteObject object = new SiteObject();
    object.setId(document.getOid());
    object.setName(name);
    object.setComponentId(component.getOid());
    object.setKey(key);
    object.setType(SiteObject.OBJECT);
    object.setExclude(document.getExclude() != null && document.getExclude());
    object.setLastModified(document.getLastModified());
    object.setDescription(document.getDescription());
    object.setTool(document.getTool());
    object.setPtEpsg(document.getPtEpsg());
    object.setProjectionName(document.getProjectionName());
    object.setOrthoCorrectionModel(document.getOrthoCorrectionModel());
    object.setFileSize(document.getFileSize() == null ? -1L : document.getFileSize());

    return object;
  }

  public static JSONArray serialize(List<SiteObject> objects)
  {
    JSONArray array = new JSONArray();

    for (SiteObject object : objects)
    {
      array.put(object.toJSON());
    }

    return array;
  }
}
