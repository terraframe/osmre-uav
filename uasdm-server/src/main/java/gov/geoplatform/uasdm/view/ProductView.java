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

import org.json.JSONArray;
import org.json.JSONObject;

import gov.geoplatform.uasdm.geoserver.GeoserverLayer;

public class ProductView
{
  private String         id;

  private String         name;

  private List<SiteItem> components;

  private String         imageKey;

  private boolean        published;

  private String         boundingBox;
  
  private boolean        hasPointcloud;
  
  private List<GeoserverLayer> layers;
  
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

  public String getImageKey()
  {
    return imageKey;
  }

  public void setImageKey(String imageKey)
  {
    this.imageKey = imageKey;
  }

  public boolean getHasPointcloud()
  {
    return hasPointcloud;
  }

  public void setHasPointcloud(boolean hasPointcloud)
  {
    this.hasPointcloud = hasPointcloud;
  }

  public String getBoundingBox()
  {
    return this.boundingBox;
  }

  public void setBoundingBox(String boundingBox)
  {
    this.boundingBox = boundingBox;
  }

  public boolean isPublished()
  {
    return published;
  }

  public void setPublished(boolean published)
  {
    this.published = published;
  }
  
  public List<GeoserverLayer> getLayers()
  {
    return layers;
  }

  public void setLayers(List<GeoserverLayer> layers)
  {
    this.layers = layers;
  }
  
  public JSONObject toJSON()
  {
    JSONObject object = new JSONObject();
    object.put("id", this.id);
    object.put("name", this.name);
    object.put("entities", SiteItem.serializeItems(this.components));
    object.put("published", this.published);

    if (this.imageKey != null)
    {
      object.put("imageKey", this.imageKey);
    }
    
    JSONArray jaLayers = new JSONArray();
    
    for (GeoserverLayer layer : this.layers)
    {
      JSONObject joLayer = new JSONObject();
      
      joLayer.put("workspace", layer.getWorkspace());
      joLayer.put("classification", layer.getClassification().name());
      joLayer.put("key", layer.getStoreName());
      
      jaLayers.put(joLayer);
    }
    
    object.put("layers", jaLayers);
    
    if (this.boundingBox != null && this.boundingBox.length() > 0)
    {
      object.put("boundingBox", new JSONArray(this.boundingBox));
    }
    
    object.put("hasPointcloud", this.hasPointcloud);
    
    return object;
  }

  public static JSONArray serialize(List<ProductView> products)
  {
    JSONArray array = new JSONArray();

    for (ProductView product : products)
    {
      array.put(product.toJSON());
    }

    return array;
  }

}
