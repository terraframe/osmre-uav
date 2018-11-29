package gov.geoplatform.uasdm.bus;

import gov.geoplatform.uasdm.view.SiteItem;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

public class WorkflowTask extends WorkflowTaskBase
{
  private static final long serialVersionUID = 1976980729;

  public WorkflowTask()
  {
    super();
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
	JSONObject obj = new JSONObject();
	obj.put("id", this.getUpLoadId());
	obj.put("createDate", this.getCreateDate());
	obj.put("lastUpdatedDate", this.getLastUpdateDate());
	obj.put("status", this.getStatus());
	obj.put("message", this.getMessage());
	obj.put("collection", this.getCollectionOid());
	obj.put("owner", this.getGeoprismUser());
	
	return obj;
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
