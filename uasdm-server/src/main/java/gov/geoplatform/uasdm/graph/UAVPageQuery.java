package gov.geoplatform.uasdm.graph;

import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;

import com.runwaysdk.business.graph.GraphQuery;

public class UAVPageQuery extends AbstractGraphPageQuery<UAV, UAVPageView>
{
  public UAVPageQuery(JSONObject criteria)
  {
    super(UAV.CLASS, criteria);
  }

  protected List<UAVPageView> getResults(final GraphQuery<UAV> query)
  {
    List<UAV> results = query.getResults();

    return results.stream().map(uav -> new UAVPageView(uav)).collect(Collectors.toList());
  }

  protected String getColumnName(String attributeName)
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
