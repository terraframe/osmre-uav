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
package gov.geoplatform.uasdm.uasmetadata;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import com.amazonaws.AmazonClientException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.runwaysdk.dataaccess.ProgrammingErrorException;

import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.remote.RemoteFileObject;

@JacksonXmlRootElement
@JsonRootName(value = "uas")
public class UasMetadata
{
  public static void main(String[] args) throws Exception
  {
    final String path = "/home/rich/dev/projects/uasdm/data/metadata/IDM folder structure and EROS requirements/new/dem_uasmeta.xml";
    
    XmlMapper mapper = new XmlMapper();
    mapper.setDateFormat(new SimpleDateFormat(UasMetadataService.DATE_FORMAT));
    
    UasMetadata metadata = mapper.readValue(new FileInputStream(path), UasMetadata.class);
    
    StringWriter stringWriter = new StringWriter();
    XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newFactory();
    XMLStreamWriter sw = xmlOutputFactory.createXMLStreamWriter(stringWriter);
    
    sw.writeStartDocument();
    
    mapper.writeValue(sw, metadata);
    
    sw.writeEndDocument();
    
    System.out.println(stringWriter.toString());
  }
  
  @JsonProperty("Agency")
  private Agency agency;
  
  @JsonProperty("Collect")
  private Collection collection;
  
  @JsonProperty("Mission")
  private Mission mission;
  
  @JsonProperty("Platform")
  private Platform platform;
  
  @JsonProperty("PointOfContact")
  private PointOfContact pointOfContact;
  
  @JsonProperty("Project")
  private Project project;
  
  @JsonProperty("Sensor")
  private Sensor sensor;
  
  @JsonProperty("Upload")
  private Upload upload;
  
  public UasMetadata()
  {
  }

  public Agency getAgency()
  {
    return agency;
  }
  
  public Agency getOrCreateAgency()
  {
    if (agency == null)
    {
      this.agency = new Agency();
    }
    
    return this.agency;
  }

  public void setAgency(Agency agency)
  {
    this.agency = agency;
  }

  public Collection getCollection()
  {
    return collection;
  }
  
  public Collection getOrCreateCollection()
  {
    if (this.collection == null)
    {
      this.collection = new Collection();
    }
    
    return this.collection;
  }

  public void setCollection(Collection collection)
  {
    this.collection = collection;
  }

  public Mission getMission()
  {
    return mission;
  }
  
  public Mission getOrCreateMission()
  {
    if (this.mission == null)
    {
      this.mission = new Mission();
    }
    
    return this.mission;
  }

  public void setMission(Mission mission)
  {
    this.mission = mission;
  }

  public Platform getPlatform()
  {
    return platform;
  }
  
  public Platform getOrCreatePlatform()
  {
    if (this.platform == null)
    {
      this.platform = new Platform();
    }
    
    return platform;
  }

  public void setPlatform(Platform platform)
  {
    this.platform = platform;
  }

  public PointOfContact getPointOfContact()
  {
    return pointOfContact;
  }
  
  public PointOfContact getOrCreatePointOfContact()
  {
    if (pointOfContact == null)
    {
      pointOfContact = new PointOfContact();
    }
    
    return pointOfContact;
  }

  public void setPointOfContact(PointOfContact pointOfContact)
  {
    this.pointOfContact = pointOfContact;
  }

  public Project getProject()
  {
    return project;
  }
  
  public Project getOrCreateProject()
  {
    if (project == null)
    {
      project = new Project();
    }
    
    return project;
  }

  public void setProject(Project project)
  {
    this.project = project;
  }

  public Sensor getSensor()
  {
    return sensor;
  }
  
  public Sensor getOrCreateSensor()
  {
    if (sensor == null)
    {
      sensor = new Sensor();
    }
    
    return sensor;
  }

  public void setSensor(Sensor sensor)
  {
    this.sensor = sensor;
  }

  public Upload getUpload()
  {
    return upload;
  }
  
  public Upload getOrCreateUpload()
  {
    if (upload == null)
    {
      upload = new Upload();
    }
    
    return upload;
  }

  public void setUpload(Upload upload)
  {
    this.upload = upload;
  }

  public static UasMetadata get(UasComponentIF component, String folderName, String filename)
  {
    // TODO : This path needs to change
    String key = component.getS3location() + folderName + "/" + component.getName() + filename;

    try (RemoteFileObject object = component.download(key))
    {
      if (object != null)
      {
        try (InputStream istream = object.getObjectContent())
        {
          ObjectMapper mapper = new XmlMapper();
          mapper.setDateFormat(new SimpleDateFormat(UasMetadataService.DATE_FORMAT));
          
          return mapper.readValue(istream, UasMetadata.class);
        }
      }
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
    catch (AmazonClientException e)
    {
      // Metadata doesn't exist
    }
    
    return null;
  }
}
