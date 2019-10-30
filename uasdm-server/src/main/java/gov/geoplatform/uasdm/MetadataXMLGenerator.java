package gov.geoplatform.uasdm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

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
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.transport.conversion.ConversionException;

import gov.geoplatform.uasdm.bus.Platform;
import gov.geoplatform.uasdm.bus.Sensor;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.ComponentFactory;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.service.SolrService;

public class MetadataXMLGenerator
{
  public static final String FILENAME = "_uasmetadata.xml";

  private Document           dom;

  private JSONObject         json;

  private CollectionIF       collection;

  public MetadataXMLGenerator(String json)
  {
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
    parseJson(json);
  }

  private void parseJson(String sJson)
  {
    json = new JSONObject(sJson);

    this.collection = ComponentFactory.getCollection(json.getString("collectionId"));
  }

  public void generate(OutputStream out)
  {
    List<UasComponentIF> ancestors = collection.getAncestors();

    Element e = null;

    Element root = dom.createElement("rootEl");
    dom.appendChild(root);

//    JSONObject agency = json.getJSONObject("agency");
    e = dom.createElement("Agency");
    e.setAttribute("name", "Department of Interior");
    e.setAttribute("shortName", "");
    e.setAttribute("fieldCenter", "");
    root.appendChild(e);

    JSONObject pointOfContact = json.getJSONObject("pointOfContact");
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

    JSONObject jPlatform = json.getJSONObject("platform");
    String platformId = jPlatform.getString("name");
    Platform platform = Platform.get(platformId);

    String platformName = platform.isOther() ? jPlatform.getString("otherName") : platform.getDisplayLabel();
    e = dom.createElement("Platform");
    e.setAttribute("name", platformName);
    e.setAttribute("class", jPlatform.getString("class"));
    e.setAttribute("type", jPlatform.getString("type"));
    e.setAttribute("serialNumber", jPlatform.getString("serialNumber"));
    e.setAttribute("faaIdNumber", jPlatform.getString("faaIdNumber"));
    root.appendChild(e);

    JSONObject jSensor = json.getJSONObject("sensor");
    String sensorId = jSensor.getString("name");
    Sensor sensor = Sensor.get(sensorId);

    String sensorName = sensor.isOther() ? jSensor.getString("otherName") : sensor.getDisplayLabel();

    e = dom.createElement("Sensor");
    e.setAttribute("name", sensorName);
    e.setAttribute("type", jSensor.getString("type"));
    e.setAttribute("model", jSensor.getString("model"));
    e.setAttribute("wavelength", jSensor.getJSONArray("wavelength").toString());
//    e.setAttribute("imageWidth", sensor.getString("imageWidth"));
//    e.setAttribute("imageHeight", sensor.getString("imageHeight"));
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
    e.setAttribute("sensorWidth", jSensor.getString("sensorWidth"));
    e.setAttribute("sensorHeight", jSensor.getString("sensorHeight"));
    e.setAttribute("pixelSizeWidth", jSensor.getString("pixelSizeWidth"));
    e.setAttribute("pixelSizeHeight", jSensor.getString("pixelSizeHeight"));
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
//      tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "roles.dtd");
      tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

      // send DOM to file
      tr.transform(new DOMSource(dom), new StreamResult(out));

    }
    catch (TransformerException te)
    {
      System.out.println(te.getMessage());
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

      String fileName = this.collection.getName() + FILENAME;
      String key = this.collection.getS3location() + Collection.RAW + "/" + this.collection.getName() + FILENAME;
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
