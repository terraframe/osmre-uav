package gov.osmre.uasdm.view;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class SiteItem
{
  private String  id;

  private String  name;

  private Boolean hasChildren;

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
    obj.put("id", this.id);
    obj.put("name", this.name);
    obj.put("hasChildren", this.hasChildren);
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
    item.setId(object.getString("id"));
    item.setName(object.getString("name"));
    item.setHasChildren(object.getBoolean("hasChildren"));

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
