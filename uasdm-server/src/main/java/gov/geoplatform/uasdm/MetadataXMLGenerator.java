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
package gov.geoplatform.uasdm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.transport.conversion.ConversionException;

import gov.geoplatform.uasdm.bus.CollectionReport;
import gov.geoplatform.uasdm.bus.MissingMetadataMessage;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.graph.Platform;
import gov.geoplatform.uasdm.graph.PlatformType;
import gov.geoplatform.uasdm.graph.Sensor;
import gov.geoplatform.uasdm.graph.SensorType;
import gov.geoplatform.uasdm.graph.UAV;
import gov.geoplatform.uasdm.graph.WaveLength;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.service.IndexService;
import gov.geoplatform.uasdm.view.Artifact;
import gov.geoplatform.uasdm.view.FlightMetadata;
import gov.geoplatform.uasdm.view.FlightMetadata.ArtifactMetadata;
import gov.geoplatform.uasdm.view.FlightMetadata.CollectionMetadata;
import gov.geoplatform.uasdm.view.FlightMetadata.MissionMetadata;
import gov.geoplatform.uasdm.view.FlightMetadata.ProcessingRunMetadata;
import gov.geoplatform.uasdm.view.FlightMetadata.ProjectMetadata;
import gov.geoplatform.uasdm.view.FlightMetadata.SensorMetadata;

public class MetadataXMLGenerator
{
  private static final Logger    logger   = LoggerFactory.getLogger(MetadataXMLGenerator.class);

  public static final String     FILENAME = "_uasmetadata.xml";

  private DocumentBuilderFactory factory;

  private DocumentBuilder        builder;

  public MetadataXMLGenerator()
  {
    this.factory = DocumentBuilderFactory.newInstance();
    this.builder = null;

    try
    {
      this.builder = this.factory.newDocumentBuilder();
    }
    catch (ParserConfigurationException e)
    {
      throw new ConversionException(e);
    }

  }

  public FlightMetadata generate(CollectionIF collection)
  {
    FlightMetadata metadata = new FlightMetadata();

    List<UasComponentIF> ancestors = collection.getAncestors();

    metadata.setName(collection.getPocName());
    metadata.setEmail(collection.getPocEmail());

    UasComponentIF proj = ancestors.get(1);

    metadata.getProject().populate(proj);

    UasComponentIF mission = ancestors.get(0);

    metadata.getMission().populate(mission);

    metadata.getCollection().populate(collection);

    Artifact[] artifacts = collection.getArtifactObjects();

    for (Artifact artifact : artifacts)
    {
      if (artifact.hasObjects())
      {
        metadata.addArtifact(new ArtifactMetadata().populate(artifact));
      }
    }

    UAV uav = collection.getUav();
    Platform platform = uav.getPlatform();
    PlatformType platformType = platform.getPlatformType();

    metadata.getPlatform().setName(platform.getName());
    metadata.getPlatform().setType(platformType.getName());
    metadata.getPlatform().setSerialNumber(uav.getSerialNumber());
    metadata.getPlatform().setFaaIdNumber(uav.getFaaNumber());

    Sensor sensor = collection.getSensor();

    if (sensor != null)
    {

      SensorType sensorType = sensor.getSensorType();

      List<WaveLength> wavelengths = sensor.getSensorHasWaveLengthChildWaveLengths();
      JSONArray array = wavelengths.stream().map(w -> w.getName()).collect(Collector.of(JSONArray::new, JSONArray::put, JSONArray::put));

      metadata.getSensor().setName(sensor.getName());
      metadata.getSensor().setType(sensorType.getName());
      metadata.getSensor().setModel(sensor.getModel());
      metadata.getSensor().setWavelength(array.toString());

      Integer width = collection.getImageWidth();

      if (width != null && width != 0)
      {
        metadata.getSensor().setImageWidth(String.valueOf(collection.getImageWidth()));
      }

      Integer height = collection.getImageHeight();

      if (height != null && height != 0)
      {
        metadata.getSensor().setImageHeight(String.valueOf(collection.getImageHeight()));
      }

      if (sensor.getRealSensorWidth() != null)
      {
        metadata.getSensor().setSensorWidth(sensor.getRealSensorWidth().toString());
      }

      if (sensor.getRealSensorHeight() != null)
      {
        metadata.getSensor().setSensorHeight(sensor.getRealSensorHeight().toString());
      }

      if (sensor.getRealPixelSizeWidth() != null)
      {
        metadata.getSensor().setPixelSizeWidth(sensor.getRealPixelSizeWidth().toString());
      }

      if (sensor.getRealPixelSizeHeight() != null)
      {
        metadata.getSensor().setPixelSizeHeight(sensor.getRealPixelSizeHeight().toString());
      }

      metadata.getSensor().setFocalLength(sensor.getFocalLength());
    }

    return metadata;
  }

  public Document generate(CollectionIF collection, FlightMetadata metadata)
  {
    Document dom = this.builder.newDocument();
    dom.setStrictErrorChecking(false);
    dom.setXmlStandalone(true);

    Element e = null;

    Element root = dom.createElement("rootEl");
    dom.appendChild(root);

    // JSONObject agency = json.getJSONObject("agency");
    e = dom.createElement("Agency");
    e.setAttribute("name", "Department of Interior");
    e.setAttribute("shortName", "");
    e.setAttribute("fieldCenter", "");
    root.appendChild(e);

    e = this.createPointOfContactElement(metadata, dom);
    root.appendChild(e);

    e = this.createProjectElement(metadata, dom);

    root.appendChild(e);

    e = this.createMissionElement(metadata, dom);

    root.appendChild(e);

    e = this.createCollectionElement(metadata, dom);

    root.appendChild(e);

    e = this.createPlatformElement(metadata, dom);
    root.appendChild(e);

    e = this.createSensorElement(metadata, dom);
    root.appendChild(e);

    e = this.createArtifactsElement(metadata, dom);
    root.appendChild(e);

    e = dom.createElement("Upload");
    e.setAttribute("dataType", "raw");
    root.appendChild(e);

    return dom;
  }

  private Element createSensorElement(FlightMetadata metadata, Document dom)
  {
    SensorMetadata sensor = metadata.getSensor();

    Element e = dom.createElement("Sensor");
    e.setAttribute("name", sensor.getName());
    e.setAttribute("type", sensor.getType());
    e.setAttribute("model", sensor.getModel());
    e.setAttribute("wavelength", sensor.getWavelength());

    String width = sensor.getImageWidth();
    if (width != null && width.length() > 0)
    {
      e.setAttribute("imageWidth", width);
    }
    else
    {
      e.setAttribute("imageWidth", "");
    }

    String height = sensor.getImageHeight();
    if (height != null && height.length() > 0)
    {
      e.setAttribute("imageHeight", height);
    }
    else
    {
      e.setAttribute("imageHeight", "");
    }

    e.setAttribute("sensorWidth", sensor.getSensorWidth());
    e.setAttribute("sensorWidthUnits", "mm");
    e.setAttribute("sensorHeight", sensor.getSensorHeight());
    e.setAttribute("sensorHeightUnits", "mm");
    e.setAttribute("pixelSizeWidth", sensor.getPixelSizeWidth());
    e.setAttribute("pixelSizeHeight", sensor.getPixelSizeHeight());

    if (sensor.getFocalLength() != null)
    {
      e.setAttribute("rawFocalLength", sensor.getFocalLength().toString());
    }

    return e;
  }

  private Element createArtifactsElement(FlightMetadata metadata, Document dom)
  {
    Element element = dom.createElement("Artifacts");

    metadata.getArtifacts().stream().forEach(artifact -> {

      Element e = this.createArtifactElement(artifact, dom);

      element.appendChild(e);
    });

    return element;
  }

  private Element createArtifactElement(ArtifactMetadata metadata, Document dom)
  {
    Element element = dom.createElement("Artifact");
    element.setAttribute("type", metadata.getType());

    if (metadata.getFormat() != null)
    {
      element.setAttribute("format", metadata.getFormat());
    }

    if (metadata.getResolution() != null)
    {
      element.setAttribute("resolution", metadata.getResolution());
    }

    if (metadata.getBands() != null)
    {
      element.setAttribute("bands", metadata.getBands());
    }

    if (metadata.getProcessingRun() != null)
    {
      Element e = this.createProcessingRunElement(metadata.getProcessingRun(), dom);

      element.appendChild(e);

    }

    return element;
  }

  private Element createProcessingRunElement(ProcessingRunMetadata metadata, Document dom)
  {
    Element e = dom.createElement("ProcessingRun");

    if (metadata.getType() != null)
    {
      e.setAttribute("type", metadata.getType());
    }

    if (metadata.getStartDate() != null)
    {
      e.setAttribute("endDate", Util.formatMetadata(metadata.getEndDate(), false));
    }

    if (metadata.getEndDate() != null)
    {
      e.setAttribute("endDate", Util.formatMetadata(metadata.getEndDate(), false));
    }

    if (metadata.getGeneratedFeatureQuality() != null)
    {
      e.setAttribute("generatedFeatureQuality", metadata.getGeneratedFeatureQuality().name());
    }

    if (metadata.getResolution() != null)
    {
      e.setAttribute("resolution", metadata.getResolution().setScale(5, RoundingMode.HALF_UP).toPlainString());
    }

    if (metadata.getMatchingNeighbors() != null)
    {
      e.setAttribute("matchingNeighbors", metadata.getMatchingNeighbors().toString());
    }

    if (metadata.getMinimumExtractFeatures() != null)
    {
      e.setAttribute("minimumExtractFeatures", metadata.getMinimumExtractFeatures().toString());
    }

    if (metadata.getRadiometricCallibration() != null)
    {
      e.setAttribute("radiometricCalibration", metadata.getRadiometricCallibration().toString());
    }

    if (metadata.getGeoLoggerUsed() != null)
    {
      e.setAttribute("geoLoggerUsed", metadata.getGeoLoggerUsed().toString());
    }

    if (metadata.getPtCloudQuality() != null)
    {
      e.setAttribute("ptCloudQuality", metadata.getPtCloudQuality().name());
    }

    return e;
  }

  private Element createPlatformElement(FlightMetadata metadata, Document dom)
  {
    Element e = dom.createElement("Platform");
    String platformName = metadata.getPlatform().getName();
    e.setAttribute("name", platformName);
    // e.setAttribute("class", jPlatform.getString("class"));
    e.setAttribute("type", metadata.getPlatform().getType());
    e.setAttribute("serialNumber", metadata.getPlatform().getSerialNumber());
    e.setAttribute("faaIdNumber", metadata.getPlatform().getFaaIdNumber());
    return e;
  }

  private Element createPointOfContactElement(FlightMetadata metadata, Document dom)
  {
    Element e = dom.createElement("PointOfContact");
    e.setAttribute("name", metadata.getName());
    e.setAttribute("email", metadata.getEmail());
    return e;
  }

  private Element createCollectionElement(FlightMetadata metadata, Document dom)
  {
    CollectionMetadata collection = metadata.getCollection();

    Element e = dom.createElement("Collect");
    e.setAttribute("name", collection.getName());
    e.setAttribute("description", collection.getDescription());

    if (collection.getExifIncluded() != null)
    {
      e.setAttribute("exifIncluded", collection.getExifIncluded().toString());
    }

    if (collection.getNorthBound() != null)
    {
      e.setAttribute("northBound", collection.getNorthBound().setScale(5, RoundingMode.HALF_UP).toPlainString());
    }

    if (collection.getSouthBound() != null)
    {
      e.setAttribute("southBound", collection.getSouthBound().setScale(5, RoundingMode.HALF_UP).toPlainString());
    }

    if (collection.getEastBound() != null)
    {
      e.setAttribute("eastBound", collection.getEastBound().setScale(5, RoundingMode.HALF_UP).toPlainString());
    }

    if (collection.getWestBound() != null)
    {
      e.setAttribute("westBound", collection.getWestBound().setScale(5, RoundingMode.HALF_UP).toPlainString());
    }

    if (collection.getAcquisitionDateStart() != null)
    {
      e.setAttribute("acquisitionDateStart", Util.formatMetadata(collection.getAcquisitionDateStart(), false));
    }

    if (collection.getAcquisitionDateEnd() != null)
    {
      e.setAttribute("acquisitionDateEnd", Util.formatMetadata(collection.getAcquisitionDateEnd(), false));
    }

    if (collection.getFlyingHeight() != null)
    {
      e.setAttribute("flyingHeight", collection.getFlyingHeight().toString());
    }

    if (collection.getNumberOfFlights() != null)
    {
      e.setAttribute("numberOfFlights", collection.getNumberOfFlights().toString());
    }

    if (collection.getPercentEndLap() != null)
    {
      e.setAttribute("percentEndLap", collection.getPercentEndLap().toString());
    }

    if (collection.getPercentSideLap() != null)
    {
      e.setAttribute("percentSideLap", collection.getPercentSideLap().toString());
    }

    if (collection.getAreaCovered() != null)
    {
      e.setAttribute("areaCovered", collection.getAreaCovered().setScale(5, RoundingMode.HALF_UP).toPlainString());
    }

    if (collection.getWeatherConditions() != null)
    {
      e.setAttribute("weatherConditions", collection.getWeatherConditions().toString());
    }

    return e;
  }

  private Element createProjectElement(FlightMetadata metadata, Document dom)
  {
    ProjectMetadata project = metadata.getProject();

    Element e = dom.createElement("Project");
    e.setAttribute("name", project.getName());
    e.setAttribute("shortName", project.getShortName());
    e.setAttribute("description", project.getDescription());

    if (project.getRestricted() != null)
    {
      e.setAttribute("restricted", project.getRestricted().toString());
    }

    if (project.getSunsetDate() != null)
    {
      e.setAttribute("sunsetDate", Util.formatMetadata(project.getSunsetDate(), false));
    }

    if (project.getProjectType() != null)
    {
      e.setAttribute("projectType", project.getProjectType());
    }

    return e;
  }

  private Element createMissionElement(FlightMetadata metadata, Document dom)
  {
    MissionMetadata mission = metadata.getMission();

    Element e = dom.createElement("Mission");
    e.setAttribute("name", mission.getName());
    e.setAttribute("description", mission.getDescription());

    if (mission.getContractingOffice() != null)
    {
      e.setAttribute("contractingOffice", mission.getContractingOffice());
    }

    if (mission.getVendor() != null)
    {
      e.setAttribute("vendor", mission.getVendor());
    }
    return e;
  }

  @Transaction
  public void generateAndUpload(CollectionIF collection)
  {
    FlightMetadata metadata = this.generate(collection);

    this.generateAndUpload(collection, metadata);
  }

  @Transaction
  public void generateAndUpload(CollectionIF collection, FlightMetadata metadata)
  {
    Document document = generate(collection, metadata);

    this.upload(collection, document);
  }

  private void upload(CollectionIF collection, Document document) throws TransformerFactoryConfigurationError
  {
    File temp = null;

    try
    {

      temp = createTempFile(document);

      String fileName = collection.getFolderName() + FILENAME;
      String key = collection.getS3location() + Collection.RAW + "/" + collection.getFolderName() + FILENAME;
      Util.uploadFileToS3(temp, key, null);

      collection.createDocumentIfNotExist(key, fileName, null, null);

      IndexService.updateOrCreateMetadataDocument(collection.getAncestors(), collection, key, fileName, temp);

      if (!collection.getMetadataUploaded())
      {
        collection.appLock();
        collection.setMetadataUploaded(true);
        collection.apply();
      }

      // Remove any messages
      MissingMetadataMessage.remove(collection);

      CollectionReport.updateIncludeSize(collection);
    }
    finally
    {
      if (temp != null)
      {
        FileUtils.deleteQuietly(temp);
      }
    }
  }

  private File createTempFile(Document document) throws TransformerFactoryConfigurationError
  {
    File temp = new File(AppProperties.getTempDirectory(), "metadata.xml");

    try (FileOutputStream fos = new FileOutputStream(temp))
    {
      try
      {
        Transformer tr = TransformerFactory.newInstance().newTransformer();
        tr.setOutputProperty(OutputKeys.INDENT, "yes");
        tr.setOutputProperty(OutputKeys.METHOD, "xml");
        tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        // tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "roles.dtd");
        tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        // send DOM to file
        tr.transform(new DOMSource(document), new StreamResult(fos));

      }
      catch (TransformerException te)
      {
        logger.error("Unexpected error while generating IDM metadata.", te);
        throw new ProgrammingErrorException(te);
      }
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
    return temp;
  }

}
