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

import org.json.JSONObject;

public class ProductCriteria
{
  public static final Integer SITE     = 0;

  public static final Integer LOCATION = 1;

  private Integer             type;

  private String              id;

  private String              hierarchy;

  private String              organization;

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
  
  public String getOrganization()
  {
    return organization;
  }
  
  public void setOrganization(String organization)
  {
    this.organization = organization;
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
    
    if (object.has("organization"))
    {
      criteria.setOrganization(object.getString("organization"));
    }


    return criteria;
  }
}
