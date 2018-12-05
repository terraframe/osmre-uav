package gov.geoplatform.uasdm.view;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class SiteItem
{
  public static String ID           = "id";

  public static String TYPE         = "type";

  public static String TYPE_LABEL   = "typeLabel";

  public static String NAME         = "name";

  public static String HAS_CHILDREN = "hasChildren";

  private String       id;

  private String       type;

  private String       typeLabel;

  private String       name;

  private Boolean      hasChildren;

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String getTypeLabel()
  {
    return typeLabel;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public void setTypeLabel(String typeLabel)
  {
    this.typeLabel = typeLabel;
  }

  public Boolean getHasChildren()
  {
    return hasChildren;
  }

  public void setHasChildren(Boolean hasChildren)
  {
    this.hasChildren = hasChildren;
  }

  public JSONObject toJSON()
  {
    JSONObject obj = new JSONObject();
    obj.put(ID, this.id);
    obj.put(TYPE, this.type);
    obj.put(TYPE_LABEL, this.typeLabel);
    obj.put(NAME, this.name);
    obj.put(HAS_CHILDREN, this.hasChildren);
    return obj;
  }

  public static JSONArray serialize(Iterable<SiteItem> items)
  {
    JSONArray array = new JSONArray();

    for (SiteItem item : items)
    {
      array.put(item.toJSON());
    }

    return array;
  }

  public static SiteItem deserialize(JSONObject object)
  {
    SiteItem item = new SiteItem();
    item.setId(object.getString(ID));
    item.setType(object.getString(TYPE));
    item.setTypeLabel(object.getString(TYPE_LABEL));
    item.setName(object.getString(NAME));
    item.setHasChildren(object.getBoolean(HAS_CHILDREN));

    return item;
  }

  public static List<SiteItem> deserialize(JSONArray array)
  {
    LinkedList<SiteItem> list = new LinkedList<SiteItem>();

    for (int i = 0; i < array.length(); i++)
    {
      SiteItem item = deserialize(array.getJSONObject(i));

      list.add(item);
    }

    return list;
  }

}
