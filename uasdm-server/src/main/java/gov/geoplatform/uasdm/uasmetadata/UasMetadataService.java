package gov.geoplatform.uasdm.uasmetadata;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.resource.CloseableFile;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.model.CollectionSubfolder;
import gov.geoplatform.uasdm.service.SolrService;

public class UasMetadataService
{
  public static final String DATE_FORMAT = "MM/dd/yyyy";
  
  public static final String POSTFIX = "_uasmeta";
  
  public UasMetadata fromJson(InputStream json)
  {
    XmlMapper mapper = new XmlMapper();
    mapper.setDateFormat(new SimpleDateFormat(DATE_FORMAT));
    
    try
    {
      return mapper.readValue(json, UasMetadata.class);
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }
  
  public void toXml(OutputStream xml, UasMetadata metadata)
  {
    try
    {
      XmlMapper mapper = new XmlMapper();
      mapper.setDateFormat(new SimpleDateFormat(DATE_FORMAT));
      
      OutputStreamWriter stringWriter = new OutputStreamWriter(xml);
      XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newFactory();
      XMLStreamWriter sw = xmlOutputFactory.createXMLStreamWriter(stringWriter);
      
      sw.writeStartDocument();
      
      mapper.writeValue(sw, metadata);
      
      sw.writeEndDocument();
    }
    catch (XMLStreamException | IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }
  
  @Transaction
  public void saveMetadataFormSubmission(Collection collection, String uasMetadataJson)
  {
    generateAndUpload(CollectionSubfolder.RAW, collection, uasMetadataJson);
    generateAndUpload(CollectionSubfolder.PTCLOUD, collection, uasMetadataJson);
    generateAndUpload(CollectionSubfolder.ORTHO, collection, uasMetadataJson);
    generateAndUpload(CollectionSubfolder.INACCESSIBLE_SUPPORT, collection, uasMetadataJson);
    generateAndUpload(CollectionSubfolder.ACCESSIBLE_SUPPORT, collection, uasMetadataJson);
    generateAndUpload(CollectionSubfolder.DEM, collection, uasMetadataJson);
  }

  private void generateAndUpload(CollectionSubfolder folder, Collection collection, String uasMetadataJson)
  {
    try (CloseableFile temp = new CloseableFile(AppProperties.getTempDirectory(), collection.getOid() + "_" + folder.getFolderName() + POSTFIX + ".xml", true))
    {
      try (FileOutputStream fos = new FileOutputStream(temp))
      {
        UasMetadata metadata = this.fromJson(IOUtils.toInputStream(uasMetadataJson, Charset.forName("UTF-8")));
        
        this.toXml(fos, metadata);
      }
      catch (IOException e)
      {
        throw new ProgrammingErrorException(e);
      }
      
      String fileName = folder.getFolderName() + POSTFIX + ".xml";
      String key = collection.getS3location() + folder.getFolderName() + "/" + fileName;
      Util.uploadFileToS3(temp, key, null);

      collection.createDocumentIfNotExist(key, fileName);

      SolrService.updateOrCreateMetadataDocument(collection.getAncestors(), collection, key, fileName, temp);

      collection.appLock();
      collection.setMetadataUploaded(true);
      collection.apply();
    }
  }
}
