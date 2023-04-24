package gov.geoplatform.uasdm.view;

import java.util.Date;

import org.json.JSONException;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.runwaysdk.mvc.JsonConfiguration;
import com.runwaysdk.mvc.JsonSerializable;
import com.runwaysdk.mvc.RestSerializer;

import gov.geoplatform.uasdm.graph.ODMRun;
import gov.geoplatform.uasdm.odm.ODMProcessConfiguration;

public class ODMRunView implements JsonSerializable
{
  private String output;
  
  private ODMProcessConfiguration config;
  
  private DocumentView report;
  
  private Date runStart;
  
  private Date runEnd;
  
//  private Document[] generatedArtifacts;
  
  public ODMRunView()
  {
    
  }
  
  public static ODMRunView fromODMRun(ODMRun run)
  {
    ODMRunView view = new ODMRunView();
    
    view.setOutput(run.getOutput());
    
    view.setConfig(run.getConfiguration());
    
    view.setReport(DocumentView.fromDocument(run.getReport()));
    
    view.setRunStart(run.getRunStart());
    
    view.setRunEnd(run.getRunEnd());
    
    return view;
  }

  public String getOutput()
  {
    return output;
  }

  public void setOutput(String output)
  {
    this.output = output;
  }

  public ODMProcessConfiguration getConfig()
  {
    return config;
  }

  public void setConfig(ODMProcessConfiguration config)
  {
    this.config = config;
  }

  public DocumentView getReport()
  {
    return report;
  }

  public void setReport(DocumentView report)
  {
    this.report = report;
  }

//  public SiteObject[] getGeneratedArtifacts()
//  {
//    return generatedArtifacts;
//  }
//
//  public void setGeneratedArtifacts(SiteObject[] generatedArtifacts)
//  {
//    this.generatedArtifacts = generatedArtifacts;
//  }
  
  public Date getRunStart()
  {
    return runStart;
  }

  public void setRunStart(Date runStart)
  {
    this.runStart = runStart;
  }

  public Date getRunEnd()
  {
    return runEnd;
  }

  public void setRunEnd(Date runEnd)
  {
    this.runEnd = runEnd;
  }
  
  public JsonObject toJson()
  {
    GsonBuilder builder = new GsonBuilder();
    builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    JsonObject jo = (JsonObject) builder.create().toJsonTree(this);
    
    return jo;
  }
  
  public ODMRunView parse(String json)
  {
    GsonBuilder builder = new GsonBuilder();
    builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    return builder.create().fromJson(json, ODMRunView.class);
  }

  @Override
  public Object serialize(RestSerializer serializer, JsonConfiguration configuration) throws JSONException
  {
    return this.toJson().toString();
  }
}
