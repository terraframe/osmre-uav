package gov.geoplatform.uasdm.bus;

import java.util.LinkedList;
import java.util.List;

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
