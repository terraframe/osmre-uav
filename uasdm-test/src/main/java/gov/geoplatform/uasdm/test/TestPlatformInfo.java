package gov.geoplatform.uasdm.test;

import gov.geoplatform.uasdm.graph.Platform;
import gov.geoplatform.uasdm.graph.PlatformManufacturer;
import gov.geoplatform.uasdm.graph.PlatformType;
import gov.geoplatform.uasdm.graph.UAV;

public class TestPlatformInfo
{
  
  protected String name;
  
  protected String description;
  
  protected PlatformType type;
  
  protected PlatformManufacturer manufacturer;
  
  protected TestSensorInfo sensor;
  
  public TestPlatformInfo(String name, String description, PlatformType type, PlatformManufacturer manufacturer, TestSensorInfo sensor)
  {
    this.name = name;
    this.description = description;
    this.type = type;
    this.manufacturer = manufacturer;
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
    return type;
  }

  public void setType(PlatformType type)
  {
    this.type = type;
  }

  public PlatformManufacturer getManufacturer()
  {
    return manufacturer;
  }

  public void setManufacturer(PlatformManufacturer manufacturer)
  {
    this.manufacturer = manufacturer;
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
