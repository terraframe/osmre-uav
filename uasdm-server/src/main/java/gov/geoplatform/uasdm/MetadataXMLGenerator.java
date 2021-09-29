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
package gov.geoplatform.uasdm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.transport.conversion.ConversionException;

import gov.geoplatform.uasdm.bus.CollectionReport;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.graph.Platform;
import gov.geoplatform.uasdm.graph.PlatformType;
import gov.geoplatform.uasdm.graph.Sensor;
import gov.geoplatform.uasdm.graph.SensorType;
import gov.geoplatform.uasdm.graph.UAV;
import gov.geoplatform.uasdm.graph.WaveLength;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.service.SolrService;
import gov.geoplatform.uasdm.view.FlightMetadata;

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

  public FlightMetadata generate(CollectionIF collection, JSONObject selection)
  {
    FlightMetadata metadata = new FlightMetadata();

    List<UasComponentIF> ancestors = collection.getAncestors();

    JSONObject pointOfContact = selection.getJSONObject("pointOfContact");

    metadata.setName(pointOfContact.getString("name"));
    metadata.setEmail(pointOfContact.getString("email"));

    UasComponentIF proj = ancestors.get(1);

    metadata.getProject().setName(proj.getName());
    metadata.getProject().setShortName(proj.getName());
    metadata.getProject().setDescription(proj.getDescription());

    UasComponentIF mission = ancestors.get(0);

    metadata.getMission().setName(mission.getName());
    metadata.getMission().setDescription(mission.getDescription());

    metadata.getCollection().setName(collection.getName());
    metadata.getCollection().setDescription(collection.getDescription());

    UAV uav = collection.getUav();
    Platform platform = uav.getPlatform();
    PlatformType platformType = platform.getPlatformType();

    metadata.getPlatform().setName(platform.getName());
    metadata.getPlatform().setType(platformType.getName());
    metadata.getPlatform().setSerialNumber(uav.getSerialNumber());
    metadata.getPlatform().setFaaIdNumber(uav.getFaaNumber());

    Sensor sensor = collection.getSensor();
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

    metadata.getSensor().setSensorWidth(sensor.getRealSensorWidth().toString());
    metadata.getSensor().setSensorHeight(sensor.getRealSensorHeight().toString());
    metadata.getSensor().setPixelSizeWidth(sensor.getPixelSizeWidth().toString());
    metadata.getSensor().setPixelSizeHeight(sensor.getPixelSizeHeight().toString());

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

    e = dom.createElement("PointOfContact");
    e.setAttribute("name", metadata.getName());
    e.setAttribute("email", metadata.getEmail());
    root.appendChild(e);

    e = dom.createElement("Project");
    e.setAttribute("name", metadata.getProject().getName());
    e.setAttribute("shortName", metadata.getProject().getShortName());
    e.setAttribute("description", metadata.getProject().getDescription());
    root.appendChild(e);

    e = dom.createElement("Mission");
    e.setAttribute("name", metadata.getMission().getName());
    e.setAttribute("description", metadata.getMission().getDescription());
    root.appendChild(e);

    e = dom.createElement("Collect");
    e.setAttribute("name", metadata.getCollection().getName());
    e.setAttribute("description", metadata.getCollection().getDescription());
    root.appendChild(e);

    String platformName = metadata.getPlatform().getName();
    e = dom.createElement("Platform");
    e.setAttribute("name", platformName);
    // e.setAttribute("class", jPlatform.getString("class"));
    e.setAttribute("type", metadata.getPlatform().getType());
    e.setAttribute("serialNumber", metadata.getPlatform().getSerialNumber());
    e.setAttribute("faaIdNumber", metadata.getPlatform().getFaaIdNumber());
    root.appendChild(e);

    e = dom.createElement("Sensor");
    e.setAttribute("name", metadata.getSensor().getName());
    e.setAttribute("type", metadata.getSensor().getType());
    e.setAttribute("model", metadata.getSensor().getModel());
    e.setAttribute("wavelength", metadata.getSensor().getWavelength());

    String width = metadata.getSensor().getImageWidth();
    if (width != null && width.length() > 0)
    {
      e.setAttribute("imageWidth", width);
    }
    else
    {
      e.setAttribute("imageWidth", "");
    }

    String height = metadata.getSensor().getImageHeight();
    if (height != null && height.length() > 0)
    {
      e.setAttribute("imageHeight", height);
    }
    else
    {
      e.setAttribute("imageHeight", "");
    }

    e.setAttribute("sensorWidth", metadata.getSensor().getSensorWidth());
    e.setAttribute("sensorWidthUnits", "mm");
    e.setAttribute("sensorHeight", metadata.getSensor().getSensorHeight());
    e.setAttribute("sensorHeightUnits", "mm");
    e.setAttribute("pixelSizeWidth", metadata.getSensor().getPixelSizeWidth());
    e.setAttribute("pixelSizeHeight", metadata.getSensor().getPixelSizeHeight());
    root.appendChild(e);

    e = dom.createElement("Upload");
    e.setAttribute("dataType", "raw");
    root.appendChild(e);

    return dom;
  }

  @Transaction
  public void generateAndUpload(CollectionIF collection, JSONObject selection)
  {
    FlightMetadata metadata = this.generate(collection, selection);

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

      collection.createDocumentIfNotExist(key, fileName);

      SolrService.updateOrCreateMetadataDocument(collection.getAncestors(), collection, key, fileName, temp);

      collection.appLock();
      collection.setMetadataUploaded(true);
      collection.apply();

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
