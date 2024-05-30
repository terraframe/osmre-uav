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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import gov.geoplatform.uasdm.graph.Sensor;
import gov.geoplatform.uasdm.graph.SensorType;
import gov.geoplatform.uasdm.graph.WaveLength;

public class TestSensorInfo
{
  
  protected String name;
  
  protected String description;
  
  protected String model;
  
  protected String sensorTypeName;
  
  protected Integer pixelSizeWidth;
  
  protected Integer pixelSizeHeight;
  
  protected Integer sensorWidth;
  
  protected Integer sensorHeight;
  
  protected List<String> wavelengthNames;
  
  public TestSensorInfo(String name, String description, String model, String sensorTypeName, Integer pixelSizeWidth, Integer pixelSizeHeight, Integer sensorWidth, Integer sensorHeight, String... wavelengthNames)
  {
    this.name = name;
    this.description = description;
    this.model = model;
    this.sensorTypeName = sensorTypeName;
    this.pixelSizeWidth = pixelSizeWidth;
    this.pixelSizeHeight = pixelSizeHeight;
    this.sensorWidth = sensorWidth;
    this.sensorHeight = sensorHeight;
    
    this.wavelengthNames = Arrays.asList(wavelengthNames);
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

  public String getModel()
  {
    return model;
  }

  public void setModel(String model)
  {
    this.model = model;
  }

  public SensorType getType()
  {
    return SensorType.getByName(this.sensorTypeName);
  }

  public Integer getPixelSizeWidth()
  {
    return pixelSizeWidth;
  }

  public void setPixelSizeWidth(Integer pixelSizeWidth)
  {
    this.pixelSizeWidth = pixelSizeWidth;
  }

  public Integer getPixelSizeHeight()
  {
    return pixelSizeHeight;
  }

  public void setPixelSizeHeight(Integer pixelSizeHeight)
  {
    this.pixelSizeHeight = pixelSizeHeight;
  }

  public Integer getSensorWidth()
  {
    return sensorWidth;
  }

  public void setSensorWidth(Integer sensorWidth)
  {
    this.sensorWidth = sensorWidth;
  }

  public Integer getSensorHeight()
  {
    return sensorHeight;
  }

  public void setSensorHeight(Integer sensorHeight)
  {
    this.sensorHeight = sensorHeight;
  }

  public List<WaveLength> getWavelengths()
  {
    return this.wavelengthNames.stream().map(name -> WaveLength.getByName(name)).collect(Collectors.toList());
  }

  public void populate(Sensor sensor)
  {
    sensor.setName(this.getName());
    sensor.setDescription(this.getDescription());
    sensor.setModel(this.getModel());
    sensor.setSensorType(this.getType());
    sensor.setPixelSizeWidth(this.getPixelSizeWidth());
    sensor.setPixelSizeHeight(this.getPixelSizeHeight());
    sensor.setSensorWidth(this.getSensorWidth());
    sensor.setSensorHeight(this.getSensorHeight());
    
    for (WaveLength wl : this.getWavelengths())
    {
      sensor.addSensorHasWaveLengthChild(wl);
    }
  }
  
  public Sensor apply()
  {
    Sensor sensor = new Sensor();
    this.populate(sensor);
    sensor.apply();
    
    return sensor;
  }
  
  public Sensor getServerObject()
  {
    return TestDataSet.getSensor(name);
  }
  
  public void delete()
  {
    Sensor sensor = this.getServerObject();
    
    if (sensor != null)
    {
      sensor.delete();
    }
  }
  
}
