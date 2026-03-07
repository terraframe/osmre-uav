package gov.geoplatform.uasdm.view;

import java.util.List;

public class ComponentImageSetView
{
  private String           componentId;

  private List<ImageSetView> sets;

  private String           componentType;

  public String getComponentId()
  {
    return componentId;
  }

  public void setComponentId(String componentId)
  {
    this.componentId = componentId;
  }

  public List<ImageSetView> getSets()
  {
    return sets;
  }

  public void setSets(List<ImageSetView> sets)
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
