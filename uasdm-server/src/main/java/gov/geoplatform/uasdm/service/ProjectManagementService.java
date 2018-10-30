package gov.geoplatform.uasdm.service;

import gov.geoplatform.uasdm.bus.Site;
import gov.geoplatform.uasdm.bus.SiteQuery;
import gov.geoplatform.uasdm.bus.UasComponent;
import gov.geoplatform.uasdm.view.SiteItem;
import gov.geoplatform.uasdm.view.Converter;

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
      i.forEach(c -> children.add(Converter.toSiteItem(c)));
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
      i.forEach(s -> roots.add(Converter.toSiteItem(s)));
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
      return Converter.toSiteItem(childUasComponent);
    }
    else
    {
      return null;
    }
  }

  /**
   * Returns null if the given parent type has no child type.
   * 
   * @param parent
   * @param siteItem
   * @return
   */
  @Request(RequestType.SESSION)
  public SiteItem applyWithParent(String sessionId, SiteItem siteItem, String parentId)
  {
    UasComponent parent = UasComponent.get(parentId);
    
    UasComponent child = Converter.toNewUasComponent(parent, siteItem);
    
    if (child != null)
    {
//      child.setS3location(child.buildS3Key(parent));
      
      child.applyWithParent(parent);
      
//      parent.addComponent(child).apply();
      
      return Converter.toSiteItem(child);
    }
    else
    {
      return null;
    }
    // TODO Do domain stuff here
//    throw new ProgrammingErrorException("Unable to create item");
  }

  @Request(RequestType.SESSION)
  public SiteItem edit(String sessionId, String id)
  {
    UasComponent uasComponent = UasComponent.get(id);
    
    return Converter.toSiteItem(uasComponent);
  }

  @Request(RequestType.SESSION)
  public SiteItem update(String sessionId, SiteItem siteItem)
  {
    UasComponent uasComponent = UasComponent.get(siteItem.getId());

    uasComponent.lock();
    
    uasComponent = Converter.toExistingUasComponent(siteItem);
    
    uasComponent.apply();

    uasComponent.unlock();
    
    SiteItem updatedSiteItem = Converter.toSiteItem(uasComponent);
    
    return updatedSiteItem;
  }

  @Request(RequestType.SESSION)
  public void remove(String sessionId, String id)
  {
    UasComponent uasComponent = UasComponent.get(id);
    
    uasComponent.delete();
  }

}
