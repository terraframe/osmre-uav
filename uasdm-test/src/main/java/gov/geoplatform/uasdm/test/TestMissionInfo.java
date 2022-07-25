package gov.geoplatform.uasdm.test;

import gov.geoplatform.uasdm.graph.Mission;
import gov.geoplatform.uasdm.graph.UasComponent;

public class TestMissionInfo extends TestUasComponentInfo
{
  public TestMissionInfo(String name)
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
    return new Mission();
  }
}
