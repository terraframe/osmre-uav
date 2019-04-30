package gov.geoplatform.uasdm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
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
import com.runwaysdk.session.Request;
import com.runwaysdk.transport.conversion.ConversionException;

import gov.geoplatform.uasdm.bus.Collection;
import gov.geoplatform.uasdm.bus.UasComponent;

public class MetadataXMLGenerator
{
  
  private Document dom;
  
  private JSONObject json;
  
  private Collection collection;
  
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
    
    this.collection = Collection.get(json.getString("collectionId")); // TODO
  }
  
  public void generate(OutputStream out)
  {
    List<UasComponent> ancestors = collection.getAncestors();
    
    Element e = null;
    
    Element root = dom.createElement("rootEl");
    dom.appendChild(root);
    
    e = dom.createElement("Agency");
    e.setAttribute("name", "Department of Interior");
    e.setAttribute("shortName", "TBD"); // TODO
    e.setAttribute("fieldCenter", "TBD"); // TODO
    root.appendChild(e);
    
    e = dom.createElement("PointOfContact");
    e.setAttribute("name", ""); // TODO
    e.setAttribute("email", ""); // TODO
    root.appendChild(e);
    
    UasComponent proj = ancestors.get(1); // TODO
    e = dom.createElement("Project");
    e.setAttribute("name", proj.getName());
    e.setAttribute("shortName", proj.getName());
    e.setAttribute("description", proj.getDescription());
    root.appendChild(e);
    
    UasComponent mission = ancestors.get(0); // TODO
    e = dom.createElement("Mission");
    e.setAttribute("name", mission.getName());
    e.setAttribute("description", mission.getDescription());
    root.appendChild(e);
    
    e = dom.createElement("Collect");
    e.setAttribute("name", collection.getName());
    e.setAttribute("description", collection.getDescription());
    root.appendChild(e);
    
    // TODO
    e = dom.createElement("Platform");
    e.setAttribute("name", "");
    e.setAttribute("class", "");
    e.setAttribute("type", "");
    e.setAttribute("serialNumber", "");
    e.setAttribute("faaIdNumber", "");
    root.appendChild(e);
    
    // TODO
    e = dom.createElement("Sensor");
    e.setAttribute("name", "");
    e.setAttribute("type", "");
    e.setAttribute("model", "");
    e.setAttribute("wavelength", "");
    e.setAttribute("imageWidth", "");
    e.setAttribute("imageHeight", "");
    e.setAttribute("sensorWidth", "");
    e.setAttribute("sensorHeight", "");
    e.setAttribute("pixelSizeWidth", "");
    e.setAttribute("pixelSizeHeight", "");
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

    } catch (TransformerException te) {
        System.out.println(te.getMessage());
    }
  }
  
  public void generateAndUpload()
  {
    File temp = null;
    try
    {
      temp = File.createTempFile("metadata", "xml");
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
    finally
    {
      if (temp != null)
      {
        FileUtils.deleteQuietly(temp);
      }
    }
    
    try (FileOutputStream fos = new FileOutputStream(temp))
    {
      this.generate(fos);
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
    
    String key = this.collection.getS3location() + Collection.RAW + "/" + this.collection.getName() + "_uasmetadata.xml";
    Util.uploadFileToS3(temp, key, null);
  }
  
}
