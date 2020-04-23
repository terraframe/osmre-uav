package gov.geoplatform.uasdm.model;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Page<T extends JSONSerializable>
{
  private Integer count;

  private Integer pageNumber;

  private Integer pageSize;

  private List<T> results;

  public Page()
  {
    // TODO Auto-generated constructor stub
  }

  public Page(Integer count, Integer pageNumber, Integer pageSize, List<T> results)
  {
    super();
    this.count = count;
    this.pageNumber = pageNumber;
    this.pageSize = pageSize;
    this.results = results;
  }

  public Integer getCount()
  {
    return count;
  }

  public void setCount(Integer count)
  {
    this.count = count;
  }

  public Integer getPageNumber()
  {
    return pageNumber;
  }

  public void setPageNumber(Integer pageNumber)
  {
    this.pageNumber = pageNumber;
  }

  public Integer getPageSize()
  {
    return pageSize;
  }

  public void setPageSize(Integer pageSize)
  {
    this.pageSize = pageSize;
  }

  public List<T> getResults()
  {
    return results;
  }

  public void setResults(List<T> results)
  {
    this.results = results;
  }

  public JSONObject toJSON()
  {
    JSONArray array = new JSONArray();

    for (JSONSerializable result : results)
    {
      array.put(result.toJSON());
    }

    JSONObject object = new JSONObject();
    object.put("count", this.count);
    object.put("pageNumber", this.pageNumber);
    object.put("pageSize", this.pageSize);
    object.put("results", array);

    return object;
  }

}
