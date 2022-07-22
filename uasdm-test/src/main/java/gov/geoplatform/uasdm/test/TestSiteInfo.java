package gov.geoplatform.uasdm.test;

import gov.geoplatform.uasdm.graph.Bureau;
import gov.geoplatform.uasdm.graph.Site;
import gov.geoplatform.uasdm.graph.UasComponent;

public class TestSiteInfo extends TestUasComponentInfo
{
  protected Bureau bureau;
  
  protected String otherBureauTxt;
  
  public TestSiteInfo(Bureau bureau, String name, String geoPoint)
  {
    super(name, name, name, geoPoint);
    this.bureau = bureau;
  }
  
  @Override
  public UasComponent instantiate()
  {
    return new Site();
  }
  
  /**
   * Populates the component with the values contained within this wrapper
   */
  @Override
  public void populate(UasComponent component)
  {
    super.populate(component);
    
    ((Site)component).setBureau(this.getBureau().getBureau());
    
    component.setGeoPoint(this.getGeoPoint());
  }

  public Bureau getBureau()
  {
    return bureau;
  }

  public void setBureau(Bureau bureau)
  {
    this.bureau = bureau;
  }

  public String getOtherBureauTxt()
  {
    return otherBureauTxt;
  }

  public void setOtherBureauTxt(String otherBureauTxt)
  {
    this.otherBureauTxt = otherBureauTxt;
  }
}
