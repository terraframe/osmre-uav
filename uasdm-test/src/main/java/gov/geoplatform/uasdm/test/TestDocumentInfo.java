package gov.geoplatform.uasdm.test;

import gov.geoplatform.uasdm.graph.Document;
import gov.geoplatform.uasdm.graph.UasComponent;

public class TestDocumentInfo
{
  protected TestCollectionInfo component;

  protected String key;

  protected String fileName;

  protected String description;

  protected String tool;

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

  public void populate(Document document)
  {
  }

  public Document apply()
  {
    UasComponent collection = this.component.getServerObject();

    return Document.createIfNotExist(collection, collection.getS3location() + this.key, this.fileName, this.description, this.tool);
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
