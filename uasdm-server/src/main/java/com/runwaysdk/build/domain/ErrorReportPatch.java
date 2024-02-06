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

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.CollectionStatus;
import gov.geoplatform.uasdm.bus.ErrorReport;
import gov.geoplatform.uasdm.bus.WorkflowTask;
import gov.geoplatform.uasdm.bus.WorkflowTaskQuery;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.model.CollectionIF;

public class ErrorReportPatch
{
  public static void main(String[] args) throws InterruptedException
  {
    doIt();
  }

  @Request
  public static void doIt() throws InterruptedException
  {
    int count = 0;
    
    for (Collection col : getCollectionQuery().getResults())
    {
      WorkflowTask task = getFailureTask(col);
      
      if (task != null)
      {
        ErrorReport er = new ErrorReport();
        er.Populate(col, task);
        er.apply();
        
        count++;
      }
    }
    
    System.out.println("Successfully created " + count + " error reports.");
  }
  
  public static WorkflowTask getFailureTask(CollectionIF col)
  {
    WorkflowTaskQuery wtq = new WorkflowTaskQuery(new QueryFactory());
    
    wtq.WHERE(wtq.getComponent().EQ(col.getOid()));
    
    try (OIterator<? extends WorkflowTask> it = wtq.getIterator())
    {
      for (WorkflowTask task : it)
      {
        if (CollectionStatus.getStatus(task) == "Failed")
        {
          return task;
        }
      }
    }
    
    return null;
  }
  
  private static GraphQuery<Collection> getCollectionQuery()
  {
    final String collection0 = MdVertexDAO.getMdVertexDAO(Collection.CLASS).getDBClassName();

    StringBuilder builder = new StringBuilder();
    builder.append("SELECT * FROM " + collection0);
    
    final GraphQuery<Collection> query = new GraphQuery<Collection>(builder.toString());

    return query;
  }
}
