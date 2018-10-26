package gov.osmre.uasdm.service;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.osmre.uasdm.view.SiteItem;

public class ProjectManagementService
{
  @Request(RequestType.SESSION)
  public List<SiteItem> getChildren(String sessionId, String id)
  {
    // TODO Do domain stuff here 
    
    LinkedList<SiteItem> children = new LinkedList<SiteItem>();
    children.add(this.toItem("2", "Child-1.1", false));
    children.add(this.toItem("3", "Child-1.2", false));
    

    return children;
  }

  @Request(RequestType.SESSION)
  public List<SiteItem> getRoots(String sessionId)
  {
    // TODO Do domain stuff here 

    LinkedList<SiteItem> roots = new LinkedList<SiteItem>();
    roots.add(this.toItem("1", "Root", true));

    return roots;
  }

  @Request(RequestType.SESSION)
  public SiteItem newChild(String sessionId, String parentId)
  {
    // TODO Do domain stuff here 

    return this.toItem(UUID.randomUUID().toString(), "", false);
  }

  @Request(RequestType.SESSION)
  public SiteItem applyWithParent(String sessionId, SiteItem item, String parentId)
  {
    // TODO Do domain stuff here 

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

  }

  private SiteItem toItem(String oid, String name, boolean hasChildren)
  {
    SiteItem item = new SiteItem();
    item.setId(oid);
    item.setName(name);
    item.setHasChildren(hasChildren);

    return item;
  }

}
