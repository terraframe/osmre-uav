package gov.geoplatform.uasdm.bus;

import org.json.JSONObject;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

public class WorkflowTask extends WorkflowTaskBase
{
  private static final long serialVersionUID = 1976980729;

  public WorkflowTask()
  {
    super();
  }

  public JSONObject toJSON()
  {
    JSONObject obj = super.toJSON();
    obj.put("uploadId", this.getUpLoadId());
    obj.put("collection", this.getCollectionOid());
    obj.put("message", this.getMessage());
    obj.put("status", this.getStatus());
    obj.put("lastUpdateDate", this.getLastUpdateDate());
    obj.put("createDate", this.getCreateDate());

    return obj;
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
}
