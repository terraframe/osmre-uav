package gov.geoplatform.uasdm.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask.WorkflowTaskStatus;
import gov.geoplatform.uasdm.bus.WorkflowTask;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.ComponentFacade;
import gov.geoplatform.uasdm.model.ImageryWorkflowTaskIF;
import gov.geoplatform.uasdm.model.Page;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.view.RequestParser;

public class WorkflowService
{
  final Logger log = LoggerFactory.getLogger(WorkflowService.class);

  @Request(RequestType.SESSION)
  public JSONObject createUploadTask(String sessionId, RequestParser parser)
  {
    return this.createUploadTaskInTransaction(sessionId, parser);
  }

  @Transaction
  public JSONObject createUploadTaskInTransaction(String sessionId, RequestParser parser)
  {
    AbstractWorkflowTask task = ImageryWorkflowTaskIF.getWorkflowTaskForUpload(parser);

    if (task == null)
    {
      UasComponentIF uasComponent = ImageryWorkflowTaskIF.getOrCreateUasComponentFromRequestParser(parser);
      task = uasComponent.createWorkflowTask(parser.getUuid());
    }
    else
    {
      task.lock();
    }

    if (task.getStatus().length() == 0)
    {
      task.setStatus(WorkflowTaskStatus.STARTED.toString());
    }

    task.setMessage(parser.getPercent() + "% complete");
    task.apply();

    JSONObject message = new JSONObject();
    message.put("currentTask", task.toJSON());

    return message;
  }

  @Request(RequestType.SESSION)
  public JSONObject updateUploadTask(String sessionId, RequestParser parser)
  {
    return this.updateUploadTaskInTransaction(sessionId, parser);
  }

  @Transaction
  public JSONObject updateUploadTaskInTransaction(String sessionId, RequestParser parser)
  {
    AbstractWorkflowTask task = ImageryWorkflowTaskIF.getWorkflowTaskForUpload(parser);

    if (task != null)
    {
      task.lock();
      task.setStatus(WorkflowTaskStatus.UPLOADING.toString());
      task.setMessage("Uploading to the staging environment..."); // parser.getPercent()
                                                                  // + "%
                                                                  // complete"
      task.apply();
    }

    JSONObject message = new JSONObject();
    message.put("currentTask", task.toJSON());

    return message;
  }

  @Request(RequestType.SESSION)
  public void errorUploadTask(String sessionId, RequestParser parser, String message)
  {
    this.errorUploadTaskInTransaction(sessionId, parser, message);
  }

  @Transaction
  public void errorUploadTaskInTransaction(String sessionId, RequestParser parser, String message)
  {
    AbstractWorkflowTask task = ImageryWorkflowTaskIF.getWorkflowTaskForUpload(parser);

    if (task != null)
    {
      task.lock();
      task.setStatus(WorkflowTaskStatus.ERROR.toString());
      task.setMessage(message);
      task.apply();
    }
  }

  @Request(RequestType.SESSION)
  public JSONObject getTasks(String sessionId, String statuses, Integer pageNumber, Integer pageSize)
  {
    Page<WorkflowTask> page = WorkflowTask.getUserWorkflowTasks(statuses, pageNumber, pageSize);
    java.util.Collection<CollectionIF> missions = ComponentFacade.getMissingMetadata();

    JSONObject response = new JSONObject();
    response.put("tasks", page.toJSON());
    response.put("messages", Collection.toMetadataMessage(missions));

    return response;
  }
  
  @Request(RequestType.SESSION)
  public JSONObject getTasksCount(String sessionId, String status)
  {
    java.util.Collection<CollectionIF> missions = ComponentFacade.getMissingMetadata();
    long count = WorkflowTask.getUserWorkflowTasksCount(status);

    JSONObject response = new JSONObject();
    response.put("tasksCount", count + Collection.toMetadataMessage(missions).length());

    return response;
  }

  @Request(RequestType.SESSION)
  public JSONArray getMissingMetadata(String sessionId)
  {
    java.util.Collection<CollectionIF> missions = ComponentFacade.getMissingMetadata();

    return Collection.toMetadataMessage(missions);
  }

  @Request(RequestType.SESSION)
  public JSONObject getTask(String sessionId, String id)
  {
    JSONObject response = new JSONObject();
    response.put("task", AbstractWorkflowTask.get(id).toJSON());

    return response;
  }

}
