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
package com.runwaysdk.build.domain;

import java.util.List;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.CollectionStatus;
import gov.geoplatform.uasdm.CollectionStatusQuery;
import gov.geoplatform.uasdm.bus.WorkflowTask;
import gov.geoplatform.uasdm.bus.WorkflowTaskQuery;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.graph.ODMRun;
import gov.geoplatform.uasdm.graph.Sensor;
import gov.geoplatform.uasdm.model.ComponentFacade;
import gov.geoplatform.uasdm.model.UasComponentIF;

public class CollectionStatusAnalytics
{
  public static void main(String[] args) throws InterruptedException
  {
    doIt();
  }

  @Request
  public static void doIt() throws InterruptedException
  {
    CollectionStatusQuery query = new CollectionStatusQuery(new QueryFactory());
    
    query.WHERE(query.getStatus().EQ("Failed"));
    
    try (OIterator<? extends CollectionStatus> it = query.getIterator())
    {
      while (it.hasNext())
      {
        CollectionStatus cs = it.next();
        
        String componentId = cs.getComponent();
        
        UasComponentIF uas = ComponentFacade.getComponent(componentId);
        if (!(uas instanceof Collection)) { continue; }
        Collection col = (Collection)uas;
        
        Sensor sensor = col.getSensor();
        cs.setSensorName(sensor.getName());
        cs.setSensorType(sensor.getSensorType().getName());
        
        cs.setCollectionSize((long) col.getDocuments().size());
        
        List<ODMRun> odmRuns = ODMRun.getByComponentOrdered(col.getOid());
        
        if (odmRuns.size() > 0)
        {
          ODMRun odmRun = odmRuns.get(odmRuns.size()-1);
          
          cs.setOdmConfig(odmRun.getConfig());
        }
        
        cs.setFailReason(getFailureReason(cs));
      }
    }
  }
  
  public static String getFailureReason(CollectionStatus cs)
  {
    WorkflowTaskQuery wtq = new WorkflowTaskQuery(new QueryFactory());
    
    wtq.WHERE(wtq.getComponent().EQ(cs.getComponent()));
    
    try (OIterator<? extends WorkflowTask> it = wtq.getIterator())
    {
      for (WorkflowTask task : it)
      {
        if (CollectionStatus.getStatus(task) == "Failed")
        {
          return task.getMessage();
        }
      }
    }
    
    return "";
  }
}
