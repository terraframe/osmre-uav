package gov.geoplatform.uasdm.bus;

public class Mission extends MissionBase
{
  private static final long serialVersionUID = -112103870;
  
  public Mission()
  {
    super();
  }
  
  public Collection createChild()
  {
    return new Collection();
  }
  
  public ComponentHasComponent addComponent(gov.geoplatform.uasdm.bus.UasComponent uasComponent)
  {
    return this.addCollections((Collection)uasComponent);
  }
  
}
