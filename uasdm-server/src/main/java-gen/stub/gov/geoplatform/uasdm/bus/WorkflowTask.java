package gov.geoplatform.uasdm.bus;

import java.text.DateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.query.Condition;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.OR;
import com.runwaysdk.query.QueryFactory;

import gov.geoplatform.uasdm.model.ComponentFacade;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ImageryWorkflowTaskIF;
import gov.geoplatform.uasdm.model.Page;
import gov.geoplatform.uasdm.model.UasComponentIF;
import net.geoprism.GeoprismUser;

public class WorkflowTask extends WorkflowTaskBase implements ImageryWorkflowTaskIF
{
  private static final long  serialVersionUID = 1976980729;

  public static final String NEEDS_METADATA   = "NEEDS_METADATA";

  public WorkflowTask()
  {
    super();
  }

  public static Page<WorkflowTask> getUserWorkflowTasks(String statuses, Integer pageNumber, Integer pageSize)
  {
    WorkflowTaskQuery query = new WorkflowTaskQuery(new QueryFactory());
    query.WHERE(query.getGeoprismUser().EQ(GeoprismUser.getCurrentUser()));

    if (statuses != null)
    {
      List<Condition> conditions = new LinkedList<Condition>();

      final JSONArray array = new JSONArray(statuses);

      for (int i = 0; i < array.length(); i++)
      {
        final WorkflowTaskStatus status = WorkflowTaskStatus.valueOf(array.getString(i));
        conditions.add(query.getStatus().EQ(status.name()));
      }

      if (conditions.size() > 0)
      {
        query.AND(OR.get(conditions.toArray(new Condition[conditions.size()])));
      }
    }

    query.ORDER_BY_ASC(query.getComponent());
    query.ORDER_BY_ASC(query.getLastUpdateDate());

    if (pageNumber != null && pageSize != null)
    {
      query.restrictRows(pageSize, pageNumber);
    }

    final long count = query.getCount();

    try (OIterator<? extends WorkflowTask> iterator = query.getIterator())
    {
      List<WorkflowTask> results = new LinkedList<WorkflowTask>(iterator.getAll());

      return new Page<WorkflowTask>(count, pageNumber, pageSize, results);
    }
  }

  public JSONObject toJSON()
  {
    DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.US);

    JSONObject obj = super.toJSON();
    obj.put("uploadId", this.getUploadId());
    obj.put("collection", this.getComponent());
    obj.put("collectionLabel", this.getComponentLabel());
    obj.put("message", this.getMessage());
    obj.put("status", this.getStatus());
    obj.put("lastUpdateDate", format.format(this.getLastUpdateDate()));
    obj.put("createDate", format.format(this.getCreateDate()));
    obj.put("type", this.getType());

    return obj;
  }

  /**
   * Returns a label of a component associated with this task.
   * 
   * @return label of a component associated with this task.
   */
  public String getComponentLabel()
  {
    return this.getImageryComponent().getName();
  }

  public ImageryComponent getImageryComponent()
  {
    return (ImageryComponent) getComponentInstance();
  }

  public UasComponentIF getComponentInstance()
  {
    return ComponentFacade.getComponent(this.getComponent());
  }
}
