/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.geoplatform.uasdm.view;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.amazonaws.AmazonClientException;
import com.runwaysdk.dataaccess.ProgrammingErrorException;

import gov.geoplatform.uasdm.GenericException;
import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.graph.ODMRun;
import gov.geoplatform.uasdm.graph.Project;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.MissionIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.odm.ODMProcessConfiguration;
import gov.geoplatform.uasdm.odm.ODMProcessConfiguration.Quality;
import gov.geoplatform.uasdm.odm.ODMProcessConfiguration.RadiometricCalibration;
import gov.geoplatform.uasdm.remote.RemoteFileObject;

public class FlightMetadata
{
  public static class ProcessingRunMetadata
  {
    private String                 type;

    private Date                   startDate;

    private Date                   endDate;

    private BigDecimal             resolution;

    private Integer                matchingNeighbors;

    private Integer                minimumExtractFeatures;

    private Quality                generatedFeatureQuality;

    private RadiometricCalibration radiometricCallibration;

    private Boolean                geoLoggerUsed;

    private Quality                ptCloudQuality;

    public String getType()
    {
      return type;
    }

    public void setType(String type)
    {
      this.type = type;
    }

    public Date getStartDate()
    {
      return startDate;
    }

    public void setStartDate(Date startDate)
    {
      this.startDate = startDate;
    }

    public Date getEndDate()
    {
      return endDate;
    }

    public void setEndDate(Date endDate)
    {
      this.endDate = endDate;
    }

    public BigDecimal getResolution()
    {
      return resolution;
    }

    public void setResolution(BigDecimal resolution)
    {
      this.resolution = resolution;
    }

    public Integer getMatchingNeighbors()
    {
      return matchingNeighbors;
    }

    public void setMatchingNeighbors(Integer matchingNeighbors)
    {
      this.matchingNeighbors = matchingNeighbors;
    }

    public Integer getMinimumExtractFeatures()
    {
      return minimumExtractFeatures;
    }

    public void setMinimumExtractFeatures(Integer minimumExtractFeatures)
    {
      this.minimumExtractFeatures = minimumExtractFeatures;
    }

    public Quality getGeneratedFeatureQuality()
    {
      return generatedFeatureQuality;
    }

    public void setGeneratedFeatureQuality(Quality generatedFeatureQuality)
    {
      this.generatedFeatureQuality = generatedFeatureQuality;
    }

    public RadiometricCalibration getRadiometricCallibration()
    {
      return radiometricCallibration;
    }

    public void setRadiometricCallibration(RadiometricCalibration radiometricCallibration)
    {
      this.radiometricCallibration = radiometricCallibration;
    }

    public Boolean getGeoLoggerUsed()
    {
      return geoLoggerUsed;
    }

    public void setGeoLoggerUsed(Boolean geoLoggerUsed)
    {
      this.geoLoggerUsed = geoLoggerUsed;
    }

    public Quality getPtCloudQuality()
    {
      return ptCloudQuality;
    }

    public void setPtCloudQuality(Quality ptCloudQuality)
    {
      this.ptCloudQuality = ptCloudQuality;
    }

    public ProcessingRunMetadata populate(ODMRun run)
    {
      ODMProcessConfiguration configuration = run.getConfiguration();

      this.setType("ODM");
      this.setStartDate(run.getRunStart());
      this.setEndDate(run.getRunEnd());
      this.setResolution(configuration.getResolution());
      this.setMatchingNeighbors(configuration.getMatcherNeighbors());
      this.setMinimumExtractFeatures(configuration.getMinNumFeatures());
      this.setGeneratedFeatureQuality(configuration.getFeatureQuality());
      this.setRadiometricCallibration(configuration.getRadiometricCalibration());
      this.setGeoLoggerUsed(configuration.isIncludeGeoLocationFile());
      this.setPtCloudQuality(configuration.getPcQuality());

      return this;
    }

    public static ProcessingRunMetadata parse(Element item)
    {
      ProcessingRunMetadata metadata = new ProcessingRunMetadata();
      metadata.setType(item.getAttribute("type"));

      if (item.hasAttribute("startDate"))
      {
        try
        {
          metadata.setStartDate(Util.parseMetadata(item.getAttribute("startDate"), false));
        }
        catch (ParseException e)
        {
          GenericException exception = new GenericException(e);
          exception.setUserMessage(e.getMessage());
          throw exception;
        }
      }

      if (item.hasAttribute("endDate"))
      {
        try
        {
          metadata.setEndDate(Util.parseMetadata(item.getAttribute("endDate"), false));
        }
        catch (ParseException e)
        {
          GenericException exception = new GenericException(e);
          exception.setUserMessage(e.getMessage());
          throw exception;
        }
      }

      if (item.hasAttribute("resolution"))
      {
        metadata.setResolution(new BigDecimal(item.getAttribute("resolution")));
      }

      if (item.hasAttribute("matchingNeighbors"))
      {
        metadata.setMatchingNeighbors(Integer.valueOf(item.getAttribute("matchingNeighbors")));
      }

      if (item.hasAttribute("minimumExtractFeatures"))
      {
        metadata.setMinimumExtractFeatures(Integer.valueOf(item.getAttribute("minimumExtractFeatures")));
      }

      if (item.hasAttribute("generatedFeatureQuality"))
      {
        metadata.setGeneratedFeatureQuality(Quality.valueOf(item.getAttribute("generatedFeatureQuality")));
      }

      if (item.hasAttribute("radiometricCallibration"))
      {
        metadata.setRadiometricCallibration(RadiometricCalibration.valueOf(item.getAttribute("radiometricCallibration")));
      }

      if (item.hasAttribute("geoLoggerUsed"))
      {
        metadata.setGeoLoggerUsed(Boolean.valueOf(item.getAttribute("geoLoggerUsed")));
      }

      if (item.hasAttribute("ptCloudQuality"))
      {
        metadata.setPtCloudQuality(Quality.valueOf(item.getAttribute("ptCloudQuality")));
      }

      return metadata;
    }
  }

  public static class ArtifactMetadata
  {
    private String                type;

    private String                bands;

    private String                format;

    private String                resolution;

    private String                ptEpsg;

    private String                orthoCorrectionModel;

    private Date                  startDate;

    private Date                  endDate;

    private ProcessingRunMetadata processingRun;

    public String getType()
    {
      return type;
    }

    public void setType(String type)
    {
      this.type = type;
    }

    public String getBands()
    {
      return bands;
    }

    public void setBands(String bands)
    {
      this.bands = bands;
    }

    public String getFormat()
    {
      return format;
    }

    public void setFormat(String format)
    {
      this.format = format;
    }

    public String getResolution()
    {
      return resolution;
    }

    public void setResolution(String resolution)
    {
      this.resolution = resolution;
    }

    public ProcessingRunMetadata getProcessingRun()
    {
      return processingRun;
    }

    public void setProcessingRun(ProcessingRunMetadata processingRun)
    {
      this.processingRun = processingRun;
    }

    public String getPtEpsg()
    {
      return ptEpsg;
    }

    public void setPtEpsg(String ptEpsg)
    {
      this.ptEpsg = ptEpsg;
    }

    public String getOrthoCorrectionModel()
    {
      return orthoCorrectionModel;
    }

    public void setOrthoCorrectionModel(String orthoCorrectionModel)
    {
      this.orthoCorrectionModel = orthoCorrectionModel;
    }

    public Date getStartDate()
    {
      return startDate;
    }

    public void setStartDate(Date startDate)
    {
      this.startDate = startDate;
    }

    public Date getEndDate()
    {
      return endDate;
    }

    public void setEndDate(Date endDate)
    {
      this.endDate = endDate;
    }

    public ArtifactMetadata populate(Artifact artifact, CollectionIF collection)
    {
      this.type = artifact.getFolder();
      this.ptEpsg = artifact.getPtEpsg();
      this.orthoCorrectionModel = artifact.getOrthoCorrectionModel();
      this.startDate = collection.getCollectionDate();
      this.endDate = collection.getCollectionEndDate();

      if (this.endDate == null)
      {
        this.endDate = collection.getCollectionDate();
      }

      List<SiteObject> objects = artifact.getObjects();

      SiteObject object = objects.get(0);
      ODMRun run = ODMRun.getGeneratingRun(gov.geoplatform.uasdm.graph.Document.get(object.getId()));

      if (run != null)
      {
        this.processingRun = new ProcessingRunMetadata().populate(run);
      }

      return this;
    }

    public static ArtifactMetadata parse(Element item)
    {
      ArtifactMetadata metadata = new ArtifactMetadata();
      metadata.setType(item.getAttribute("type"));

      // Point cloud specific metadata
      if (metadata.getType().equals(ImageryComponent.PTCLOUD))
      {
        if (item.hasAttribute("ptCloudEpsgNumber"))
        {
          metadata.setPtEpsg(item.getAttribute("ptCloudEpsgNumber"));
        }

        if (item.hasAttribute("ptCloudStartDate"))
        {
          try
          {
            metadata.setStartDate(Util.parseIso8601(item.getAttribute("ptCloudStartDate"), false));
          }
          catch (ParseException e)
          {
            GenericException exception = new GenericException(e);
            exception.setUserMessage(e.getMessage());
            throw exception;
          }
        }

        if (item.hasAttribute("ptCloudEndDate"))
        {
          try
          {
            metadata.setStartDate(Util.parseIso8601(item.getAttribute("ptCloudEndDate"), false));
          }
          catch (ParseException e)
          {
            GenericException exception = new GenericException(e);
            exception.setUserMessage(e.getMessage());
            throw exception;
          }
        }
      }

      // Otho specific metadata
      if (metadata.getType().equals(ImageryComponent.ORTHO))
      {
        if (item.hasAttribute("orthoCorrectionModel"))
        {
          metadata.setOrthoCorrectionModel(item.getAttribute("orthoCorrectionModel"));
        }

        if (item.hasAttribute("orthoStartDate"))
        {
          try
          {
            metadata.setStartDate(Util.parseIso8601(item.getAttribute("orthoStartDate"), false));
          }
          catch (ParseException e)
          {
            GenericException exception = new GenericException(e);
            exception.setUserMessage(e.getMessage());
            throw exception;
          }
        }

        if (item.hasAttribute("orthoStartDate"))
        {
          try
          {
            metadata.setStartDate(Util.parseIso8601(item.getAttribute("orthoStartDate"), false));
          }
          catch (ParseException e)
          {
            GenericException exception = new GenericException(e);
            exception.setUserMessage(e.getMessage());
            throw exception;
          }
        }
      }

      NodeList list = item.getElementsByTagName("ProcessingRun");

      if (list.getLength() > 0)
      {
        Node child = list.item(0);

        metadata.setProcessingRun(ProcessingRunMetadata.parse((Element) child));
      }

      return metadata;
    }

  }

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

    private String description;

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

    public void populate(UasComponentIF component)
    {
      this.setName(component.getName());
      this.setDescription(component.getDescription());
    }

    public LocationMetadata create()
    {
      return new LocationMetadata();
    }

    public LocationMetadata parse(Element item)
    {
      LocationMetadata metadata = this.create();
      metadata.setName(item.getAttribute("name"));
      metadata.setDescription(item.getAttribute("description"));

      return metadata;
    }

  }

  public static class ProjectMetadata extends LocationMetadata
  {
    private Boolean restricted;

    private String  shortName;

    private Date    sunsetDate;

    private String  projectType;

    public String getProjectType()
    {
      return projectType;
    }

    public void setProjectType(String projectType)
    {
      this.projectType = projectType;
    }

    public Boolean getRestricted()
    {
      return restricted;
    }

    public void setRestricted(Boolean restricted)
    {
      this.restricted = restricted;
    }

    public Date getSunsetDate()
    {
      return sunsetDate;
    }

    public void setSunsetDate(Date sunsetDate)
    {
      this.sunsetDate = sunsetDate;
    }

    public String getShortName()
    {
      return shortName;
    }

    public void setShortName(String shortName)
    {
      this.shortName = shortName;
    }

    @Override
    public void populate(UasComponentIF component)
    {
      super.populate(component);

      if (component instanceof Project)
      {
        Project project = (Project) component;

        this.setShortName(project.getShortName());
        this.setSunsetDate(project.getSunsetDate());
        this.setRestricted(project.getRestricted());
        this.setProjectType(project.getProjectType());
      }
    }

    @Override
    public LocationMetadata create()
    {
      return new ProjectMetadata();
    }

    public LocationMetadata parse(Element item)
    {
      ProjectMetadata metadata = (ProjectMetadata) super.parse(item);
      metadata.setShortName(item.getAttribute("shortName"));

      if (item.hasAttribute("restricted"))
      {
        metadata.setRestricted(Boolean.valueOf(item.getAttribute("restricted")));
      }

      if (item.hasAttribute("sunsetDate"))
      {
        try
        {
          metadata.setSunsetDate(Util.parseMetadata(item.getAttribute("sunsetDate"), false));
        }
        catch (ParseException e)
        {
          GenericException exception = new GenericException(e);
          exception.setUserMessage(e.getMessage());
          throw exception;
        }
      }

      if (item.hasAttribute("projectType"))
      {
        metadata.setProjectType(item.getAttribute("projectType"));
      }

      return metadata;
    }
  }

  public static class MissionMetadata extends LocationMetadata
  {
    private String contractingOffice;

    private String vendor;

    public String getContractingOffice()
    {
      return contractingOffice;
    }

    public void setContractingOffice(String contractingOffice)
    {
      this.contractingOffice = contractingOffice;
    }

    public String getVendor()
    {
      return vendor;
    }

    public void setVendor(String vendor)
    {
      this.vendor = vendor;
    }

    @Override
    public LocationMetadata create()
    {
      return new MissionMetadata();
    }

    public LocationMetadata parse(Element item)
    {
      MissionMetadata metadata = (MissionMetadata) super.parse(item);

      if (item.hasAttribute("contractingOffice"))
      {
        metadata.setContractingOffice(item.getAttribute("contractingOffice"));
      }

      if (item.hasAttribute("vendor"))
      {
        metadata.setVendor(item.getAttribute("vendor"));
      }

      return metadata;
    }

    @Override
    public void populate(UasComponentIF component)
    {
      super.populate(component);

      if (component instanceof MissionIF)
      {
        MissionIF mission = (MissionIF) component;

        this.setContractingOffice(mission.getContractingOffice());
        this.setVendor(mission.getVendor());
      }
    }

  }

  public static class CollectionMetadata extends LocationMetadata
  {
    private BigDecimal northBound;

    private BigDecimal southBound;

    private BigDecimal eastBound;

    private BigDecimal westBound;

    private Boolean    exifIncluded;

    private Date       acquisitionDateStart;

    private Date       acquisitionDateEnd;

    private Integer    flyingHeight;

    private Integer    numberOfFlights;

    private Integer    percentEndLap;

    private Integer    percentSideLap;

    private BigDecimal areaCovered;

    private String     weatherConditions;

    public BigDecimal getNorthBound()
    {
      return northBound;
    }

    public void setNorthBound(BigDecimal northBound)
    {
      this.northBound = northBound;
    }

    public BigDecimal getSouthBound()
    {
      return southBound;
    }

    public void setSouthBound(BigDecimal southBound)
    {
      this.southBound = southBound;
    }

    public BigDecimal getEastBound()
    {
      return eastBound;
    }

    public void setEastBound(BigDecimal eastBound)
    {
      this.eastBound = eastBound;
    }

    public BigDecimal getWestBound()
    {
      return westBound;
    }

    public void setWestBound(BigDecimal westBound)
    {
      this.westBound = westBound;
    }

    public Boolean getExifIncluded()
    {
      return exifIncluded;
    }

    public void setExifIncluded(Boolean exifIncluded)
    {
      this.exifIncluded = exifIncluded;
    }

    public Date getAcquisitionDateStart()
    {
      return acquisitionDateStart;
    }

    public void setAcquisitionDateStart(Date acquisitionDateStart)
    {
      this.acquisitionDateStart = acquisitionDateStart;
    }

    public Date getAcquisitionDateEnd()
    {
      return acquisitionDateEnd;
    }

    public void setAcquisitionDateEnd(Date acquisitionDateEnd)
    {
      this.acquisitionDateEnd = acquisitionDateEnd;
    }

    public Integer getFlyingHeight()
    {
      return flyingHeight;
    }

    public void setFlyingHeight(Integer flyingHeight)
    {
      this.flyingHeight = flyingHeight;
    }

    public Integer getNumberOfFlights()
    {
      return numberOfFlights;
    }

    public void setNumberOfFlights(Integer numberOfFlights)
    {
      this.numberOfFlights = numberOfFlights;
    }

    public Integer getPercentEndLap()
    {
      return percentEndLap;
    }

    public void setPercentEndLap(Integer percentEndLap)
    {
      this.percentEndLap = percentEndLap;
    }

    public Integer getPercentSideLap()
    {
      return percentSideLap;
    }

    public void setPercentSideLap(Integer percentSideLap)
    {
      this.percentSideLap = percentSideLap;
    }

    public BigDecimal getAreaCovered()
    {
      return areaCovered;
    }

    public void setAreaCovered(BigDecimal areaCovered)
    {
      this.areaCovered = areaCovered;
    }

    public String getWeatherConditions()
    {
      return weatherConditions;
    }

    public void setWeatherConditions(String weatherConditions)
    {
      this.weatherConditions = weatherConditions;
    }

    @Override
    public void populate(UasComponentIF component)
    {
      super.populate(component);

      if (component instanceof CollectionIF)
      {
        CollectionIF collection = (CollectionIF) component;

        this.setNorthBound(collection.getNorthBound());
        this.setSouthBound(collection.getSouthBound());
        this.setEastBound(collection.getEastBound());
        this.setWestBound(collection.getWestBound());
        this.setExifIncluded(collection.getExifIncluded());
        this.setAcquisitionDateStart(collection.getAcquisitionDateStart());
        this.setAcquisitionDateEnd(collection.getAcquisitionDateEnd());
        this.setFlyingHeight(collection.getFlyingHeight());
        this.setNumberOfFlights(collection.getNumberOfFlights());
        this.setPercentEndLap(collection.getPercentEndLap());
        this.setPercentSideLap(collection.getPercentSideLap());
        this.setAreaCovered(collection.getAreaCovered());
        this.setWeatherConditions(collection.getWeatherConditions());
      }
    }

    @Override
    public LocationMetadata create()
    {
      return new CollectionMetadata();
    }

    public LocationMetadata parse(Element item)
    {
      CollectionMetadata metadata = (CollectionMetadata) super.parse(item);

      if (item.hasAttribute("northBound"))
      {
        metadata.setNorthBound(new BigDecimal(item.getAttribute("northBound")));
      }

      if (item.hasAttribute("southBound"))
      {
        metadata.setNorthBound(new BigDecimal(item.getAttribute("southBound")));
      }

      if (item.hasAttribute("eastBound"))
      {
        metadata.setNorthBound(new BigDecimal(item.getAttribute("eastBound")));
      }

      if (item.hasAttribute("westBound"))
      {
        metadata.setNorthBound(new BigDecimal(item.getAttribute("westBound")));
      }

      if (item.hasAttribute("exifIncluded"))
      {
        metadata.setExifIncluded(Boolean.valueOf(item.getAttribute("exifIncluded")));
      }

      if (item.hasAttribute("acquisitionDateStart"))
      {
        try
        {
          metadata.setAcquisitionDateStart(Util.parseMetadata(item.getAttribute("acquisitionDateStart"), false));
        }
        catch (ParseException e)
        {
          GenericException exception = new GenericException(e);
          exception.setUserMessage(e.getMessage());
          throw exception;
        }
      }

      if (item.hasAttribute("acquisitionDateEnd"))
      {
        try
        {
          metadata.setAcquisitionDateEnd(Util.parseMetadata(item.getAttribute("acquisitionDateEnd"), false));
        }
        catch (ParseException e)
        {
          GenericException exception = new GenericException(e);
          exception.setUserMessage(e.getMessage());
          throw exception;
        }
      }

      if (item.hasAttribute("flyingHeight"))
      {
        metadata.setFlyingHeight(Integer.parseInt(item.getAttribute("flyingHeight")));
      }

      if (item.hasAttribute("numberOfFlights"))
      {
        metadata.setNumberOfFlights(Integer.parseInt(item.getAttribute("numberOfFlights")));
      }

      if (item.hasAttribute("percentEndLap"))
      {
        metadata.setPercentEndLap(Integer.parseInt(item.getAttribute("percentEndLap")));
      }

      if (item.hasAttribute("percentSideLap"))
      {
        metadata.setPercentEndLap(Integer.parseInt(item.getAttribute("percentSideLap")));
      }

      if (item.hasAttribute("areaCovered"))
      {
        metadata.setAreaCovered(new BigDecimal(item.getAttribute("areaCovered")));
      }

      if (item.hasAttribute("weatherConditions"))
      {
        metadata.setWeatherConditions(item.getAttribute("weatherConditions"));
      }

      return metadata;
    }
  }

  public static class SensorMetadata
  {
    private String  name;

    private String  type;

    private String  model;

    private String  wavelength;

    private String  imageWidth;

    private String  imageHeight;

    private String  sensorWidth;

    private String  sensorHeight;

    private String  pixelSizeWidth;

    private String  pixelSizeHeight;

    private Integer focalLength;

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

    public Integer getFocalLength()
    {
      return focalLength;
    }

    public void setFocalLength(Integer focalLength)
    {
      this.focalLength = focalLength;
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

      if (item.hasAttribute("focalLength"))
      {
        metadata.setFocalLength(Integer.parseInt(item.getAttribute("focalLength")));
      }

      return metadata;
    }

  }

  private String                 name;

  private String                 email;

  private ProjectMetadata        project;

  private MissionMetadata        mission;

  private CollectionMetadata     collection;

  private PlatformMetadata       platform;

  private SensorMetadata         sensor;

  private List<ArtifactMetadata> artifacts;

  public FlightMetadata()
  {
    this.name = "";
    this.email = "";
    this.project = new ProjectMetadata();
    this.mission = new MissionMetadata();
    this.collection = new CollectionMetadata();
    this.platform = new PlatformMetadata();
    this.sensor = new SensorMetadata();
    this.artifacts = new LinkedList<>();
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

  public ProjectMetadata getProject()
  {
    return project;
  }

  public void setProject(ProjectMetadata project)
  {
    this.project = project;
  }

  public MissionMetadata getMission()
  {
    return mission;
  }

  public void setMission(MissionMetadata mission)
  {
    this.mission = mission;
  }

  public CollectionMetadata getCollection()
  {
    return collection;
  }

  public void setCollection(CollectionMetadata collection)
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

  public void addArtifact(ArtifactMetadata artifact)
  {
    this.artifacts.add(artifact);
  }

  public List<ArtifactMetadata> getArtifacts()
  {
    return artifacts;
  }

  public void parse(Document document)
  {
    this.parsePointOfContact(document);

    this.setProject(this.parseLocation(document, "Project", new ProjectMetadata()));
    this.setMission(this.parseLocation(document, "Mission", new MissionMetadata()));
    this.setCollection(this.parseLocation(document, "Collect", new CollectionMetadata()));

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

  private <T extends LocationMetadata> T parseLocation(Document document, String tagName, T metadata)
  {
    NodeList nl = document.getElementsByTagName(tagName);

    if (nl.getLength() > 0)
    {
      Element item = (Element) nl.item(0);

      metadata.parse(item);
    }

    return metadata;
  }

  public static FlightMetadata get(UasComponentIF component, String folderName, String filename)
  {
    FlightMetadata metadata = new FlightMetadata();

    String key = component.getS3location() + folderName + "/" + filename;

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

  public static FlightMetadata parse(CollectionIF collection, JSONObject json)
  {
    List<UasComponentIF> ancestors = collection.getAncestors();

    FlightMetadata metadata = new FlightMetadata();

    JSONObject pointOfContact = json.getJSONObject("pointOfContact");

    metadata.setName(pointOfContact.getString("name"));
    metadata.setEmail(pointOfContact.getString("email"));

    UasComponentIF proj = ancestors.get(1);

    metadata.getProject().populate(proj);

    UasComponentIF mission = ancestors.get(0);

    metadata.getMission().populate(mission);

    metadata.getCollection().setName(collection.getName());
    metadata.getCollection().setDescription(collection.getDescription());

    JSONObject jPlatform = json.getJSONObject("platform");

    metadata.getPlatform().setName(jPlatform.getString("otherName"));
    metadata.getPlatform().setType(jPlatform.getString("type"));
    metadata.getPlatform().setSerialNumber(jPlatform.get("serialNumber").toString());
    metadata.getPlatform().setFaaIdNumber(jPlatform.get("faaIdNumber").toString());

    JSONObject jSensor = json.getJSONObject("sensor");
    metadata.getSensor().setName(jSensor.getString("otherName"));
    metadata.getSensor().setType(jSensor.getString("type"));
    metadata.getSensor().setModel(jSensor.getString("model"));
    metadata.getSensor().setWavelength(jSensor.getJSONArray("wavelength").toString());

    Integer width = collection.getImageWidth();
    if (width != null && width != 0)
    {
      metadata.getSensor().setImageWidth(String.valueOf(width));
    }
    else
    {
      metadata.getSensor().setImageWidth("");
    }

    Integer height = collection.getImageHeight();
    if (height != null && height != 0)
    {
      metadata.getSensor().setImageHeight(String.valueOf(height));
    }
    else
    {
      metadata.getSensor().setImageHeight("");
    }

    metadata.getSensor().setSensorWidth(jSensor.get("sensorWidth").toString());
    metadata.getSensor().setSensorHeight(jSensor.get("sensorHeight").toString());
    metadata.getSensor().setPixelSizeWidth(jSensor.get("pixelSizeWidth").toString());
    metadata.getSensor().setPixelSizeHeight(jSensor.get("pixelSizeHeight").toString());

    if (jSensor.has("focalLength"))
    {
      metadata.getSensor().setFocalLength(jSensor.getInt("focalLength"));
    }

    Artifact[] artifacts = collection.getArtifactObjects();

    for (Artifact artifact : artifacts)
    {
      if (artifact.hasObjects())
      {
        metadata.addArtifact(new ArtifactMetadata().populate(artifact, collection));
      }
    }

    return metadata;
  }
}
