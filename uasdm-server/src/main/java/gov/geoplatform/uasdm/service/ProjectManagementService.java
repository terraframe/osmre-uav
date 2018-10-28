package gov.geoplatform.uasdm.service;

import gov.geoplatform.uasdm.bus.Site;
import gov.geoplatform.uasdm.bus.SiteQuery;
import gov.geoplatform.uasdm.bus.UasComponent;
import gov.geoplatform.uasdm.view.SiteItem;

import java.util.LinkedList;
import java.util.List;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

public class ProjectManagementService
{
  @Request(RequestType.SESSION)
  public List<SiteItem> getChildren(String sessionId, String parentid)
  {
    LinkedList<SiteItem> children = new LinkedList<SiteItem>();
    
    UasComponent uasComponent = UasComponent.get(parentid);
    
    OIterator<? extends UasComponent> i = uasComponent.getAllComponents();
    
    try
    {
      i.forEach(c -> children.add(this.convertToSiteItem(c)));
    }
    finally
    {
      i.close();
    }
    return children;
  }

  @Request(RequestType.SESSION)
  public List<SiteItem> getRoots(String sessionId)
  {
    LinkedList<SiteItem> roots = new LinkedList<SiteItem>();

    QueryFactory qf = new QueryFactory();
    SiteQuery q = new SiteQuery(qf);
    
    OIterator<? extends Site> i = q.getIterator();
    
    try
    {      
      i.forEach(s -> roots.add(this.convertToSiteItem(s)));
    }
    finally
    {
      i.close();
    }

    return roots;
  }

  @Request(RequestType.SESSION)
  /**
   * Should this method return null if the given parent has no children?
   * 
   * @param sessionId
   * @param parentId
   * @return
   */
  public SiteItem newChild(String sessionId, String parentId)
  {
    UasComponent uasComponent = UasComponent.get(parentId);
    
    UasComponent childUasComponent = uasComponent.createChild();
    
    if (childUasComponent != null)
    {
      return this.convertToSiteItem(childUasComponent);
    }
    else
    {
      return null;
    }

//    return this.toItem(UUID.randomUUID().toString(), "", false);
  }

  @Request(RequestType.SESSION)
  public SiteItem applyWithParent(String sessionId, SiteItem item, String parentId)
  {
    // TODO Do domain stuff here
//    throw new ProgrammingErrorException("Unable to create item");


    
    
    return item;
  }

  @Request(RequestType.SESSION)
  public SiteItem edit(String sessionId, String id)
  {
    // TODO Do domain stuff here

    return this.toItem(id, "Test Name", false);
  }

  @Request(RequestType.SESSION)
  public SiteItem update(String sessionId, SiteItem item)
  {
    // TODO Do domain stuff here

    return item;
  }

  @Request(RequestType.SESSION)
  public void remove(String sessionId, String id)
  {
    // TODO Do domain stuff here
//    throw new ProgrammingErrorException("Unable to delete item [" + id + "]");
  }

  private SiteItem toItem(String oid, String name, boolean hasChildren)
  {
    SiteItem item = new SiteItem();
    item.setId(oid);
    item.setName(name);
    item.setHasChildren(hasChildren);

    return item;
  }
  
  private SiteItem convertToSiteItem(UasComponent uasComponent)
  {
    SiteItem siteItem = new SiteItem();
    
    siteItem.setId(uasComponent.getOid());
    
    siteItem.setName(uasComponent.getName());
    
    OIterator<? extends UasComponent> children = uasComponent.getAllComponents();
    
    try
    {
      if (children.hasNext())
      {
        siteItem.setHasChildren(true);
      }
      else
      {
        siteItem.setHasChildren(false);
      }
    }
    finally
    {
      children.close();
    }
     
    return siteItem;
  }

}
