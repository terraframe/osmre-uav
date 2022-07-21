package gov.geoplatform.uasdm.test;

import gov.geoplatform.uasdm.graph.Site;
import gov.geoplatform.uasdm.graph.UasComponent;

public class TestProjectInfo extends TestUasComponentInfo
{
  public TestProjectInfo(String name)
  {
    super(name, name, name, null);
  }
  
  /**
   * Populates the component with the values contained within this wrapper
   */
  @Override
  public void populate(UasComponent component)
  {
    super.populate(component);
  }
  
  /**
   * Creates a new instance of the server object type.
   */
  @Override
  public UasComponent instantiate()
  {
    return new Site();
  }
}
