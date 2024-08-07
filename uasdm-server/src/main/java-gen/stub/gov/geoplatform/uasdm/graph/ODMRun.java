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
package gov.geoplatform.uasdm.graph;

import java.util.Date;
import java.util.List;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;

import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.EdgeType;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.odm.ODMProcessConfiguration;
import gov.geoplatform.uasdm.odm.ODMProcessingTask;

public class ODMRun extends ODMRunBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 489278396;
  
  public ODMRun()
  {
    super();
  }
  
  /*
   * TODO: This shouldn't be necessary, but the base class is throwing a ClassCastException
   */
  @Override
  public Document getReport()
  {
    return Document.get(this.getObjectValue(REPORT));
  }
  
  @Override
  public UasComponent getComponent()
  {
    return UasComponent.get(this.getObjectValue(COMPONENT));
  }
  
  public ODMProcessConfiguration getConfiguration()
  {
    return ODMProcessConfiguration.parse(this.getConfig());
  }
  
  public static List<ODMRun> getByComponentOrdered(String componentId)
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(ODMRun.CLASS);
    final String oid = mdVertex.definesAttribute(ODMRun.OID).getColumnName();
    final String component = mdVertex.definesAttribute(ODMRun.COMPONENT).getColumnName();
    final String runEnd = mdVertex.definesAttribute(ODMRun.RUNEND).getColumnName();
    
    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName() + " WHERE " + component + "." + oid + " = :oid ORDER BY " + runEnd + " DESC");

    final GraphQuery<ODMRun> query = new GraphQuery<ODMRun>(statement.toString());
    query.setParameter("oid", componentId);

    return query.getResults();
  }
  
  /**
   * Returns the ODMRun which was responsible for generating the given artifact.
   * 
   * @param artifact
   * @return
   */
  public static ODMRun getGeneratingRun(Document artifact)
  {
    final MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.ODM_RUN_OUTPUT);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT EXPAND( IN('" + mdEdge.getDBClassName() + "'))\n");
    statement.append("FROM :rid \n");

    final GraphQuery<ODMRun> query = new GraphQuery<ODMRun>(statement.toString());
    query.setParameter("rid", artifact.getRID());

    return query.getResults().stream().sorted((a, b) -> a.getRunStart().compareTo(b.getRunStart())).findFirst().orElse(null);
  }
  
  /**
   * Returns the ODMRun associated with the given ODMProcessingTask.
   * 
   * @param task
   * @return
   */
  public static ODMRun getForTask(String taskId)
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(ODMRun.CLASS);
    
    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName() + " WHERE " + ODMRun.WORKFLOWTASK + " = :oid");

    final GraphQuery<ODMRun> query = new GraphQuery<ODMRun>(statement.toString());
    query.setParameter("oid", taskId);

    return query.getSingleResult();
  }
  
  /**
   * Creates a new ODMRun and populates it with data from the given ODMProcessingTask.
   * 
   * @param task
   * @return
   */
  public static ODMRun createAndApplyFor(ODMProcessingTask task)
  {
    UasComponent component = (UasComponent) task.getComponentInstance();
    
    ODMRun odmRun = new ODMRun();
    odmRun.setWorkflowTask(task);
    odmRun.setConfig(task.getConfigurationJson());
    odmRun.setRunStart(new Date());
    odmRun.setComponent(component);
    odmRun.apply();
    
    List<DocumentIF> documents = component.getDocuments();
    documents.forEach(doc -> odmRun.addODMRunInputParent((Document) doc).apply());
    
    return odmRun;
  }
  
}
