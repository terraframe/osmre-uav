package gov.geoplatform.uasdm.bus;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONObject;

import gov.geoplatform.uasdm.view.SiteItem;
import gov.geoplatform.uasdm.view.SiteObject;
import gov.geoplatform.uasdm.view.TreeComponent;

public class SiteObjectsResultSet
{
  private Integer total;
  
  private Integer pageNumber;
  
  private Integer pageSize;
  
  private List<SiteObject> objects;
  
  private String folder;
  
  public SiteObjectsResultSet(Integer total, Integer pageNumber, Integer pageSize, List<SiteObject> objects, String folder)
  {
    this.total = total;
    this.pageNumber = pageNumber;
    this.pageSize = pageSize;
    this.objects = objects;
    this.folder = folder;
  }

  public Integer getTotalObjects()
  {
    return total;
  }

  public void setTotalObjects(Integer maxObjects)
  {
    this.total = maxObjects;
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

  public List<SiteObject> getObjects()
  {
    return objects;
  }

  public void setObjects(List<SiteObject> objects)
  {
    this.objects = objects;
  }

  public String getFolder()
  {
    return folder;
  }

  public void setFolder(String folder)
  {
    this.folder = folder;
  }

  public JSONObject toJSON()
  {
    JSONObject json = new JSONObject();
    
    json.put("count", total);
    json.put("pageNumber", pageNumber);
    json.put("pageSize", pageSize);
    
    List<TreeComponent> items = new LinkedList<TreeComponent>();
    items.addAll(objects);
    json.put("results", SiteItem.serialize(items));
    
    json.put("folder", folder);
    
    return json;
  }
  
}
