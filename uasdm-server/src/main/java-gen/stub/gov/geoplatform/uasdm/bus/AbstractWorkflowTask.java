/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

import gov.geoplatform.uasdm.CollectionStatus;
import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.model.AbstractWorkflowTaskIF;
import gov.geoplatform.uasdm.model.JSONSerializable;
import gov.geoplatform.uasdm.model.Page;
import gov.geoplatform.uasdm.odm.ODMStatus;
import gov.geoplatform.uasdm.ws.GlobalNotificationMessage;
import gov.geoplatform.uasdm.ws.MessageType;
import gov.geoplatform.uasdm.ws.NotificationFacade;
import net.geoprism.GeoprismUser;

public abstract class AbstractWorkflowTask extends AbstractWorkflowTaskBase implements AbstractWorkflowTaskIF, JSONSerializable
{
  private static final long serialVersionUID = 227492042;

  public static enum WorkflowTaskStatus {
    PROCESSING("Processing"), COMPLETE("Complete"), ERROR("Error"), QUEUED("Queued"), UPLOADING("Uploading"), STARTED("Started");

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
  
  public static enum TaskActionType {
    ERROR("error"),
    WARNING("warning");
    
    private String type;
    
    TaskActionType(String type)
    {
      this.type = type;
    }
    
    public String getType()
    {
      return this.type;
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

    if (type != null && type.equals("error"))
    {
      CollectionStatus.updateStatus(this);

      NotificationFacade.queue(new GlobalNotificationMessage(MessageType.JOB_CHANGE, null));
    }
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

  public String getNormalizedStatus()
  {
    String status = this.getStatus();

    if (status != null && status.equals(WorkflowTaskStatus.ERROR.toString()))
    {
      return ODMStatus.FAILED.getLabel();
    }
    else if (status != null && ( status.equals(ODMStatus.RUNNING.getLabel()) || status.equals(WorkflowTaskStatus.STARTED.toString()) ))
    {
      return WorkflowTaskStatus.PROCESSING.toString();
    }
    else if (status != null && status.equals("Pending"))
    {
      return WorkflowTaskStatus.QUEUED.toString();
    }

    return status;
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
    obj.put("createDate", Util.formatIso8601(this.getCreateDate(), true));
    obj.put("lastUpdateDate", Util.formatIso8601(this.getLastUpdateDate(), true));
    obj.put("status", this.getNormalizedStatus());
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
