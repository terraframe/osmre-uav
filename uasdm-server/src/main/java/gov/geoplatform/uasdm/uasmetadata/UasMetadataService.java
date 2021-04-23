package gov.geoplatform.uasdm.uasmetadata;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.resource.CloseableFile;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.graph.Mission;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.graph.Project;
import gov.geoplatform.uasdm.graph.Site;
import gov.geoplatform.uasdm.model.CollectionSubfolder;
import gov.geoplatform.uasdm.service.SolrService;

public class UasMetadataService
{
  public static final String DATE_FORMAT = "MM/dd/yyyy";
  
  public UasMetadata fromJson(InputStream json)
  {
    ObjectMapper mapper = new ObjectMapper();
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
  
  public UasMetadata fromXml(InputStream xml)
  {
    XmlMapper mapper = new XmlMapper();
    mapper.setDateFormat(new SimpleDateFormat(DATE_FORMAT));
    
    try
    {
      return mapper.readValue(xml, UasMetadata.class);
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
  /**
   * Populates the UasMetadata object with values from the collection.
   */
  public void populate(CollectionSubfolder folder, Collection collection, UasMetadata metadata)
  {
    Mission mission = collection.getMissionHasCollectionParentMissions().get(0);
    Project project = mission.getProjectHasMissionParentProjects().get(0);
    Site site = project.getSiteHasProjectParentSites().get(0);
    
    Agency agency = metadata.getOrCreateAgency();
    
    agency.setName(site.getBureau().getDisplayLabel());
    agency.setShortName(site.getBureau().getName());
    
    gov.geoplatform.uasdm.uasmetadata.Collection mdCol = metadata.getOrCreateCollection();
    mdCol.setName(collection.getName());
    
    gov.geoplatform.uasdm.uasmetadata.Mission mdMission = metadata.getOrCreateMission();
    mdMission.setName(mission.getName());
    
    gov.geoplatform.uasdm.uasmetadata.Project mdProject = metadata.getOrCreateProject();
    mdProject.setName(project.getName());
    
    Sensor mdSensor = metadata.getOrCreateSensor();
    mdSensor.setImageWidth(collection.getImageWidth());
    mdSensor.setImageHeight(collection.getImageHeight());
    
    Upload upload = metadata.getOrCreateUpload();
    upload.setDataType(folder.getFolderName());
    
    if (folder.equals(CollectionSubfolder.ORTHO))
    {
      List<Product> products = collection.getProducts();
      
      if (products != null && products.size() > 0)
      {
        upload.setOrthoStartDate(products.get(0).getLastUpdateDate());
        upload.setOrthoEndDate(products.get(0).getLastUpdateDate());
  //      upload.setOrthoCorrectionModel("unknown");
      }
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
    try (CloseableFile temp = new CloseableFile(AppProperties.getTempDirectory(), collection.getOid() + "_" + folder.getFolderName() + "_metadata.xml", true))
    {
      try (FileOutputStream fos = new FileOutputStream(temp))
      {
        UasMetadata metadata = this.fromJson(IOUtils.toInputStream(uasMetadataJson, Charset.forName("UTF-8")));
        
        this.populate(folder, collection, metadata);
        
        this.toXml(fos, metadata);
      }
      catch (IOException e)
      {
        throw new ProgrammingErrorException(e);
      }
      
      String key = UasMetadata.buildS3MetadataPath(collection, folder);
      String fileName = FilenameUtils.getName(key);
      Util.uploadFileToS3(temp, key, null);

      collection.createDocumentIfNotExist(key, fileName);

      SolrService.updateOrCreateMetadataDocument(collection.getAncestors(), collection, key, fileName, temp);

      collection.appLock();
      collection.setMetadataUploaded(true);
      collection.apply();
    }
  }
}
