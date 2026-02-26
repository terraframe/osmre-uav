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
package gov.geoplatform.uasdm.view;

import java.util.Date;

import org.json.JSONException;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.runwaysdk.mvc.JsonConfiguration;
import com.runwaysdk.mvc.JsonSerializable;
import com.runwaysdk.mvc.RestSerializer;

import gov.geoplatform.uasdm.graph.Document;
import gov.geoplatform.uasdm.graph.ProcessingRun;
import gov.geoplatform.uasdm.lidar.LidarProcessConfiguration;
import gov.geoplatform.uasdm.odm.ODMProcessConfiguration;

public class ProcessingRunView implements JsonSerializable
{
  private String output;
  
  private LidarProcessConfiguration config;
  
  private DocumentView report;
  
  private Date runStart;
  
  private Date runEnd;
  
//  private Document[] generatedArtifacts;
  
  public ProcessingRunView()
  {
    
  }
  
  public static ProcessingRunView fromProcessingRun(ProcessingRun run)
  {
    ProcessingRunView view = new ProcessingRunView();
    
    view.setOutput(run.getOutput());
    
    view.setConfig(run.getConfiguration());
    
    Document report = run.getReport();
    if (report != null)
    {
      view.setReport(DocumentView.fromDocument(report));
    }
    
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

  public LidarProcessConfiguration getConfig()
  {
    return config;
  }

  public void setConfig(LidarProcessConfiguration config)
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
  
  public ProcessingRunView parse(String json)
  {
    GsonBuilder builder = new GsonBuilder();
    builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    return builder.create().fromJson(json, ProcessingRunView.class);
  }

  @Override
  public Object serialize(RestSerializer serializer, JsonConfiguration configuration) throws JSONException
  {
    return this.toJson().toString();
  }
}
