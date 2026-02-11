package gov.geoplatform.uasdm.graph;

import java.util.Date;
import java.util.List;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;

import gov.geoplatform.uasdm.lidar.LidarProcessConfiguration;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.EdgeType;
import gov.geoplatform.uasdm.processing.FargateProcessingTask;
import gov.geoplatform.uasdm.processing.FargateTaskDefinition;

public class ProcessingRun extends ProcessingRunBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 1954517997;
  
  public ProcessingRun()
  {
    super();
  }
  
  @Override
  public UasComponent getComponent()
  {
    return UasComponent.get(this.getObjectValue(COMPONENT));
  }
  
  // TODO : This might not always be lidar
  public LidarProcessConfiguration getConfiguration()
  {
    return LidarProcessConfiguration.parse(this.getConfig());
  }
  
  public static List<ProcessingRun> getByComponentOrdered(String componentId)
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(ProcessingRun.CLASS);
    final String oid = mdVertex.definesAttribute(ProcessingRun.OID).getColumnName();
    final String component = mdVertex.definesAttribute(ProcessingRun.COMPONENT).getColumnName();
    final String runEnd = mdVertex.definesAttribute(ProcessingRun.RUNEND).getColumnName();
    
    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName() + " WHERE " + component + "." + oid + " = :oid ORDER BY " + runEnd + " DESC");

    final GraphQuery<ProcessingRun> query = new GraphQuery<ProcessingRun>(statement.toString());
    query.setParameter("oid", componentId);

    return query.getResults();
  }
  
  /**
   * Returns the ProcessingRun which was responsible for generating the given artifact.
   * 
   * @param artifact
   * @return
   */
  public static ProcessingRun getGeneratingRun(Document artifact)
  {
    final MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.ODM_RUN_OUTPUT);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT EXPAND( IN('" + mdEdge.getDBClassName() + "'))\n");
    statement.append("FROM :rid \n");

    final GraphQuery<ProcessingRun> query = new GraphQuery<ProcessingRun>(statement.toString());
    query.setParameter("rid", artifact.getRID());

    return query.getResults().stream().sorted((a, b) -> a.getRunStart().compareTo(b.getRunStart())).findFirst().orElse(null);
  }
  
  /**
   * Returns the ProcessingRun associated with the given ODMProcessingTask.
   * 
   * @param task
   * @return
   */
  public static ProcessingRun getForTask(String taskId)
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(ProcessingRun.CLASS);
    
    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName() + " WHERE " + ProcessingRun.WORKFLOWTASK + " = :oid");

    final GraphQuery<ProcessingRun> query = new GraphQuery<ProcessingRun>(statement.toString());
    query.setParameter("oid", taskId);

    return query.getSingleResult();
  }
  
  /**
   * Creates a new ProcessingRun and populates it with data from the given FargateProcessingTask.
   * 
   * @param task
   * @return
   */
  public static ProcessingRun createAndApplyFor(FargateProcessingTask task, String autoscaleResourceId, FargateTaskDefinition fargateTask)
  {
    UasComponent component = (UasComponent) task.getComponentInstance();
    
    ProcessingRun odmRun = new ProcessingRun();
    odmRun.setWorkflowTask(task);
    odmRun.setConfig(task.getConfigurationJson());
    odmRun.setRunStart(new Date());
    odmRun.setProcessingType(fargateTask.getArn());
    odmRun.setAutoscaleResourceId(autoscaleResourceId);
    odmRun.setComponent(component);
    odmRun.apply();
    
    List<DocumentIF> documents;
    if (component instanceof Collection)
      documents = ((Collection)component).getRaw();
    else
      documents = component.getDocuments();
    
    documents.forEach(doc -> odmRun.addProcessingRunInputParent((Document) doc).apply());
    
    return odmRun;
  }
  
}
