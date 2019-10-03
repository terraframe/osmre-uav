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
import gov.geoplatform.uasdm.bus.Collection;
import gov.geoplatform.uasdm.bus.Imagery;
import gov.geoplatform.uasdm.bus.ImageryWorkflowTask;
import gov.geoplatform.uasdm.bus.ImageryWorkflowTaskIF;
import gov.geoplatform.uasdm.bus.UasComponent;
import gov.geoplatform.uasdm.bus.WorkflowTask;
import gov.geoplatform.uasdm.view.RequestParser;
import net.geoprism.GeoprismUser;

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
      UasComponent uasComponent = ImageryWorkflowTaskIF.getOrCreateUasComponentFromRequestParser(parser);

      if (uasComponent instanceof Imagery)
      {
//        Imagery imagery = this.getImagery(sessionId, parser);
        Imagery imagery = (Imagery) uasComponent;

        ImageryWorkflowTask imageryWorkflowTask = new ImageryWorkflowTask();
        imageryWorkflowTask.setUploadId(parser.getUuid());
        imageryWorkflowTask.setImagery(imagery);
        imageryWorkflowTask.setGeoprismUser((GeoprismUser) GeoprismUser.getCurrentUser());
        imageryWorkflowTask.setTaskLabel("UAV data upload for imagery [" + imagery.getName() + "]");

        task = imageryWorkflowTask;
      }
      else
      {
//        Collection collection = this.getCollection(sessionId, parser);
        Collection collection = (Collection) uasComponent;

        WorkflowTask workflowTask = new WorkflowTask();
        workflowTask.setUploadId(parser.getUuid());
        workflowTask.setComponent(collection);
        workflowTask.setGeoprismUser((GeoprismUser) GeoprismUser.getCurrentUser());
        workflowTask.setTaskLabel("UAV data upload for collection [" + collection.getName() + "]");

        task = workflowTask;
      }
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
      task.setStatus("Error");
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
    java.util.Collection<Collection> missions = Collection.getMissingMetadata();

    JSONObject response = new JSONObject();
    response.put("tasks", AbstractWorkflowTask.serialize(tasks));
    response.put("messages", Collection.toMetadataMessage(missions));

    return response;
  }

  @Request(RequestType.SESSION)
  public JSONArray getMissingMetadata(String sessionId)
  {
    java.util.Collection<Collection> missions = Collection.getMissingMetadata();

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
