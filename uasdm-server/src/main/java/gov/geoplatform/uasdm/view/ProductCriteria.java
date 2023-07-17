package gov.geoplatform.uasdm.view;

import org.json.JSONObject;

public class ProductCriteria
{
  public static final Integer SITE     = 0;

  public static final Integer LOCATION = 1;

  private Integer             type;

  private String              id;

  private String              hierarchy;

  private String              uid;

  private String              sortField;

  private String              sortOrder;

  public ProductCriteria()
  {
    this.type = SITE;
  }

  public Integer getType()
  {
    return type;
  }

  public void setType(Integer type)
  {
    this.type = type;
  }

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public String getHierarchy()
  {
    return hierarchy;
  }

  public void setHierarchy(String hierarchy)
  {
    this.hierarchy = hierarchy;
  }

  public String getUid()
  {
    return uid;
  }

  public void setUid(String uid)
  {
    this.uid = uid;
  }

  public String getSortField()
  {
    return sortField;
  }

  public void setSortField(String sortField)
  {
    this.sortField = sortField;
  }

  public String getSortOrder()
  {
    return sortOrder;
  }

  public void setSortOrder(String sortOrder)
  {
    this.sortOrder = sortOrder;
  }

  public static ProductCriteria deserialize(String jsonString)
  {
    return deserialize(new JSONObject(jsonString));
  }

  public static ProductCriteria deserialize(JSONObject object)
  {
    ProductCriteria criteria = new ProductCriteria();

    if (object.has("type"))
    {
      criteria.setType(object.getInt("type"));
    }

    if (object.has("id"))
    {
      criteria.setId(object.getString("id"));
    }

    if (object.has("uid"))
    {
      criteria.setUid(object.getString("uid"));
    }

    if (object.has("hierarchy"))
    {
      criteria.setHierarchy(object.getString("hierarchy"));
    }

    if (object.has("sortField"))
    {
      criteria.setSortField(object.getString("sortField"));
    }

    if (object.has("sortOrder"))
    {
      criteria.setSortOrder(object.getString("sortOrder"));
    }

    return criteria;
  }
}
