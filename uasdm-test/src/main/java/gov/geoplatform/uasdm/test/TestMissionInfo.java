package gov.geoplatform.uasdm.test;

import gov.geoplatform.uasdm.graph.Mission;
import gov.geoplatform.uasdm.graph.UasComponent;

public class TestMissionInfo extends TestUasComponentInfo
{
  private TestProjectInfo project;

  public TestMissionInfo(String name, TestProjectInfo project)
  {
    super(name, name, name, null);

    this.project = project;
  }

  public TestProjectInfo getProject()
  {
    return project;
  }

  /**
   * Populates the component with the values contained within this wrapper
   */
  @Override
  public void populate(UasComponent component)
  {
    super.populate(component);
  }

  @Override
  public void apply()
  {
    super.apply(this.project);
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
