package gov.geoplatform.uasdm.bus;

public class Project extends ProjectBase
{
  private static final long serialVersionUID = 935245787;
  
  public Project()
  {
    super();
  }
  
  public Mission createChild()
  {
    return new Mission();
  }
  
  public ComponentHasComponent addComponent(gov.geoplatform.uasdm.bus.UasComponent uasComponent)
  {
    return this.addMissions((Mission)uasComponent);
  }
}
