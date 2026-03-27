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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.geoplatform.uasdm.serialization.SiteItemSerializer;

public class ImageSetView
{
  private String             id;

  private String             name;

  @JsonSerialize(contentUsing = SiteItemSerializer.class)
  private List<SiteItem>     components;

  private List<DocumentView> documents;

  private boolean            published;

  private boolean            locked;

  private String             boundingBox;

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

  public List<SiteItem> getComponents()
  {
    return components;
  }

  public void setComponents(List<SiteItem> components)
  {
    this.components = components;
  }

  public boolean isPublished()
  {
    return published;
  }

  public void setPublished(boolean published)
  {
    this.published = published;
  }

  public boolean isLocked()
  {
    return locked;
  }

  public void setLocked(boolean locked)
  {
    this.locked = locked;
  }

  public String getBoundingBox()
  {
    return boundingBox;
  }

  public void setBoundingBox(String boundingBox)
  {
    this.boundingBox = boundingBox;
  }

  public List<DocumentView> getDocuments()
  {
    return documents;
  }

  public void setDocuments(List<DocumentView> documents)
  {
    this.documents = documents;
  }
}
