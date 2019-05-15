package gov.geoplatform.uasdm.view;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.amazonaws.services.s3.model.S3ObjectSummary;

import gov.geoplatform.uasdm.bus.UasComponent;

public class SiteObject implements TreeComponent
{
  public static final String KEY       = "key";

  public static final String NAME      = "name";

  public static final String COMPONENT = "component";

  public static final String FOLDER    = "folder";

  public static final String OBJECT    = "object";

  public static final String IMAGE_KEY = "imageKey";

  private String             id;

  private String             name;

  private String             componentId;

  private String             key;

  private String             type;

  private String             imageKey;

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

    if (this.type.equals(SiteObject.FOLDER))
    {
      json.put(SiteItem.HAS_CHILDREN, true);
    }

    if (this.imageKey != null)
    {
      json.put(SiteObject.IMAGE_KEY, this.imageKey);
    }

    return json;
  }

  public static SiteObject create(UasComponent component, String prefix, S3ObjectSummary summary)
  {
    String key = summary.getKey();
    String name = key.replaceFirst(prefix + "/", "");

    SiteObject object = new SiteObject();
    object.setId(component.getOid() + "-" + key);
    object.setName(name);
    object.setComponentId(component.getOid());
    object.setKey(key);
    object.setType(SiteObject.OBJECT);

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
