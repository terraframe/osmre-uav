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

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.build.domain.CogTiffPatcher;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.Condition;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.OR;
import com.runwaysdk.query.QueryFactory;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask.TaskActionType;
import gov.geoplatform.uasdm.bus.WorkflowAction;
import gov.geoplatform.uasdm.bus.WorkflowTask;
import gov.geoplatform.uasdm.bus.WorkflowTaskQuery;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.graph.UasComponent;
import gov.geoplatform.uasdm.graph.UserAccessEntity;
import gov.geoplatform.uasdm.model.AbstractWorkflowTaskIF;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.ComponentFacade;
import gov.geoplatform.uasdm.model.JSONSerializable;
import gov.geoplatform.uasdm.model.Page;
import gov.geoplatform.uasdm.model.UasComponentIF;
import net.geoprism.GeoprismUser;

public class CollectionStatus extends CollectionStatusBase implements JSONSerializable
{
  private static final Logger logger           = LoggerFactory.getLogger(CollectionStatus.class);

  private static final long   serialVersionUID = -1406310431;

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

    if (StringUtils.isNotBlank(this.getProductId()))
    {
      Product product = Product.get(this.getProductId());

      if (product != null)
      {
        obj.put("productId", this.getProductId());
        obj.put("productName", product.getProductName());
      }
      else
      {
        logger.error("Collection status with invalid product id for collection [" + component.getOid() + "][" + component.getName() + "][" + this.getProductId() + "]");
      }
    }

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

  public static String mergeTaskActionStatuses(List<WorkflowAction> actions)
  {
    String status = actions.stream().map(action -> action.getActionType()).reduce(TaskActionType.INFO.getType(), (a, b) -> {
      if (a.equals(TaskActionType.ERROR.getType()) || b.equals(TaskActionType.ERROR.getType()))
      {
        return TaskActionType.ERROR.getType();
      }
      if (a.equals(TaskActionType.WARNING.getType()) || b.equals(TaskActionType.WARNING.getType()))
      {
        return TaskActionType.WARNING.getType();
      }

      return TaskActionType.INFO.getType();
    });
    return status;
  }

  private static String getGroupStatus(LinkedList<WorkflowTask> componentTasks)
  {
    return getStatus(componentTasks.getFirst());
  }

  public static String getStatus(WorkflowTask task)
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
      String mergedActionType = mergeTaskActionStatuses(task.getActions());

      if (mergedActionType.equals(TaskActionType.ERROR.getType()))
      {
        // status = "Failed";
        status = "Warning"; // I know this seems weird, but if the action failed
                            // and it actually affected the task then the task's
                            // status would be error or failed.
      }
      else if (mergedActionType.equals(TaskActionType.WARNING.getType()))
      {
        status = "Warning";
      }
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
      updateStatus( ( (WorkflowTask) task ).getComponent(), ( (AbstractWorkflowTask) task ).getProductId());
    }
  }

  public static void updateStatus(String componentId)
  {
    updateStatus(componentId, null);
  }

  @Transaction
  public static void updateStatus(String componentId, String productId)
  {
    if (StringUtils.isBlank(componentId))
      return;

    UasComponentIF component = UasComponent.get(componentId);

    List<? extends WorkflowTask> tasks = null;

    if (component instanceof CollectionIF)
    {
      tasks = WorkflowTask.getTasksForComponent(componentId);
    }
    else if (StringUtils.isNotBlank(productId))
    {
      tasks = WorkflowTask.getTasksForProduct(productId);
    }

    if (tasks != null && tasks.size() > 0)
    {
      Map<String, LinkedList<WorkflowTask>> taskGroups = createTaskGroups(tasks);
      String status = mergeTaskGroupStatuses(taskGroups);

      CollectionStatusQuery query = new CollectionStatusQuery(new QueryFactory());

      if (StringUtils.isNotBlank(productId) && ! ( component instanceof CollectionIF ))
      {
        query.WHERE(query.getProductId().EQ(productId));
      }
      else
      {
        query.WHERE(query.getComponent().EQ(componentId));
      }

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

        if (StringUtils.isNotBlank(productId))
        {
          collectionStatus.setProductId(productId);
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

    boolean showUserOnly = WorkflowTask.isShowUserOnly();
    if (showUserOnly)
    {
      WorkflowTaskQuery taskQuery = new WorkflowTaskQuery(query.getQueryFactory());
      taskQuery.WHERE(taskQuery.getGeoprismUser().EQ(GeoprismUser.getCurrentUser()));

      query.WHERE(query.getComponent().EQ(taskQuery.getComponent()));
    }

    query.ORDER_BY_DESC(query.getLastModificationDate());

    try (OIterator<? extends CollectionStatus> iterator = query.getIterator())
    {
      List<CollectionStatus> results = iterator.getAll().stream().filter(status -> showUserOnly || UserAccessEntity.hasAccess(status.getComponent())).collect(Collectors.toList());

      return new Page<CollectionStatus>(query.getCount(), pageNumber, pageSize, results);
    }
  }

  public static void deleteForProduct(Product product)
  {
    CollectionStatusQuery query = new CollectionStatusQuery(new QueryFactory());
    query.WHERE(query.getProductId().EQ(product.getOid()));
    query.ORDER_BY_DESC(query.getLastModificationDate());

    try (OIterator<? extends CollectionStatus> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        iterator.next().delete();
      }
    }
  }
}
