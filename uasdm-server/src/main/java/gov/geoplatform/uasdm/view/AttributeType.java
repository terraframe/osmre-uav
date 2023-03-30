/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.geoplatform.uasdm.view;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.dataaccess.MdAttributeBooleanDAOIF;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDateDAOIF;
import com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF;
import com.runwaysdk.dataaccess.MdAttributeTextDAOIF;
import com.runwaysdk.gis.dataaccess.MdAttributePointDAOIF;
import com.runwaysdk.session.Session;

public class AttributeType
{
  private String    name;

  private String    label;

  private Boolean   required;

  private Boolean   immutable;

  private Boolean   readOnly;

  private Condition condition;

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }

  public Boolean getRequired()
  {
    return required;
  }

  public void setRequired(Boolean required)
  {
    this.required = required;
  }

  public Boolean getImmutable()
  {
    return immutable;
  }

  public void setImmutable(Boolean immutable)
  {
    this.immutable = immutable;
  }

  protected String getType()
  {
    return "text";
  }

  public Condition getCondition()
  {
    return condition;
  }

  public void setCondition(Condition condition)
  {
    this.condition = condition;
  }

  public Boolean getReadOnly()
  {
    return readOnly;
  }

  public void setReadOnly(Boolean readOnly)
  {
    this.readOnly = readOnly;
  }

  public JSONObject toJSON()
  {
    JSONObject object = new JSONObject();
    object.put("name", name);
    object.put("label", label);
    object.put("required", required);
    object.put("immutable", immutable);
    object.put("type", this.getType());
    object.put("readonly", this.getReadOnly());

    if (this.condition != null)
    {
      object.put("condition", this.condition.toJSON());
    }

    return object;
  }

  public static AttributeType create(MdAttributeConcreteDAOIF mdAttribute)
  {
    return AttributeType.create(mdAttribute, false, null);
  }

  public static AttributeType create(MdAttributeConcreteDAOIF mdAttribute, boolean readOnly, Condition condition)
  {
    AttributeType attributeType = new AttributeType();

    if (mdAttribute instanceof MdAttributeReferenceDAOIF)
    {
      attributeType = new AttributeListType();
    }
    else if (mdAttribute instanceof MdAttributeTextDAOIF)
    {
      attributeType = new AttributeTextType();
    }
    else if (mdAttribute instanceof MdAttributePointDAOIF)
    {
      attributeType = new AttributePointType();
    }
    else if (mdAttribute instanceof MdAttributeBooleanDAOIF)
    {
      attributeType = new AttributeBooleanType();
    }
    else if (mdAttribute instanceof MdAttributeDateDAOIF)
    {
      attributeType = new AttributeDateType();
    }

    attributeType.setName(mdAttribute.definesAttribute());
    attributeType.setLabel(mdAttribute.getDisplayLabel(Session.getCurrentLocale()));
    attributeType.setImmutable(mdAttribute.isImmutable());
    attributeType.setRequired(mdAttribute.isRequired());
    attributeType.setReadOnly(readOnly);

    if (condition != null)
    {
      attributeType.setCondition(condition);
    }

    return attributeType;
  }

  public static JSONArray toJSON(List<AttributeType> attributes)
  {
    JSONArray array = new JSONArray();

    for (AttributeType attribute : attributes)
    {
      array.put(attribute.toJSON());
    }

    return array;
  }
}
