package gov.geoplatform.uasdm.view;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class ProductView
{
  private String         id;

  private String         name;

  private List<SiteItem> components;

  private String         imageKey;

  private String         mapKey;

  private boolean        published;

  private String         workspace;

  private String         boundingBox;

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

  public String getMapKey()
  {
    return mapKey;
  }

  public void setMapKey(String mapKey)
  {
    this.mapKey = mapKey;
  }

  public String getBoundingBox()
  {
    return mapKey;
  }

  public void setBoundingBox(String boundingBox)
  {
    this.boundingBox = boundingBox;
  }

  public String getWorkspace()
  {
    return workspace;
  }

  public void setWorkspace(String workspace)
  {
    this.workspace = workspace;
  }

  public boolean isPublished()
  {
    return published;
  }

  public void setPublished(boolean published)
  {
    this.published = published;
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

    if (this.mapKey != null)
    {
      object.put("mapKey", this.mapKey);
    }

    if (this.workspace != null)
    {
      object.put("workspace", this.workspace);
    }

    if (this.boundingBox != null && this.boundingBox.length() > 0)
    {
      object.put("boundingBox", new JSONArray(this.boundingBox));
    }

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
