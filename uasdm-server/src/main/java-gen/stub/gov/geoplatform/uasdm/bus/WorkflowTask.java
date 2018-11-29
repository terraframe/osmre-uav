package gov.geoplatform.uasdm.bus;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import net.geoprism.GeoprismUser;

public class WorkflowTask extends WorkflowTaskBase
{
  private static final long serialVersionUID = 1976980729;

  public WorkflowTask()
  {
    super();
  }

  @Override
  @Transaction
  public void delete()
  {
    List<WorkflowAction> actions = this.getActions();

    for (WorkflowAction action : actions)
    {
      action.delete();
    }

    super.delete();
  }

  public List<WorkflowAction> getActions()
  {
    WorkflowActionQuery query = new WorkflowActionQuery(new QueryFactory());
    query.WHERE(query.getWorkflowTask().EQ(this));

    OIterator<? extends WorkflowAction> iterator = query.getIterator();

    try
    {
      return new LinkedList<WorkflowAction>(iterator.getAll());
    }
    finally
    {
      iterator.close();
    }
  }

  @Transaction
  public void createAction(String message, String type)
  {
    WorkflowAction action = new WorkflowAction();
    action.setActionType(type);
    action.setDescription(message);
    action.setWorkflowTask(this);
    action.apply();
  }

  public JSONObject toJSON()
  {
    List<WorkflowAction> actions = this.getActions();

    JSONArray jActions = new JSONArray();

    for (WorkflowAction action : actions)
    {
      jActions.put(action.toJSON());
    }

    JSONObject obj = new JSONObject();
    obj.put("id", this.getUpLoadId());
    obj.put("createDate", this.getCreateDate());
    obj.put("lastUpdatedDate", this.getLastUpdateDate());
    obj.put("status", this.getStatus());
    obj.put("message", this.getMessage());
    obj.put("collection", this.getCollectionOid());
    obj.put("actions", jActions);

    return obj;
  }

  public static List<WorkflowTask> getUserTasks()
  {
    WorkflowTaskQuery query = new WorkflowTaskQuery(new QueryFactory());
    query.WHERE(query.getGeoprismUser().EQ(GeoprismUser.getCurrentUser()));

    OIterator<? extends WorkflowTask> iterator = query.getIterator();

    try
    {
      return new LinkedList<WorkflowTask>(iterator.getAll());
    }
    finally
    {
      iterator.close();
    }
  }

  public static WorkflowTask getTaskByUploadId(String uploadId)
  {
    WorkflowTaskQuery query = new WorkflowTaskQuery(new QueryFactory());
    query.WHERE(query.getUpLoadId().EQ(uploadId));

    OIterator<? extends WorkflowTask> it = query.getIterator();

    try
    {
      if (it.hasNext())
      {
        return it.next();
      }
    }
    finally
    {
      it.close();
    }

    return null;
  }

  public static JSONArray serialize(List<WorkflowTask> tasks)
  {
    JSONArray array = new JSONArray();

    for (WorkflowTask task : tasks)
    {
      array.put(task.toJSON());
    }

    return array;
  }

}
