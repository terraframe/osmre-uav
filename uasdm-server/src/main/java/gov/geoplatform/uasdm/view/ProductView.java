package gov.geoplatform.uasdm.view;

import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class ProductView
{
  private String         id;

  private String         name;

  private List<SiteItem> components;

  private String         pilotName;

  private Date           dateTime;

  private String         sensor;

  private String         imageKey;

  private String         mapKey;

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

  public String getPilotName()
  {
    return pilotName;
  }

  public void setPilotName(String pilotName)
  {
    this.pilotName = pilotName;
  }

  public Date getDateTime()
  {
    return dateTime;
  }

  public void setDateTime(Date dateTime)
  {
    this.dateTime = dateTime;
  }

  public String getSensor()
  {
    return sensor;
  }

  public void setSensor(String sensor)
  {
    this.sensor = sensor;
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

  private JSONObject toJSON()
  {
    JSONObject object = new JSONObject();
    object.put("id", this.id);
    object.put("name", this.name);
    object.put("entities", SiteItem.serializeItems(this.components));
    object.put("pilotName", this.pilotName);
    object.put("dateTime", this.dateTime);
    object.put("sensor", this.sensor);

    if (this.imageKey != null)
    {
      object.put("imageKey", this.imageKey);
    }

    if (this.mapKey != null)
    {
      object.put("mapKey", this.mapKey);
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
