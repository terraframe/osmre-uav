package gov.geoplatform.uasdm.uasmetadata;

public class Sensor
{
  private String name;

  private String type;

  private String model;

  private String wavelength;

  private Integer imageWidth;

  private Integer imageHeight;

  private Integer sensorWidth;

  private Integer sensorHeight;

  private Integer pixelSizeWidth;

  private Integer pixelSizeHeight;

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String getModel()
  {
    return model;
  }

  public void setModel(String model)
  {
    this.model = model;
  }

  public String getWavelength()
  {
    return wavelength;
  }

  public void setWavelength(String wavelength)
  {
    this.wavelength = wavelength;
  }

  public Integer getImageWidth()
  {
    return imageWidth;
  }

  public void setImageWidth(Integer imageWidth)
  {
    this.imageWidth = imageWidth;
  }

  public Integer getImageHeight()
  {
    return imageHeight;
  }

  public void setImageHeight(Integer imageHeight)
  {
    this.imageHeight = imageHeight;
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
}