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
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
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

public class MetadataXMLGenerator
{
  private static final Logger logger   = LoggerFactory.getLogger(MetadataXMLGenerator.class);

  public static final String  FILENAME = "_uasmetadata.xml";

  private Document            dom;

  private JSONObject          selection;

  private CollectionIF        collection;

  public MetadataXMLGenerator(CollectionIF collection, JSONObject selection)
  {
    this.collection = collection;
    this.selection = selection;

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = null;

    try
    {
      builder = factory.newDocumentBuilder();
    }
    catch (ParserConfigurationException e)
    {
      throw new ConversionException(e);
    }

    this.dom = builder.newDocument();
    this.dom.setStrictErrorChecking(false);
    this.dom.setXmlStandalone(true);
  }

  public void generate(OutputStream out)
  {
    List<UasComponentIF> ancestors = collection.getAncestors();

    Element e = null;

    Element root = dom.createElement("rootEl");
    dom.appendChild(root);

    // JSONObject agency = json.getJSONObject("agency");
    e = dom.createElement("Agency");
    e.setAttribute("name", "Department of Interior");
    e.setAttribute("shortName", "");
    e.setAttribute("fieldCenter", "");
    root.appendChild(e);

    JSONObject pointOfContact = selection.getJSONObject("pointOfContact");
    e = dom.createElement("PointOfContact");
    e.setAttribute("name", pointOfContact.getString("name"));
    e.setAttribute("email", pointOfContact.getString("email"));
    root.appendChild(e);

    UasComponentIF proj = ancestors.get(1);
    e = dom.createElement("Project");
    e.setAttribute("name", proj.getName());
    e.setAttribute("shortName", proj.getName());
    e.setAttribute("description", proj.getDescription());
    root.appendChild(e);

    UasComponentIF mission = ancestors.get(0);
    e = dom.createElement("Mission");
    e.setAttribute("name", mission.getName());
    e.setAttribute("description", mission.getDescription());
    root.appendChild(e);

    e = dom.createElement("Collect");
    e.setAttribute("name", collection.getName());
    e.setAttribute("description", collection.getDescription());
    root.appendChild(e);
    
    UAV uav = this.collection.getUav();
    Platform platform = uav.getPlatform();
    PlatformType platformType = platform.getPlatformType();

    String platformName = platform.getName();
    e = dom.createElement("Platform");
    e.setAttribute("name", platformName);
//    e.setAttribute("class", jPlatform.getString("class"));
    e.setAttribute("type", platformType.getLabel());
    e.setAttribute("serialNumber", uav.getSerialNumber());
    e.setAttribute("faaIdNumber", uav.getFaaNumber());
    root.appendChild(e);
    
    Sensor sensor = this.collection.getSensor();
    SensorType sensorType = sensor.getSensorType();
    
    List<WaveLength> wavelengths = sensor.getSensorHasWaveLengthChildWaveLengths();
    JSONArray array = wavelengths.stream().map(w -> w.getLabel()).collect(Collector.of(JSONArray::new, JSONArray::put, JSONArray::put));

    String sensorName = sensor.getName();

    e = dom.createElement("Sensor");
    e.setAttribute("name", sensorName);
    e.setAttribute("type", sensorType.getLabel());
//    e.setAttribute("model", jSensor.getString("model"));
    e.setAttribute("wavelength", array.toString());
    // e.setAttribute("imageWidth", sensor.getString("imageWidth"));
    // e.setAttribute("imageHeight", sensor.getString("imageHeight"));
    
    // TODO Does the new workflow make sense with this
    Integer width = collection.getImageWidth();
    if (width != null && width != 0)
    {
      e.setAttribute("imageWidth", String.valueOf(collection.getImageWidth()));
    }
    else
    {
      e.setAttribute("imageWidth", "");
    }
    Integer height = collection.getImageHeight();
    if (height != null && height != 0)
    {
      e.setAttribute("imageHeight", String.valueOf(collection.getImageHeight()));
    }
    else
    {
      e.setAttribute("imageHeight", "");
    }
    
    e.setAttribute("sensorWidth", sensor.getSensorWidth().toString());
    e.setAttribute("sensorWidthUnits", "mm");
    e.setAttribute("sensorHeight", sensor.getSensorHeight().toString());
    e.setAttribute("sensorHeightUnits", "mm");
    e.setAttribute("pixelSizeWidth", sensor.getPixelSizeWidth().toString());
    e.setAttribute("pixelSizeHeight", sensor.getPixelSizeHeight().toString());
    root.appendChild(e);

    e = dom.createElement("Upload");
    e.setAttribute("dataType", "raw");
    root.appendChild(e);

    try
    {
      Transformer tr = TransformerFactory.newInstance().newTransformer();
      tr.setOutputProperty(OutputKeys.INDENT, "yes");
      tr.setOutputProperty(OutputKeys.METHOD, "xml");
      tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      // tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "roles.dtd");
      tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

      // send DOM to file
      tr.transform(new DOMSource(dom), new StreamResult(out));

    }
    catch (TransformerException te)
    {
      logger.error("Unexpected error while generating IDM metadata.", te);
      throw new ProgrammingErrorException(te);
    }
  }

  @Transaction
  public void generateAndUpload()
  {
    File temp = null;
    try
    {
      temp = new File(AppProperties.getTempDirectory(), "metadata.xml");

      try (FileOutputStream fos = new FileOutputStream(temp))
      {
        this.generate(fos);
      }
      catch (IOException e)
      {
        throw new ProgrammingErrorException(e);
      }

      String fileName = this.collection.getFolderName() + FILENAME;
      String key = this.collection.getS3location() + Collection.RAW + "/" + this.collection.getFolderName() + FILENAME;
      Util.uploadFileToS3(temp, key, null);

      this.collection.createDocumentIfNotExist(key, fileName);

      SolrService.updateOrCreateMetadataDocument(this.collection.getAncestors(), this.collection, key, fileName, temp);

      this.collection.appLock();
      this.collection.setMetadataUploaded(true);
      this.collection.apply();
    }
    finally
    {
      if (temp != null)
      {
        FileUtils.deleteQuietly(temp);
      }
    }
  }

}
