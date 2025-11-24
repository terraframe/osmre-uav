package gov.geoplatform.uasdm.controller.body;

public class EntityWithParentBody extends EntityBody
{
  private String parentId;

  public String getParentId()
  {
    return parentId;
  }

  public void setParentId(String parentId)
  {
    this.parentId = parentId;
  }

}
