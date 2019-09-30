package gov.geoplatform.uasdm.bus;

import java.text.DateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import net.geoprism.GeoprismUser;

public abstract class AbstractWorkflowTask extends AbstractWorkflowTaskBase implements AbstractWorkflowTaskIF
{
  private static final long  serialVersionUID = 227492042;

  public static final String PROCESSING       = "Processing";

  public static final String COMPLETE         = "Complete";

  public static final String QUEUED           = "Queued";

  public AbstractWorkflowTask()
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

  public static List<AbstractWorkflowTask> getUserTasks()
  {
    AbstractWorkflowTaskQuery query = new AbstractWorkflowTaskQuery(new QueryFactory());
    query.WHERE(query.getGeoprismUser().EQ(GeoprismUser.getCurrentUser()));
    query.ORDER_BY_ASC(query.getLastUpdateDate());

    OIterator<? extends AbstractWorkflowTask> iterator = query.getIterator();

    try
    {
      return new LinkedList<AbstractWorkflowTask>(iterator.getAll());
    }
    finally
    {
      iterator.close();
    }
  }

  public JSONObject toJSON()
  {
    DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.US);

    List<WorkflowAction> actions = this.getActions();

    JSONArray jActions = new JSONArray();

    for (WorkflowAction action : actions)
    {
      jActions.put(action.toJSON());
    }

    JSONObject obj = new JSONObject();
    obj.put("oid", this.getOid());
    obj.put("label", this.getTaskLabel());
    obj.put("createDate", format.format(this.getCreateDate()));
    obj.put("lastUpdatedDate", format.format(this.getLastUpdateDate()));
    obj.put("status", this.getStatus());
    obj.put("message", this.getMessage());
    obj.put("actions", jActions);

    return obj;
  }

  public static JSONArray serialize(List<AbstractWorkflowTask> tasks)
  {
    JSONArray array = new JSONArray();

    for (AbstractWorkflowTask task : tasks)
    {
      array.put(task.toJSON());
    }

    return array;
  }

  /**
   * Returns a label of a component associated with this task.
   * 
   * @return label of a component associated with this task.
   */
  public String getComponentLabel()
  {
    return "";
  }

}
