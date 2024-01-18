/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.geoplatform.uasdm.test;

import gov.geoplatform.uasdm.graph.Bureau;
import gov.geoplatform.uasdm.graph.Site;
import gov.geoplatform.uasdm.graph.UasComponent;

public class TestSiteInfo extends TestUasComponentInfo
{
  protected TestOrganizationInfo organization;

  protected String               otherBureauTxt;

  public TestSiteInfo(TestOrganizationInfo organization, String name, String geoPoint)
  {
    super(name, name, name, geoPoint);
    this.organization = organization;
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

    ( (Site) component ).setBureau(Bureau.getByName(this.getOrganization().getCode()).getBureau());
    ( (Site) component ).setOrganization(this.getOrganization().getServerObject().getGraphOrganization());

    component.setGeoPoint(this.getGeoPoint());
  }

  public TestOrganizationInfo getOrganization()
  {
    return organization;
  }

  public void setOrganization(TestOrganizationInfo organization)
  {
    this.organization = organization;
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
