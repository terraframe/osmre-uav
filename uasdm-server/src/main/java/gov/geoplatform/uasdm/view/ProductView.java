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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.dataaccess.ProgrammingErrorException;

import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.LayerClassification;
import gov.geoplatform.uasdm.processing.ODMZipPostProcessor;

public class ProductView
{
  private String             id;

  private String             name;

  private List<SiteItem>     components;

  private String             imageKey;

  private boolean            published;

  private String             boundingBox;

  private boolean            hasPointcloud;

  private boolean            hasAllZip;

  private String             orthoKey;

  private String             demKey;

  private String             publicStacUrl;

  private List<DocumentView> mappables;

  public String getPublicStacUrl()
  {
    return publicStacUrl;
  }

  public void setPublicStacUrl(String publicStacUrl)
  {
    this.publicStacUrl = publicStacUrl;
  }

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

  public boolean isHasAllZip()
  {
    return hasAllZip;
  }

  public void setHasAllZip(boolean hasAllZip)
  {
    this.hasAllZip = hasAllZip;
  }

  public String getBoundingBox()
  {
    return this.boundingBox;
  }

  public void setBoundingBox(String boundingBox)
  {
    this.boundingBox = boundingBox;
  }

  public String getOrthoKey()
  {
    return orthoKey;
  }

  public void setOrthoKey(String orthoKey)
  {
    this.orthoKey = orthoKey;
  }

  public String getDemKey()
  {
    return demKey;
  }

  public void setDemKey(String demKey)
  {
    this.demKey = demKey;
  }

  public boolean isPublished()
  {
    return published;
  }

  public void setPublished(boolean published)
  {
    this.published = published;
  }

  public List<DocumentView> getMappables()
  {
    return mappables;
  }

  public void setMappables(List<DocumentView> mappables)
  {
    this.mappables = mappables;
  }

  public JSONObject toJSON()
  {
    JSONObject object = new JSONObject();
    object.put("id", this.id);
    object.put("name", this.name);
    object.put("entities", SiteItem.serializeItems(this.components));
    object.put("published", this.published);

    if (published)
    {
      object.put("publicStacUrl", publicStacUrl);
    }

    if (this.imageKey != null)
    {
      object.put("imageKey", this.imageKey);
    }

    JSONArray jaLayers = new JSONArray();

    for (DocumentView mappable : this.mappables)
    {
      JSONObject layer = new JSONObject();

      String url;

      try
      {
        url = "api/cog/tilejson.json?path=" + URLEncoder.encode(mappable.getKey(), StandardCharsets.UTF_8.name());
      }
      catch (UnsupportedEncodingException e)
      {
        throw new ProgrammingErrorException(e);
      }

      layer.put("key", mappable.getKey());

      layer.put("url", url);

      layer.put("public", this.published);

      if (mappable.getKey().contains(ImageryComponent.ORTHO + "/"))
      {
        layer.put("classification", LayerClassification.ORTHO.name());
      }
      else if (mappable.getKey().contains(ODMZipPostProcessor.DEM_GDAL + "/"))
      {
        layer.put("classification", LayerClassification.DEM_DSM.name());
      }

      jaLayers.put(layer);
    }

    object.put("orthoKey", this.orthoKey);

    object.put("demKey", this.demKey);

    object.put("layers", jaLayers);

    if (this.boundingBox != null && this.boundingBox.length() > 0)
    {
      object.put("boundingBox", new JSONArray(this.boundingBox));
    }

    object.put("hasPointcloud", this.hasPointcloud);

    object.put("hasAllZip", this.hasAllZip);

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
