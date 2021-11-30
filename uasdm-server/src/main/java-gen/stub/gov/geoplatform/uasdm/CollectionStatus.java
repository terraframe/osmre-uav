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
package gov.geoplatform.uasdm;

import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.Condition;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.OR;
import com.runwaysdk.query.QueryFactory;

import gov.geoplatform.uasdm.bus.WorkflowTask;
import gov.geoplatform.uasdm.bus.WorkflowTaskQuery;
import gov.geoplatform.uasdm.model.AbstractWorkflowTaskIF;
import gov.geoplatform.uasdm.model.ComponentFacade;
import gov.geoplatform.uasdm.model.JSONSerializable;
import gov.geoplatform.uasdm.model.Page;
import gov.geoplatform.uasdm.model.UasComponentIF;
import net.geoprism.GeoprismUser;

public class CollectionStatus extends CollectionStatusBase implements JSONSerializable
{
  private static final long serialVersionUID = -1406310431;

  public CollectionStatus()
  {
    super();
  }

  @Override
  public JSONObject toJSON()
  {
    UasComponentIF component = ComponentFacade.getComponent(this.getComponent());

    final List<UasComponentIF> ancestors = component.getAncestors();
    Collections.reverse(ancestors);

    final JSONArray parents = new JSONArray();

    for (UasComponentIF ancestor : ancestors)
    {
      parents.put(ancestor.getName());
    }

    JSONObject obj = new JSONObject();
    obj.put("label", component.getName());
    obj.put("collectionId", component.getOid());
    obj.put("status", this.getStatus());
    obj.put("lastUpdateDate", Util.formatIso8601(this.getLastModificationDate(), true));
    obj.put("ancestors", parents);

    return obj;
  }

  @Transaction
  public static void create()
  {
    WorkflowTaskQuery taskQuery = new WorkflowTaskQuery(new QueryFactory());
    taskQuery.ORDER_BY_ASC(taskQuery.getComponent());
    taskQuery.ORDER_BY_DESC(taskQuery.getCreateDate());

    String componentId = null;
    Date date = null;

    Map<String, LinkedList<WorkflowTask>> componentTasks = new HashMap<String, LinkedList<WorkflowTask>>();

    try (OIterator<? extends WorkflowTask> iterator = taskQuery.getIterator())
    {
      while (iterator.hasNext())
      {
        WorkflowTask task = iterator.next();

        if (componentId != null && !task.getComponent().equals(componentId))
        {
          createCollectionStatus(componentId, date, componentTasks);

          date = null;
          componentId = null;
          componentTasks.clear();
        }

        if (componentId == null)
        {
          // Most recent task for the next component
          componentId = task.getComponent();
          date = task.getLastUpdateDate();
        }

        componentTasks.putIfAbsent(task.getType(), new LinkedList<WorkflowTask>());
        componentTasks.get(task.getType()).add(task);
      }
    }

    if (componentId != null)
    {
      createCollectionStatus(componentId, date, componentTasks);
    }

  }

  private static void createCollectionStatus(String componentId, Date lastUpdateDate, Map<String, LinkedList<WorkflowTask>> componentTasks)
  {
    String status = mergeTaskGroupStatuses(componentTasks);

    CollectionStatus collectionStatus = new CollectionStatus();
    collectionStatus.setComponent(componentId);
    collectionStatus.setStatus(status);
    collectionStatus.setLastModificationDate(lastUpdateDate);
    collectionStatus.apply();
  }

  public static String mergeTaskGroupStatuses(Map<String, LinkedList<WorkflowTask>> componentTasks)
  {
    String status = componentTasks.values().stream().map(tasks -> getGroupStatus(tasks)).reduce("Complete", (a, b) -> {
      if (a.equals("Processing") || b.equals("Processing"))
      {
        return "Processing";
      }
      else if (a.equals("Failed") || b.equals("Failed"))
      {
        return "Failed";
      }
      else if (a.equals("Warning") || b.equals("Warning"))
      {
        return "Warning";
      }

      return "Complete";
    });
    return status;
  }

  private static String getGroupStatus(LinkedList<WorkflowTask> componentTasks)
  {
    return getStatus(componentTasks.getFirst());
  }

  private static String getStatus(WorkflowTask task)
  {
    String taskStatus = task.getNormalizedStatus();

    String status = "Complete";

    if (taskStatus.equals("Error") || taskStatus.equals("Failed"))
    {
      status = "Failed";
    }
    else if (taskStatus.equals("Queued") || taskStatus.equals("Processing") || taskStatus.equals("Running") || taskStatus.equals("Pending") || taskStatus.equals("Uploading"))
    {
      status = "Processing";
    }
    else if (task.getActions().size() > 0)
    {
      status = "Warning";
    }

    return status;
  }

  public static Map<String, LinkedList<WorkflowTask>> createTaskGroups(List<? extends WorkflowTask> tasks)
  {
    Map<String, LinkedList<WorkflowTask>> taskGroups = new HashMap<String, LinkedList<WorkflowTask>>();

    for (WorkflowTask task : tasks)
    {
      taskGroups.putIfAbsent(task.getType(), new LinkedList<WorkflowTask>());
      taskGroups.get(task.getType()).add(task);
    }

    return taskGroups;
  }

  public static void updateStatus(AbstractWorkflowTaskIF task)
  {
    if (task instanceof WorkflowTask)
    {
      updateStatus( ( (WorkflowTask) task ).getComponent());
    }
  }

  @Transaction
  public static void updateStatus(String componentId)
  {
    if (componentId != null && componentId.length() > 0)
    {
      List<? extends WorkflowTask> tasks = WorkflowTask.getTasksForCollection(componentId);

      Map<String, LinkedList<WorkflowTask>> taskGroups = createTaskGroups(tasks);
      String status = mergeTaskGroupStatuses(taskGroups);

      CollectionStatusQuery query = new CollectionStatusQuery(new QueryFactory());
      query.WHERE(query.getComponent().EQ(componentId));

      try (OIterator<? extends CollectionStatus> iterator = query.getIterator())
      {
        CollectionStatus collectionStatus = null;

        if (iterator.hasNext())
        {
          collectionStatus = iterator.next();
          collectionStatus.appLock();
        }
        else
        {
          collectionStatus = new CollectionStatus();
          collectionStatus.setComponent(componentId);
        }

        collectionStatus.setStatus(status);
        collectionStatus.setLastModificationDate(new Date());
        collectionStatus.apply();
      }
    }
  }

  public static Page<CollectionStatus> getUserWorkflowTasks(String statuses, Integer pageNumber, Integer pageSize)
  {
    CollectionStatusQuery query = new CollectionStatusQuery(new QueryFactory());

    if (statuses != null)
    {
      final JSONArray array = new JSONArray(statuses);

      if (array.length() > 0)
      {
        List<Condition> conditions = new LinkedList<Condition>();

        for (int i = 0; i < array.length(); i++)
        {
          conditions.add(query.getStatus().EQ(array.getString(i)));
        }

        query.WHERE(OR.get(conditions.toArray(new Condition[conditions.size()])));
      }
    }

    if (WorkflowTask.isShowUserOnly())
    {
      WorkflowTaskQuery taskQuery = new WorkflowTaskQuery(query.getQueryFactory());
      taskQuery.WHERE(taskQuery.getGeoprismUser().EQ(GeoprismUser.getCurrentUser()));

      query.WHERE(query.getComponent().EQ(taskQuery.getComponent()));
    }
    
    query.ORDER_BY_DESC(query.getLastModificationDate());
    

    try (OIterator<? extends CollectionStatus> iterator = query.getIterator())
    {
      List<CollectionStatus> results = new LinkedList<CollectionStatus>(iterator.getAll());

      return new Page<CollectionStatus>(query.getCount(), pageNumber, pageSize, results);
    }
  }
}
