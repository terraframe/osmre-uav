package gov.geoplatform.uasdm.service;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.Collection;
import gov.geoplatform.uasdm.bus.Mission;
import gov.geoplatform.uasdm.bus.UasComponent;
import gov.geoplatform.uasdm.bus.WorkflowTask;
import gov.geoplatform.uasdm.view.RequestParser;
import gov.geoplatform.uasdm.view.SiteItem;
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
    WorkflowTask task = WorkflowTask.getTaskByUploadId(parser.getUuid());

    if (task == null)
    {
      Collection collection = this.getCollection(sessionId, parser);

      task = new WorkflowTask();
      task.setUpLoadId(parser.getUuid());
      task.setCollection(collection);
      task.setGeoprismUser((GeoprismUser) GeoprismUser.getCurrentUser());
      task.setTaskLabel("Upload of file [" + parser.getOriginalFilename() + "] to collection [" + collection.getName() + "]");
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
    WorkflowTask task = WorkflowTask.getTaskByUploadId(parser.getUuid());

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
    WorkflowTask task = WorkflowTask.getTaskByUploadId(parser.getUuid());

    if (task != null)
    {
      task.lock();
      task.setStatus("Error");
      task.setMessage(message);
      task.apply();
    }
  }

  // Determine if a collection needs to be created
  public Collection getCollection(String sessionId, RequestParser parser)
  {
    Map<String, String> params = parser.getCustomParams();
    Boolean createCollection = new Boolean(params.get("create"));

    if (createCollection)
    {
      String missionId = params.get("mission");
      String name = params.get("name");

      SiteItem item = new SiteItem();
      item.setValue(UasComponent.NAME, name);

      item = new ProjectManagementService().applyWithParent(sessionId, item, missionId);

      return Collection.get(item.getId());
    }
    else
    {
      String collectionId = params.get("collection");

      return Collection.get(collectionId);
    }
  }

  @Request(RequestType.SESSION)
  public JSONObject getTasks(String sessionId)
  {
    List<AbstractWorkflowTask> tasks = AbstractWorkflowTask.getUserTasks();
    List<Mission> missions = Mission.getMissingMetadata();

    JSONObject response = new JSONObject();
    response.put("tasks", AbstractWorkflowTask.serialize(tasks));
    response.put("messages", Mission.toMetadataMessage(missions));

    return response;
  }

  @Request(RequestType.SESSION)
  public JSONArray getMissingMetadata(String sessionId)
  {
    List<Mission> missions = Mission.getMissingMetadata();

    return Mission.toMetadataMessage(missions);
  }

  @Request(RequestType.SESSION)
  public JSONObject getTask(String sessionId, String id)
  {
    JSONObject response = new JSONObject();
    response.put("task", WorkflowTask.get(id).toJSON());

    return response;
  }

}
