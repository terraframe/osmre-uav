package gov.geoplatform.uasdm.graph;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.Selectable;
import com.runwaysdk.query.SelectableChar;
import com.runwaysdk.query.OrderBy.SortOrder;

import gov.geoplatform.uasdm.bus.CollectionReport;
import gov.geoplatform.uasdm.bus.CollectionReportQuery;
import gov.geoplatform.uasdm.model.JSONSerializable;
import gov.geoplatform.uasdm.model.Page;

public interface Classification extends JSONSerializable
{
  public String getOid();

  public Long getSeq();

  public String getCode();

  public void setCode(String value);

  public String getLabel();

  public void setLabel(String value);

  @Override
  default JSONObject toJSON()
  {
    JSONObject object = new JSONObject();
    object.put("oid", this.getOid());
    object.put("code", this.getCode());
    object.put("label", this.getLabel());

    if (this.getSeq() != null)
    {
      object.put("seq", this.getSeq());
    }

    return object;
  }

  public static Long getCount(String type)
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(type);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT COUNT(*) FROM " + mdVertex.getDBClassName() + "");

    final GraphQuery<Long> query = new GraphQuery<Long>(statement.toString());

    return query.getSingleResult();
  }

  public static Page<Classification> getPage(String type, JSONObject criteria)
  {
    int pageSize = 10;
    int pageNumber = 1;

    final Long count = Classification.getCount(type);

    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(type);

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

    if (criteria.has("sortField") && criteria.has("sortOrder"))
    {
      String field = criteria.getString("sortField");
      SortOrder order = criteria.getInt("sortOrder") == 1 ? SortOrder.ASC : SortOrder.DESC;

      statement.append(" ORDER BY " + field + " " + order.name());
    }

    if (criteria.has("first") && criteria.has("rows"))
    {
      int first = criteria.getInt("first");
      int rows = criteria.getInt("rows");

      statement.append(" SKIP " + first + " LIMIT " + rows);

      pageNumber = ( first / rows ) + 1;
    }

    final GraphQuery<Classification> query = new GraphQuery<Classification>(statement.toString(), parameters);

    return new Page<Classification>(count, pageNumber, pageSize, query.getResults());
  }

  public static JSONArray getAll(String type)
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(type);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName() + "");
    statement.append(" ORDER BY code");

    final GraphQuery<Classification> query = new GraphQuery<Classification>(statement.toString());
    List<Classification> results = query.getResults();

    return results.stream().map(w -> w.toJSON()).collect(Collector.of(JSONArray::new, JSONArray::put, JSONArray::put));
  }

}
