package gov.geoplatform.uasdm.view;

import java.util.List;

public class ComponentRawSetView
{
  private String           componentId;

  private List<RawSetView> sets;

  private String           componentType;

  public String getComponentId()
  {
    return componentId;
  }

  public void setComponentId(String componentId)
  {
    this.componentId = componentId;
  }

  public List<RawSetView> getSets()
  {
    return sets;
  }

  public void setSets(List<RawSetView> sets)
  {
    this.sets = sets;
  }

  public String getComponentType()
  {
    return componentType;
  }

  public void setComponentType(String componentType)
  {
    this.componentType = componentType;
  }

}
