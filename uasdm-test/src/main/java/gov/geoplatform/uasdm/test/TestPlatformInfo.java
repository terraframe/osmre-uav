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

import gov.geoplatform.uasdm.graph.Platform;
import gov.geoplatform.uasdm.graph.PlatformManufacturer;
import gov.geoplatform.uasdm.graph.PlatformType;

public class TestPlatformInfo
{
  
  protected String name;
  
  protected String description;
  
  protected String platformTypeName;
  
  protected String manufacturerName;
  
  protected TestSensorInfo sensor;
  
  public TestPlatformInfo(String name, String description, String platformTypeName, String manufacturerName, TestSensorInfo sensor)
  {
    this.name = name;
    this.description = description;
    this.platformTypeName = platformTypeName;
    this.manufacturerName = manufacturerName;
    this.sensor = sensor;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public PlatformType getType()
  {
    return PlatformType.getByName(this.platformTypeName);
  }

  public PlatformManufacturer getManufacturer()
  {
    return PlatformManufacturer.getByName(this.manufacturerName);
  }

  public TestSensorInfo getSensor()
  {
    return sensor;
  }

  public void setSensor(TestSensorInfo sensor)
  {
    this.sensor = sensor;
  }
  
  public void populate(Platform platform)
  {
    platform.setName(this.getName());
    platform.setDescription(this.getDescription());
    platform.setPlatformType(this.getType());
    platform.setManufacturer(this.getManufacturer());
    platform.addPlatformHasSensorChild(this.getSensor().getServerObject());
  }
  
  public Platform apply()
  {
    Platform platform = new Platform();
    this.populate(platform);
    platform.apply();
    
    return platform;
  }
  
  public Platform getServerObject()
  {
    return TestDataSet.getPlatform(name);
  }
  
  public void delete()
  {
    Platform server = this.getServerObject();
    
    if (server != null)
    {
      server.delete();
    }
  }
  
}
