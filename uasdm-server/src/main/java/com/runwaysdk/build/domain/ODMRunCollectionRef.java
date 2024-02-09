package com.runwaysdk.build.domain;

import java.util.HashMap;
import java.util.Map;

import com.orientechnologies.orient.core.id.ORID;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.cache.DataNotFoundException;
import com.runwaysdk.dataaccess.graph.GraphDBService;
import com.runwaysdk.dataaccess.graph.GraphRequest;
import com.runwaysdk.dataaccess.graph.orientdb.OrientDBRequest;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.bus.WorkflowTask;
import gov.geoplatform.uasdm.graph.ODMRun;
import gov.geoplatform.uasdm.graph.UasComponent;

public class ODMRunCollectionRef
{
  public static void main(String[] args)
  {
    mainInRequest();
  }
  
  @Request
  public static void mainInRequest()
  {
    mainInTrans();
  }
  
//  @Transaction // Transaction is causing max chunk read errors apparently
  public static void mainInTrans()
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(ODMRun.CLASS);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT @rid,workflowTask FROM " + mdVertex.getDBClassName() + " WHERE component IS NULL");

    final GraphQuery<Map<String,Object>> query = new GraphQuery<Map<String,Object>>(statement.toString());

    int count = 0;
    
    for (Map<String,Object> values : query.getResults())
    {
      try
      {
        String taskOid = (String) values.get("workflowTask");
        ORID rid = (ORID) values.get("@rid");
        
        WorkflowTask task = WorkflowTask.get(taskOid);
        
        // If we try to fetch an ODMRun object from the database the odm output causes max chunk read errors unfortunately
        GraphDBService service = GraphDBService.getInstance();
        GraphRequest request = service.getGraphDBRequest();
        OrientDBRequest orientDBRequest = (OrientDBRequest) request;
        
        HashMap<String,Object> params = new HashMap<String,Object>();
        params.put("colOid", ((UasComponent)task.getComponentInstance()).getRID());
        params.put("rid", rid);
        service.command(orientDBRequest, "UPDATE odmr_un SET component=:colOid WHERE @rid=:rid", params);
        
        count++;
      }
      catch (DataNotFoundException ex) {}
    }
    
    System.out.println("Successfully updated " + count + " odm run objects that did not previously have component references.");
  }
}
