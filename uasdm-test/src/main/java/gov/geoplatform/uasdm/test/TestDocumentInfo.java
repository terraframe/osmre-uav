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
package gov.geoplatform.uasdm.test;

import gov.geoplatform.uasdm.graph.Document;
import gov.geoplatform.uasdm.graph.UasComponent;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.DocumentIF.Metadata;

public class TestDocumentInfo
{
  protected TestCollectionInfo component;

  protected String             key;

  protected String             fileName;

  protected String             description;

  protected String             tool;

  protected Integer            ptEpsg;

  public TestDocumentInfo(TestCollectionInfo component, String key, String fileName)
  {
    this(component, key, fileName, "", "");
  }

  public TestDocumentInfo(TestCollectionInfo component, String key, String fileName, String description, String tool)
  {
    this.component = component;
    this.key = key;
    this.fileName = fileName;
    this.description = description;
    this.tool = tool;
  }

  public TestCollectionInfo getComponent()
  {
    return component;
  }

  public void setComponent(TestCollectionInfo component)
  {
    this.component = component;
  }

  public String getKey()
  {
    return key;
  }

  public void setKey(String key)
  {
    this.key = key;
  }

  public String getFileName()
  {
    return fileName;
  }

  public void setFileName(String fileName)
  {
    this.fileName = fileName;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getTool()
  {
    return tool;
  }

  public void setTool(String tool)
  {
    this.tool = tool;
  }
  
  public Integer getPtEpsg()
  {
    return ptEpsg;
  }
  
  public void setPtEpsg(Integer ptEpsg)
  {
    this.ptEpsg = ptEpsg;
  }

  public void populate(Document document)
  {
  }

  public Document apply()
  {
    UasComponent collection = this.component.getServerObject();

    Metadata metadata = DocumentIF.Metadata.build(this.description, this.tool, this.ptEpsg, null, null, 0L);

    return Document.createIfNotExist(collection, collection.getS3location() + this.key, this.fileName, metadata);
  }

  public Document getServerObject()
  {
    UasComponent collection = this.component.getServerObject();
    String key = collection.getS3location() + this.key;

    return Document.find(key);
  }

  public void delete()
  {
    Document server = this.getServerObject();

    if (server != null)
    {
      server.delete();
    }
  }

}
