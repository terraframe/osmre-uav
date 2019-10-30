package gov.geoplatform.uasdm.service;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask.WorkflowTaskStatus;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.ComponentFacade;
import gov.geoplatform.uasdm.model.ImageryWorkflowTaskIF;
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
      task.setStatus("Started");
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
      task.setStatus("Uploading");
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

// Heads up: Clean up    
//  // Determine if a collection needs to be created
//  public Collection getCollection(String sessionId, RequestParser parser)
//  {
//    Map<String, String> params = parser.getCustomParams();
//    Boolean createCollection = new Boolean(params.get("create"));
//
//    if (createCollection)
//    {
//      String missionId = params.get("mission");
//      String name = params.get("name");
//
//      SiteItem item = new SiteItem();
//      item.setValue(UasComponent.NAME, name);
//      item.setValue(UasComponent.FOLDERNAME, name);
//      item.setValue(UasComponent.DESCRIPTION, "");
//
//      item = new ProjectManagementService().applyWithParent(sessionId, item, missionId);
//
//      return Collection.get(item.getId());
//    }
//    else
//    {
//      String collectionId = params.get("collection");
//
//      return Collection.get(collectionId);
//    }
//  }
// 
//  // Determine if a Imagery needs to be created
//  public Imagery getImagery(String sessionId, RequestParser parser)
//  {
//    Map<String, String> params = parser.getCustomParams();
//    Boolean createCollection = new Boolean(params.get("create"));
//
//    if (createCollection)
//    {
//      String projectId = params.get("project");
//      String name = params.get("name");
//
//      SiteItem item = new SiteItem();
//      item.setValue(UasComponent.NAME, name);
//      item.setValue(UasComponent.FOLDERNAME, name);
//      item.setValue(UasComponent.DESCRIPTION, "");
//
//      item = new ProjectManagementService().applyWithParent(sessionId, item, projectId);
//
//      return Imagery.get(item.getId());
//    }
//    else
//    {
//      String imageryId = params.get("imagery");
//
//      return Imagery.get(imageryId);
//    }
//  }

  @Request(RequestType.SESSION)
  public JSONObject getTasks(String sessionId)
  {
    List<AbstractWorkflowTask> tasks = AbstractWorkflowTask.getUserTasks();
    java.util.Collection<CollectionIF> missions = ComponentFacade.getMissingMetadata();

    JSONObject response = new JSONObject();
    response.put("tasks", AbstractWorkflowTask.serialize(tasks));
    response.put("messages", Collection.toMetadataMessage(missions));

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
