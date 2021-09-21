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
package gov.geoplatform.uasdm.graph;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.query.OrderBy.SortOrder;

import gov.geoplatform.uasdm.model.JSONSerializable;
import gov.geoplatform.uasdm.model.Page;

public abstract class AbstractGraphPageQuery<K, T extends JSONSerializable>
{
  private String     type;

  private JSONObject criteria;

  public AbstractGraphPageQuery(String type)
  {
    this(type, new JSONObject());
  }

  public AbstractGraphPageQuery(String type, JSONObject criteria)
  {
    super();
    this.type = type;
    this.criteria = criteria;
  }

  protected abstract List<T> getResults(final GraphQuery<K> query);

  protected abstract String getColumnName(String attributeName);

  @SuppressWarnings("unchecked")
  public Long getCount()
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(this.type);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT COUNT(*) FROM " + mdVertex.getDBClassName() + "");

    Map<String, Object> parameters = new HashMap<String, Object>();

    if (criteria.has("filters"))
    {
      JSONObject filters = criteria.getJSONObject("filters");
      Iterator<String> keys = filters.keys();

      int i = 0;

      while (keys.hasNext())
      {
        String attributeName = keys.next();

        MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(attributeName);

        if (mdAttribute != null)
        {
          String columnName = this.getColumnName(attributeName);

          JSONObject filter = filters.getJSONObject(attributeName);

          String value = filter.get("value").toString();
          String mode = filter.get("matchMode").toString();

          if (mode.equals("contains"))
          {
            parameters.put(attributeName, "%" + value + "%");

            statement.append( ( ( i == 0 ) ? " WHERE " : "AND " ) + columnName + ".toUpperCase() LIKE :" + attributeName);
          }
          else if (mode.equals("equals"))
          {
            parameters.put(attributeName, value);

            statement.append( ( ( i == 0 ) ? " WHERE " : "AND " ) + columnName + " = :" + attributeName);
          }

          i++;
        }
      }
    }

    final GraphQuery<Long> query = new GraphQuery<Long>(statement.toString(), parameters);

    return query.getSingleResult();
  }

  @SuppressWarnings("unchecked")
  public Page<T> getPage()
  {
    int pageSize = 10;
    int pageNumber = 1;

    Long count = this.getCount();

    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(this.type);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName() + "");

    Map<String, Object> parameters = new HashMap<String, Object>();

    if (criteria.has("filters"))
    {
      JSONObject filters = criteria.getJSONObject("filters");
      Iterator<String> keys = filters.keys();

      int i = 0;

      while (keys.hasNext())
      {
        String attributeName = keys.next();

        MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(attributeName);

        if (mdAttribute != null)
        {
          String columnName = this.getColumnName(attributeName);

          JSONObject filter = filters.getJSONObject(attributeName);

          String value = filter.get("value").toString();
          String mode = filter.get("matchMode").toString();

          if (mode.equals("contains"))
          {
            parameters.put(attributeName, "%" + value.toUpperCase() + "%");

            statement.append( ( ( i == 0 ) ? " WHERE " : "AND " ) + columnName + ".toUpperCase() LIKE :" + attributeName);
          }
          else if (mode.equals("equals"))
          {
            parameters.put(attributeName, value);

            statement.append( ( ( i == 0 ) ? " WHERE " : "AND " ) + columnName + " = :" + attributeName);
          }

          i++;
        }
      }
    }

    if (criteria.has("sortField") && criteria.has("sortOrder"))
    {
      String field = criteria.getString("sortField");
      SortOrder order = criteria.getInt("sortOrder") == 1 ? SortOrder.ASC : SortOrder.DESC;

      statement.append(" ORDER BY " + this.getColumnName(field) + " " + order.name());
    }
    else if (criteria.has("multiSortMeta"))
    {
      JSONArray sorts = criteria.getJSONArray("multiSortMeta");

      for (int i = 0; i < sorts.length(); i++)
      {
        JSONObject sort = sorts.getJSONObject(i);

        String field = sort.getString("field");
        SortOrder order = sort.getInt("order") == 1 ? SortOrder.ASC : SortOrder.DESC;

        if (i == 0)
        {
          statement.append(" ORDER BY " + this.getColumnName(field) + " " + order.name());
        }
        else
        {
          statement.append(", " + this.getColumnName(field) + " " + order.name());
        }
      }
    }

    if (criteria.has("first") && criteria.has("rows"))
    {
      int first = criteria.getInt("first");
      int rows = criteria.getInt("rows");

      statement.append(" SKIP " + first + " LIMIT " + rows);

      pageNumber = ( first / rows ) + 1;
    }

    final GraphQuery<K> query = new GraphQuery<K>(statement.toString(), parameters);

    return new Page<T>(count, pageNumber, pageSize, this.getResults(query));
  }
}
