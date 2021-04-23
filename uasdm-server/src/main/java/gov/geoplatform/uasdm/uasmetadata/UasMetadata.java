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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.runwaysdk.dataaccess.ProgrammingErrorException;

import gov.geoplatform.uasdm.model.CollectionSubfolder;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.remote.RemoteFileObject;

@JacksonXmlRootElement
@JsonRootName(value = "uas")
public class UasMetadata
{
  public static final String S3_FILE_POSTFIX = "_uasmeta";
  
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
  
  @JsonIgnore
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
  
  @JsonIgnore
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
  
  @JsonIgnore
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
  
  @JsonIgnore
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
  
  @JsonIgnore
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
  
  @JsonIgnore
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
  
  @JsonIgnore
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
  
  @JsonIgnore
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
  
  public static String buildS3MetadataPath(gov.geoplatform.uasdm.graph.Collection collection, CollectionSubfolder folder)
  {
    return collection.getS3location() + folder.getFolderName() + "/" + folder.getFolderName() + S3_FILE_POSTFIX + ".xml";
  }

  public static UasMetadata get(gov.geoplatform.uasdm.graph.Collection collection, CollectionSubfolder folder)
  {
    String key = buildS3MetadataPath(collection, folder);

    try (RemoteFileObject object = collection.download(key))
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
