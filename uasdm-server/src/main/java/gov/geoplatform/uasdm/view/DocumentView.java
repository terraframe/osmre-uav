package gov.geoplatform.uasdm.view;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import gov.geoplatform.uasdm.model.DocumentIF;

public class DocumentView
{
  private String id;
  
  private String name;
  
  private String key;
  
  private String component; // An oid
  
  public DocumentView()
  {
    
  }
  
  public static DocumentView fromDocument(DocumentIF doc)
  {
    DocumentView view = new DocumentView();
    
    view.setId(doc.getOid());
    view.setName(doc.getName());
    view.setKey(doc.getS3location());
    view.setComponent(doc.getComponent().getOid());
    
    return view;
  }

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getKey()
  {
    return key;
  }

  public void setKey(String key)
  {
    this.key = key;
  }

  public String getComponent()
  {
    return component;
  }

  public void setComponent(String component)
  {
    this.component = component;
  }
  
  public JsonObject toJson()
  {
    GsonBuilder builder = new GsonBuilder();

    JsonObject jo = (JsonObject) builder.create().toJsonTree(this);
    
    return jo;
  }
  
  public ODMRunView parse(String json)
  {
    GsonBuilder builder = new GsonBuilder();

    return builder.create().fromJson(json, ODMRunView.class);
  }
}
