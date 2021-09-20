package gov.geoplatform.uasdm.graph;

import java.util.List;

import org.json.JSONObject;

import com.runwaysdk.business.graph.GraphQuery;

import gov.geoplatform.uasdm.model.JSONSerializable;

public class GraphPageQuery<T extends JSONSerializable> extends AbstractGraphPageQuery<T, T>
{
  public GraphPageQuery(String type)
  {
    super(type);
  }

  public GraphPageQuery(String type, JSONObject criteria)
  {
    super(type, criteria);
  }

  protected List<T> getResults(final GraphQuery<T> query)
  {
    return query.getResults();
  }

  protected String getColumnName(String attributeName)
  {
    return attributeName;
  }
}
