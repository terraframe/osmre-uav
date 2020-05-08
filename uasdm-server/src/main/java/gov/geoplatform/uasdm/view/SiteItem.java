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

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.geotools.geojson.geom.GeometryJSON;
import org.json.JSONArray;
import org.json.JSONObject;

import com.vividsolutions.jts.geom.Geometry;

public class SiteItem implements TreeComponent
{
  public static String        ID                 = "id";

  public static String        TYPE               = "type";

  public static String        TYPE_LABEL         = "typeLabel";

  public static String        OWNER_NAME         = "ownerName";

  public static String        OWNER_PHONE        = "ownerPhone";

  public static String        OWNER_EMAIL        = "ownerEmail";

  public static String        PRIVILEGE_TYPE     = "privilegeType";

  public static String        NUMBER_OF_CHILDREN = "numberOfChildren";

  public static String        METADATA_UPLOADED  = "metadataUploaded";

  public static String        CHILDREN           = "children";

  public static String        GEOMETRY           = "geometry";

  private String              id;

  private String              type;

  private String              typeLabel;

  private String              ownerName;

  private String              ownerPhone;

  private String              ownerEmail;

  private String              privilegeType;

  private Boolean             metadataUploaded;

  private Geometry            geometry;

  private Integer             numberOfChildren;

  private List<TreeComponent> children;

  private Map<String, Object> values;

  private List<AttributeType> attributes;

  public SiteItem()
  {
    this.children = new LinkedList<TreeComponent>();
    this.values = new HashMap<String, Object>();
    this.metadataUploaded = false;
  }

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

  public void setTypeLabel(String typeLabel)
  {
    this.typeLabel = typeLabel;
  }

  public String getOwnerName()
  {
    return ownerName;
  }

  public void setOwnerName(String ownerName)
  {
    this.ownerName = ownerName;
  }

  public String getOwnerPhone()
  {
    return ownerPhone;
  }

  public void setOwnerPhone(String ownerPhone)
  {
    this.ownerPhone = ownerPhone;
  }

  public String getOwnerEmail()
  {
    return ownerEmail;
  }

  public void setOwnerEmail(String ownerEmail)
  {
    this.ownerEmail = ownerEmail;
  }

  public String getPrivilegeType()
  {
    return privilegeType;
  }

  public void setPrivilegeType(String privilegeType)
  {
    this.privilegeType = privilegeType;
  }

  public Boolean getMetadataUploaded()
  {
    return metadataUploaded;
  }

  public void setMetadataUploaded(Boolean metadataUploaded)
  {
    this.metadataUploaded = metadataUploaded;
  }

  public Integer getNumberOfChildren()
  {
    return numberOfChildren;
  }

  public void setNumberOfChildren(Integer numberOfChildren)
  {
    this.numberOfChildren = numberOfChildren;
  }

  public void addChild(TreeComponent child)
  {
    this.children.add(child);
  }

  public Geometry getGeometry()
  {
    return geometry;
  }

  public void setGeometry(Geometry point)
  {
    this.geometry = point;
  }

  public Object getValue(String attributeName)
  {
    return this.values.get(attributeName);
  }

  public void setValue(String attributeName, Object value)
  {
    this.values.put(attributeName, value);
  }

  public Map<String, Object> getValues()
  {
    return values;
  }

  public void setAttributes(List<AttributeType> attributes)
  {
    this.attributes = attributes;
  }

  public List<AttributeType> getAttributes()
  {
    return attributes;
  }

  public JSONObject toJSON()
  {
    try
    {
      Set<Entry<String, Object>> entries = this.values.entrySet();

      JSONObject obj = new JSONObject();
      obj.put(ID, this.id);
      obj.put(TYPE, this.type);
      obj.put(TYPE_LABEL, this.typeLabel);
      obj.put(NUMBER_OF_CHILDREN, this.numberOfChildren);
      obj.put(OWNER_NAME, this.getOwnerName());
      obj.put(OWNER_PHONE, this.getOwnerPhone());
      obj.put(OWNER_EMAIL, this.getOwnerEmail());
      obj.put(METADATA_UPLOADED, this.getMetadataUploaded());
      obj.put(PRIVILEGE_TYPE, this.getPrivilegeType());

      for (Entry<String, Object> entry : entries)
      {
        obj.put(entry.getKey(), entry.getValue());
      }

      if (this.children != null && this.children.size() > 0)
      {
        obj.put(CHILDREN, SiteItem.serialize(this.children));
      }

      if (this.geometry != null)
      {
        StringWriter geomWriter = new StringWriter();
        new GeometryJSON().write(this.getGeometry(), geomWriter);

        obj.put(GEOMETRY, new JSONObject(geomWriter.toString()));
      }

      return obj;
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  public static JSONArray serialize(Iterable<TreeComponent> items)
  {
    JSONArray array = new JSONArray();

    for (TreeComponent item : items)
    {
      array.put(item.toJSON());
    }

    return array;
  }

  public static JSONArray serializeItems(Iterable<SiteItem> items)
  {
    JSONArray array = new JSONArray();

    for (SiteItem item : items)
    {
      array.put(item.toJSON());
    }

    return array;
  }

  @SuppressWarnings("unchecked")
  public static SiteItem deserialize(JSONObject object)
  {
    SiteItem item = new SiteItem();
    item.setId(object.getString(ID));
    item.setType(object.getString(TYPE));
    item.setTypeLabel(object.getString(TYPE_LABEL));
//    item.setNumberOfChildren(object.getBoolean(HAS_CHILDREN));

    // These fields are not set on the object, but rather are properties of the
    // owner itself.
    // OWNER_NAME
    // OWNER_PHONE
    // OWNER_EMAIL
    if (item.getType().equals("Collection"))
    {
      item.setPrivilegeType(object.getString(PRIVILEGE_TYPE));
    }

    Iterator<String> keys = object.keys();

    while (keys.hasNext())
    {
      String attributeName = keys.next();

      if (isValid(attributeName))
      {
        item.setValue(attributeName, object.get(attributeName));
      }
    }

    if (object.has(GEOMETRY))
    {
      try
      {
        Object oGeom = object.get(GEOMETRY);

        String sGeom = oGeom.toString();

        item.setGeometry(new GeometryJSON().readPoint(sGeom));
      }
      catch (IOException e)
      {
        throw new RuntimeException(e);
      }
    }

    return item;
  }

  private static boolean isValid(String attributeName)
  {
    if (attributeName.equals(ID))
    {
      return false;
    }

    if (attributeName.equals(TYPE))
    {
      return false;
    }

    if (attributeName.equals(TYPE_LABEL))
    {
      return false;
    }

    if (attributeName.equals(NUMBER_OF_CHILDREN))
    {
      return false;
    }

    if (attributeName.equals(GEOMETRY))
    {
      return false;
    }

    if (attributeName.equals(CHILDREN))
    {
      return false;
    }

    return true;
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
