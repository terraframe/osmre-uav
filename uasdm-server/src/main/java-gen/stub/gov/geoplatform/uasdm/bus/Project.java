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
  
}
