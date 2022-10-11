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

import java.util.LinkedList;
import java.util.List;

import gov.geoplatform.uasdm.graph.Sensor;
import gov.geoplatform.uasdm.graph.SensorType;
import gov.geoplatform.uasdm.graph.WaveLength;

public class TestSensorInfo
{
  
  protected String name;
  
  protected String description;
  
  protected String model;
  
  protected SensorType type;
  
  protected Integer pixelSizeWidth;
  
  protected Integer pixelSizeHeight;
  
  protected Integer sensorWidth;
  
  protected Integer sensorHeight;
  
  protected List<WaveLength> wavelengths;
  
  public TestSensorInfo(String name, String description, String model, SensorType type, Integer pixelSizeWidth, Integer pixelSizeHeight, Integer sensorWidth, Integer sensorHeight, WaveLength... wavelengths)
  {
    this.name = name;
    this.description = description;
    this.model = model;
    this.type = type;
    this.pixelSizeWidth = pixelSizeWidth;
    this.pixelSizeHeight = pixelSizeHeight;
    this.sensorWidth = sensorWidth;
    this.sensorHeight = sensorHeight;
    
    this.wavelengths = new LinkedList<WaveLength>();
    for (WaveLength wavelength : wavelengths)
    {
      this.wavelengths.add(wavelength);
    }
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
    return type;
  }

  public void setType(SensorType type)
  {
    this.type = type;
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
    return wavelengths;
  }

  public void setWavelengths(List<WaveLength> wavelengths)
  {
    this.wavelengths = wavelengths;
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
    
    for (WaveLength wl : this.wavelengths)
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
