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

import gov.geoplatform.uasdm.model.AbstractWorkflowTaskIF;
import gov.geoplatform.uasdm.model.JSONSerializable;
import gov.geoplatform.uasdm.model.Page;
import net.geoprism.GeoprismUser;

public abstract class AbstractWorkflowTask extends AbstractWorkflowTaskBase implements AbstractWorkflowTaskIF, JSONSerializable
{
  private static final long serialVersionUID = 227492042;

  public static enum WorkflowTaskStatus {
    PROCESSING("Processing"), COMPLETE("Complete"), ERROR("Error"), QUEUED("Queued");

    private final String asString;

    private WorkflowTaskStatus(String asString)
    {
      this.asString = asString;
    }

    public String toString()
    {
      return asString;
    }
  }

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

    try (OIterator<? extends WorkflowAction> iterator = query.getIterator())
    {
      return new LinkedList<WorkflowAction>(iterator.getAll());
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

  public static Page<AbstractWorkflowTask> getUserTasks(String status, Integer pageNumber, Integer pageSize)
  {
    AbstractWorkflowTaskQuery query = new AbstractWorkflowTaskQuery(new QueryFactory());
    query.WHERE(query.getGeoprismUser().EQ(GeoprismUser.getCurrentUser()));

    if (status != null)
    {
      query.AND(query.getStatus().EQ(WorkflowTaskStatus.valueOf(status).name()));
    }

    query.ORDER_BY_ASC(query.getLastUpdateDate());

    if (pageNumber != null && pageSize != null)
    {
      query.restrictRows(pageSize, pageNumber);
    }

    final long count = query.getCount();

    try (OIterator<? extends AbstractWorkflowTask> iterator = query.getIterator())
    {
      List<AbstractWorkflowTask> results = new LinkedList<AbstractWorkflowTask>(iterator.getAll());

      return new Page<AbstractWorkflowTask>(count, pageNumber, pageSize, results);
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
