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
import gov.geoplatform.uasdm.graph.Sensor;
import gov.geoplatform.uasdm.graph.UAV;

public class TestUavInfo
{

  protected TestPlatformInfo     platform;

  protected String               serialNumber;

  protected String               faaNumber;

  protected String               description;

  protected TestOrganizationInfo organization;

  public TestUavInfo(TestPlatformInfo platform, String serialNumber, String faaNumber, String description, TestOrganizationInfo organization)
  {
    this.platform = platform;
    this.serialNumber = serialNumber;
    this.faaNumber = faaNumber;
    this.description = description;
    this.organization = organization;
  }

  public TestPlatformInfo getPlatform()
  {
    return platform;
  }

  public void setPlatform(TestPlatformInfo platform)
  {
    this.platform = platform;
  }

  public String getSerialNumber()
  {
    return serialNumber;
  }

  public void setSerialNumber(String serialNumber)
  {
    this.serialNumber = serialNumber;
  }

  public String getFaaNumber()
  {
    return faaNumber;
  }

  public void setFaaNumber(String faaNumber)
  {
    this.faaNumber = faaNumber;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public TestOrganizationInfo getOrganization()
  {
    return organization;
  }

  public void setOrganization(TestOrganizationInfo organization)
  {
    this.organization = organization;
  }

  public void populate(UAV uav)
  {
    uav.setPlatform(this.getPlatform().getServerObject());
    uav.setSerialNumber(this.getSerialNumber());
    uav.setFaaNumber(this.getFaaNumber());
    uav.setDescription(this.getDescription());
    uav.setBureau(Bureau.getByName(this.getOrganization().getCode()));
    uav.setOrganization(this.getOrganization().getServerObject().getGraphOrganization());
  }

  public UAV apply()
  {
    UAV uav = new UAV();
    this.populate(uav);
    uav.apply();

    return uav;
  }

  public UAV getServerObject()
  {
    return TestDataSet.getUav(this.getSerialNumber());
  }

  public void delete()
  {
    UAV server = this.getServerObject();

    if (server != null)
    {
      server.delete();
    }
  }

}
