package gov.geoplatform.uasdm.service;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.uasdm.bus.Collection;
import gov.geoplatform.uasdm.bus.WorkflowTask;
import gov.geoplatform.uasdm.view.RequestParser;
import gov.geoplatform.uasdm.view.SiteItem;
import net.geoprism.GeoprismUser;

public class WorkflowService
{
  final Logger log = LoggerFactory.getLogger(WorkflowService.class);

  @Request(RequestType.SESSION)
  public void createUploadTask(String sessionId, RequestParser parser)
  {
    this.createUploadTaskInTransaction(sessionId, parser);
  }

  @Transaction
  public void createUploadTaskInTransaction(String sessionId, RequestParser parser)
  {
    WorkflowTask task = WorkflowTask.getTaskByUploadId(parser.getUuid());

    if (task == null)
    {
      Collection collection = this.getCollection(sessionId, parser);

      task = new WorkflowTask();
      task.setUpLoadId(parser.getUuid());
      task.setCollection(collection);
      task.setGeoprismUser((GeoprismUser) GeoprismUser.getCurrentUser());
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
  }

  @Request(RequestType.SESSION)
  public void updateUploadTask(String sessionId, RequestParser parser)
  {
    this.updateUploadTaskInTransaction(sessionId, parser);
  }

  @Transaction
  public void updateUploadTaskInTransaction(String sessionId, RequestParser parser)
  {
    WorkflowTask task = WorkflowTask.getTaskByUploadId(parser.getUuid());

    if (task != null)
    {
      task.lock();
      task.setStatus("Uploading");
      task.setMessage(parser.getPercent() + "% complete");
      task.apply();
    }
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
      item.setName(name);

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
  public JSONArray getTasks(String sessionId)
  {
    List<WorkflowTask> tasks = WorkflowTask.getUserTasks();
    return WorkflowTask.serialize(tasks);
  }

}
