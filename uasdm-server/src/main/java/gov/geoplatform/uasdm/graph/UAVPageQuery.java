package gov.geoplatform.uasdm.graph;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.query.OrderBy.SortOrder;

import gov.geoplatform.uasdm.model.Page;

public class UAVPageQuery
{
  private JSONObject criteria;

  public UAVPageQuery(JSONObject criteria)
  {
    super();
    this.criteria = criteria;
  }

  @SuppressWarnings("unchecked")
  public Long getCount()
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(UAV.CLASS);

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

          JSONObject filter = filters.getJSONObject(attributeName);

          String value = filter.get("value").toString();
          String mode = filter.get("matchMode").toString();

          if (mode.equals("contains"))
          {
            parameters.put(attributeName, "%" + value + "%");

            statement.append( ( ( i == 0 ) ? " WHERE " : "AND " ) + attributeName + ".toUpperCase() LIKE :" + attributeName);
          }
          else if (mode.equals("equals"))
          {
            parameters.put(attributeName, value);

            statement.append( ( ( i == 0 ) ? " WHERE " : "AND " ) + attributeName + " = :" + attributeName);
          }

          i++;
        }
      }
    }

    final GraphQuery<Long> query = new GraphQuery<Long>(statement.toString(), parameters);

    return query.getSingleResult();
  }

  @SuppressWarnings("unchecked")
  public Page<UAVPageView> getPage()
  {
    int pageSize = 10;
    int pageNumber = 1;

    Long count = this.getCount();

    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(UAV.CLASS);

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
        String columnName = this.getColumnName(attributeName);

        if (mdAttribute != null)
        {
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

    final GraphQuery<UAV> query = new GraphQuery<UAV>(statement.toString(), parameters);

    List<UAVPageView> results = getResults(query);

    return new Page<UAVPageView>(count, pageNumber, pageSize, results);
  }

  private List<UAVPageView> getResults(final GraphQuery<UAV> query)
  {
    List<UAV> results = query.getResults();

    return results.stream().map(uav -> new UAVPageView(uav)).collect(Collectors.toList());
  }

  private String getColumnName(String attributeName)
  {
    if (attributeName.equals(UAV.BUREAU))
    {
      return UAV.BUREAU + "." + Bureau.NAME;
    }
    else if (attributeName.equals(UAV.PLATFORM))
    {
      return UAV.PLATFORM + "." + Platform.NAME;
    }

    return attributeName;
  }

}
