package gov.geoplatform.uasdm.view;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.amazonaws.AmazonClientException;
import com.runwaysdk.dataaccess.ProgrammingErrorException;

import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.remote.RemoteFileObject;

public class FlightMetadata
{
  public static class PlatformMetadata
  {
    private String name;

    private String platformClass;

    private String type;

    private String serialNumber;

    private String faaIdNumber;

    public String getName()
    {
      return name;
    }

    public void setName(String name)
    {
      this.name = name;
    }

    public String getPlatformClass()
    {
      return platformClass;
    }

    public void setPlatformClass(String platformClass)
    {
      this.platformClass = platformClass;
    }

    public String getType()
    {
      return type;
    }

    public void setType(String type)
    {
      this.type = type;
    }

    public String getSerialNumber()
    {
      return serialNumber;
    }

    public void setSerialNumber(String serialNumber)
    {
      this.serialNumber = serialNumber;
    }

    public String getFaaIdNumber()
    {
      return faaIdNumber;
    }

    public void setFaaIdNumber(String faaIdNumber)
    {
      this.faaIdNumber = faaIdNumber;
    }

    public static PlatformMetadata parse(Element item)
    {
      PlatformMetadata metadata = new PlatformMetadata();
      metadata.setName(item.getAttribute("name"));
      metadata.setPlatformClass(item.getAttribute("class"));
      metadata.setType(item.getAttribute("type"));
      metadata.setSerialNumber(item.getAttribute("serialNumber"));
      metadata.setFaaIdNumber(item.getAttribute("faaIdNumber"));

      return metadata;
    }
  }

  public static class LocationMetadata
  {
    private String name;

    private String shortName;

    private String description;

    public String getName()
    {
      return name;
    }

    public void setName(String name)
    {
      this.name = name;
    }

    public String getShortName()
    {
      return shortName;
    }

    public void setShortName(String shortName)
    {
      this.shortName = shortName;
    }

    public String getDescription()
    {
      return description;
    }

    public void setDescription(String description)
    {
      this.description = description;
    }

    public static LocationMetadata parse(Element item)
    {
      LocationMetadata metadata = new LocationMetadata();
      metadata.setName(item.getAttribute("name"));
      metadata.setShortName(item.getAttribute("shortName"));
      metadata.setDescription(item.getAttribute("description"));

      return metadata;
    }
  }

  public static class SensorMetadata
  {
    private String name;

    private String type;

    private String model;

    private String wavelength;

    private String imageWidth;

    private String imageHeight;

    private String sensorWidth;

    private String sensorHeight;

    private String pixelSizeWidth;

    private String pixelSizeHeight;

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

    public String getImageWidth()
    {
      return imageWidth;
    }

    public void setImageWidth(String imageWidth)
    {
      this.imageWidth = imageWidth;
    }

    public String getImageHeight()
    {
      return imageHeight;
    }

    public void setImageHeight(String imageHeight)
    {
      this.imageHeight = imageHeight;
    }

    public String getSensorWidth()
    {
      return sensorWidth;
    }

    public void setSensorWidth(String sensorWidth)
    {
      this.sensorWidth = sensorWidth;
    }

    public String getSensorHeight()
    {
      return sensorHeight;
    }

    public void setSensorHeight(String sensorHeight)
    {
      this.sensorHeight = sensorHeight;
    }

    public String getPixelSizeWidth()
    {
      return pixelSizeWidth;
    }

    public void setPixelSizeWidth(String pixelSizeWidth)
    {
      this.pixelSizeWidth = pixelSizeWidth;
    }

    public String getPixelSizeHeight()
    {
      return pixelSizeHeight;
    }

    public void setPixelSizeHeight(String pixelSizeHeight)
    {
      this.pixelSizeHeight = pixelSizeHeight;
    }

    public static SensorMetadata parse(Element item)
    {
      SensorMetadata metadata = new SensorMetadata();
      metadata.setName(item.getAttribute("name"));
      metadata.setModel(item.getAttribute("model"));
      metadata.setType(item.getAttribute("type"));
      metadata.setWavelength(item.getAttribute("wavelength"));
      metadata.setImageWidth(item.getAttribute("imageWidth"));
      metadata.setImageHeight(item.getAttribute("imageHeight"));
      metadata.setSensorWidth(item.getAttribute("sensorWidth"));
      metadata.setSensorHeight(item.getAttribute("sensorHeight"));
      metadata.setPixelSizeWidth(item.getAttribute("pixelSizeWidth"));
      metadata.setPixelSizeHeight(item.getAttribute("pixelSizeHeight"));

      return metadata;
    }

  }

  private String           name;

  private String           email;

  private LocationMetadata project;

  private LocationMetadata mission;

  private LocationMetadata collection;

  private PlatformMetadata platform;

  private SensorMetadata   sensor;

  public FlightMetadata()
  {
    this.name = "";
    this.email = "";
    this.project = new LocationMetadata();
    this.mission = new LocationMetadata();
    this.collection = new LocationMetadata();
    this.platform = new PlatformMetadata();
    this.sensor = new SensorMetadata();
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getEmail()
  {
    return email;
  }

  public void setEmail(String email)
  {
    this.email = email;
  }

  public LocationMetadata getProject()
  {
    return project;
  }

  public void setProject(LocationMetadata project)
  {
    this.project = project;
  }

  public LocationMetadata getMission()
  {
    return mission;
  }

  public void setMission(LocationMetadata mission)
  {
    this.mission = mission;
  }

  public LocationMetadata getCollection()
  {
    return collection;
  }

  public void setCollection(LocationMetadata collection)
  {
    this.collection = collection;
  }

  public PlatformMetadata getPlatform()
  {
    return platform;
  }

  public void setPlatform(PlatformMetadata platform)
  {
    this.platform = platform;
  }

  public SensorMetadata getSensor()
  {
    return sensor;
  }

  public void setSensor(SensorMetadata sensor)
  {
    this.sensor = sensor;
  }

  public void parse(Document document)
  {
    this.parsePointOfContact(document);

    this.setProject(this.parseLocation(document, "Project"));
    this.setMission(this.parseLocation(document, "Mission"));
    this.setCollection(this.parseLocation(document, "Collection"));

    this.parsePlatform(document);
    this.parseSensor(document);
  }

  private void parseSensor(Document document)
  {
    NodeList nl = document.getElementsByTagName("Sensor");

    if (nl.getLength() > 0)
    {
      Element item = (Element) nl.item(0);

      this.setSensor(SensorMetadata.parse(item));
    }
  }

  private void parsePlatform(Document document)
  {
    NodeList nl = document.getElementsByTagName("Platform");

    if (nl.getLength() > 0)
    {
      Element item = (Element) nl.item(0);

      this.setPlatform(PlatformMetadata.parse(item));
    }
  }

  private void parsePointOfContact(Document document)
  {
    NodeList nl = document.getElementsByTagName("PointOfContact");

    if (nl.getLength() > 0)
    {
      Element item = (Element) nl.item(0);

      this.setName(item.getAttribute("name"));
      this.setEmail(item.getAttribute("email"));
    }
  }

  private LocationMetadata parseLocation(Document document, String tagName)
  {
    NodeList nl = document.getElementsByTagName(tagName);

    if (nl.getLength() > 0)
    {
      Element item = (Element) nl.item(0);

      return LocationMetadata.parse(item);
    }

    return null;
  }

  public static FlightMetadata get(UasComponentIF component, String folderName, String filename)
  {
    FlightMetadata metadata = new FlightMetadata();

    String key = component.getS3location() + folderName + "/" + component.getName() + filename;

    try (RemoteFileObject object = component.download(key))
    {
      if (object != null)
      {
        try (InputStream istream = object.getObjectContent())
        {
          DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
          Document document = builder.parse(istream);

          metadata.parse(document);
        }
      }
    }
    catch (IOException | ParserConfigurationException | SAXException e)
    {
      throw new ProgrammingErrorException(e);
    }
    catch (AmazonClientException e)
    {
      // Metadata doesn't exist
    }

    return metadata;
  }
}
