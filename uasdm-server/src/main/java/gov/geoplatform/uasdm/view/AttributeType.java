package gov.geoplatform.uasdm.view;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF;
import com.runwaysdk.session.Session;

public class AttributeType
{
  private String    name;

  private String    label;

  private Boolean   required;

  private Boolean   immutable;

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

  public JSONObject toJSON()
  {
    JSONObject object = new JSONObject();
    object.put("name", name);
    object.put("label", label);
    object.put("required", required);
    object.put("immutable", immutable);
    object.put("type", this.getType());

    if (this.condition != null)
    {
      object.put("condition", this.condition.toJSON());
    }

    return object;
  }

  public static AttributeType create(MdAttributeConcreteDAOIF mdAttribute)
  {
    AttributeType attributeType = new AttributeType();

    if (mdAttribute instanceof MdAttributeReferenceDAOIF)
    {
      attributeType = new AttributeListType();
    }

    attributeType.setName(mdAttribute.definesAttribute());
    attributeType.setLabel(mdAttribute.getDisplayLabel(Session.getCurrentLocale()));
    attributeType.setImmutable(mdAttribute.isImmutable());
    attributeType.setRequired(mdAttribute.isRequired());
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
