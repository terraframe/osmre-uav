package gov.geoplatform.uasdm.test;

import gov.geoplatform.uasdm.graph.Project;
import gov.geoplatform.uasdm.graph.UasComponent;

public class TestProjectInfo extends TestUasComponentInfo
{
  private TestSiteInfo site;

  public TestProjectInfo(String name, TestSiteInfo site)
  {
    super(name, name, name, null);

    this.site = site;
  }

  public TestSiteInfo getSite()
  {
    return site;
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
    super.apply(this.site);
  }

  /**
   * Creates a new instance of the server object type.
   */
  @Override
  public UasComponent instantiate()
  {
    return new Project();
  }
}
