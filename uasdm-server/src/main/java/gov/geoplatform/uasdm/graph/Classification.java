package gov.geoplatform.uasdm.graph;

import java.util.List;
import java.util.stream.Collector;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;

import gov.geoplatform.uasdm.model.JSONSerializable;
import gov.geoplatform.uasdm.model.Page;

public interface Classification extends JSONSerializable
{
  public String getOid();

  public Long getSeq();

  public String getName();

  public void setName(String value);

  @Override
  default JSONObject toJSON()
  {
    JSONObject object = new JSONObject();
    object.put("oid", this.getOid());
    object.put("name", this.getName());

    if (this.getSeq() != null)
    {
      object.put("seq", this.getSeq());
    }

    return object;
  }

  public static Long getCount(String type)
  {
    GraphPageQuery<Classification> query = new GraphPageQuery<Classification>(type);

    return query.getCount();
  }

  public static Page<Classification> getPage(String type, JSONObject criteria)
  {
    GraphPageQuery<Classification> query = new GraphPageQuery<Classification>(type, criteria);

    return query.getPage();
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
