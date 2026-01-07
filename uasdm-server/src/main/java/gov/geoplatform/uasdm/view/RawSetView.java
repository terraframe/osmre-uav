package gov.geoplatform.uasdm.view;

import java.util.List;

public class RawSetView
{
  private String             id;

  private String             name;

  private List<SiteItem>     components;

  private List<DocumentView> documents;

  private boolean            published;

  private boolean            locked;

  private String             boundingBox;

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public List<SiteItem> getComponents()
  {
    return components;
  }

  public void setComponents(List<SiteItem> components)
  {
    this.components = components;
  }

  public boolean isPublished()
  {
    return published;
  }

  public void setPublished(boolean published)
  {
    this.published = published;
  }

  public boolean isLocked()
  {
    return locked;
  }

  public void setLocked(boolean locked)
  {
    this.locked = locked;
  }

  public String getBoundingBox()
  {
    return boundingBox;
  }

  public void setBoundingBox(String boundingBox)
  {
    this.boundingBox = boundingBox;
  }

  public List<DocumentView> getDocuments()
  {
    return documents;
  }

  public void setDocuments(List<DocumentView> documents)
  {
    this.documents = documents;
  }
}
