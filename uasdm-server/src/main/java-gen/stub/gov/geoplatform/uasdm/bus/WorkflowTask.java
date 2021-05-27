/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.geoplatform.uasdm.bus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.ValueObject;
import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.query.AttributeUUID;
import com.runwaysdk.query.Condition;
import com.runwaysdk.query.F;
import com.runwaysdk.query.MAX;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.OR;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.ValueQuery;
import com.runwaysdk.session.Session;

import gov.geoplatform.uasdm.CollectionStatus;
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

  @Override
  public void apply()
  {
    super.apply();

    CollectionStatus.updateStatus(this);
  }

  public static long getUserWorkflowTasksCount(String statuses)
  {
    WorkflowTaskQuery query = new WorkflowTaskQuery(new QueryFactory());

    if (isShowUserOnly())
    {
      query.WHERE(query.getGeoprismUser().EQ(GeoprismUser.getCurrentUser()));
    }

    if (statuses != null)
    {
      List<Condition> conditions = new LinkedList<Condition>();

      final JSONArray array = new JSONArray(statuses);

      for (int i = 0; i < array.length(); i++)
      {
        conditions.add(query.getStatus().EQ(array.getString(i)));
      }

      if (conditions.size() > 0)
      {
        query.AND(OR.get(conditions.toArray(new Condition[conditions.size()])));
      }
    }

    return query.getCount();
  }

  public static Page<WorkflowTask> getUserWorkflowTasks(String statuses, Integer pageNumber, Integer pageSize)
  {
    List<String> components = getUserWorkflowComponents(statuses, pageNumber, pageSize);
    Long count = getUserWorkflowComponentCount(statuses);

    if (components.size() > 0)
    {

      WorkflowTaskQuery query = new WorkflowTaskQuery(new QueryFactory());

      if (isShowUserOnly())
      {
        query.WHERE(query.getGeoprismUser().EQ(GeoprismUser.getCurrentUser()));
      }

      // if (statuses != null)
      // {
      // List<Condition> conditions = new LinkedList<Condition>();
      //
      // final JSONArray array = new JSONArray(statuses);
      //
      // for (int i = 0; i < array.length(); i++)
      // {
      // conditions.add(query.getStatus().EQ(array.getString(i)));
      // }
      //
      // if (conditions.size() > 0)
      // {
      // query.AND(OR.get(conditions.toArray(new
      // Condition[conditions.size()])));
      // }
      // }

      query.AND(query.getComponent().IN(components.toArray(new String[components.size()])));

      // query.ORDER_BY_ASC(query.getComponent());
      query.ORDER_BY_DESC(query.getCreateDate());
      // query.ORDER_BY_ASC(query.getLastUpdateDate());

      // if (pageNumber != null && pageSize != null)
      // {
      // query.restrictRows(pageSize, pageNumber);
      // }
      //
      // final long count = query.getCount();

      try (OIterator<? extends WorkflowTask> iterator = query.getIterator())
      {
        List<WorkflowTask> results = new LinkedList<WorkflowTask>(iterator.getAll());

        return new Page<WorkflowTask>(count, pageNumber, pageSize, results);
      }
    }

    return new Page<WorkflowTask>(count, pageNumber, pageSize, new LinkedList<>());
  }

  public static Long getUserWorkflowComponentCount(String statuses)
  {
    MdBusinessDAOIF mdAbstractWorkflow = MdBusinessDAO.getMdBusinessDAO(AbstractWorkflowTask.CLASS);
    MdBusinessDAOIF mdWorkflow = MdBusinessDAO.getMdBusinessDAO(WorkflowTask.CLASS);
    MdAttributeConcreteDAOIF componentAttribute = mdWorkflow.definesAttribute(WorkflowTask.COMPONENT);
    MdAttributeConcreteDAOIF userAttribute = mdAbstractWorkflow.definesAttribute(AbstractWorkflowTask.GEOPRISMUSER);
    MdAttributeConcreteDAOIF statusAttribute = mdAbstractWorkflow.definesAttribute(AbstractWorkflowTask.STATUS);

    // Runway doesn't support doing a count distinct query so I am circumventing
    // the ORM api and doing a direct query
    StringBuilder builder = new StringBuilder();
    builder.append("SELECT count (DISTINCT workflow_task_2." + componentAttribute.getColumnName() + ") AS component_3\n");
    builder.append("FROM " + mdAbstractWorkflow.getTableName() + " abstract_workflow_task_4,\n");
    builder.append("     " + mdWorkflow.getTableName() + " workflow_task_2 \n");
    builder.append("WHERE workflow_task_2.oid = abstract_workflow_task_4.oid\n");

    if (isShowUserOnly())
    {
      builder.append("AND abstract_workflow_task_4." + userAttribute.getColumnName() + " = '" + GeoprismUser.getCurrentUser().getOid() + "' ");
    }

    if (statuses != null)
    {
      final JSONArray array = new JSONArray(statuses);

      if (array.length() > 0)
      {
        builder.append("AND (");

        for (int i = 0; i < array.length(); i++)
        {
          if (i != 0)
          {
            builder.append(" OR");
          }
          builder.append(" abstract_workflow_task_4." + statusAttribute.getColumnName() + " = '" + array.getString(i) + "'");
        }

        builder.append(") \n");
      }
    }

    try (ResultSet rs = Database.query(builder.toString()))
    {
      if (rs.next())
      {
        long count = rs.getLong(1);

        return count;
      }

      return 0L;
    }
    catch (SQLException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public JSONObject toJSON()
  {
    DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.US);

    ImageryComponent component = this.getImageryComponent();

    final List<UasComponentIF> ancestors = component.getAncestors();
    Collections.reverse(ancestors);

    final JSONArray parents = new JSONArray();

    for (UasComponentIF ancestor : ancestors)
    {
      parents.put(ancestor.getName());
    }

    JSONObject obj = super.toJSON();
    obj.put("uploadId", this.getUploadId());
    obj.put("collection", component.getOid());
    obj.put("collectionLabel", component.getName());
    obj.put("message", this.getMessage());
    obj.put("status", this.getNormalizedStatus());
    obj.put("lastUpdateDate", format.format(this.getLastUpdateDate()));
    obj.put("createDate", format.format(this.getCreateDate()));
    obj.put("type", this.getType());
    obj.put("ancestors", parents);
    
    if (component instanceof gov.geoplatform.uasdm.graph.Collection)
    {
      obj.put("sensorName", ((gov.geoplatform.uasdm.graph.Collection)component).getSensor().getName());
    }

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

  public static boolean isShowUserOnly()
  {
    return Session.getCurrentSession() == null || !Session.getCurrentSession().userHasRole("geoprism.admin.Administrator");
  }

  public static List<String> getUserWorkflowComponents(String statuses, Integer pageNumber, Integer pageSize)
  {
    ValueQuery vQuery = new ValueQuery(new QueryFactory());
    WorkflowTaskQuery query = new WorkflowTaskQuery(vQuery);

    MAX max = F.MAX(query.getCreateDate());

    vQuery.SELECT(query.getComponent(COMPONENT), max);

    if (isShowUserOnly())
    {
      vQuery.WHERE(query.getGeoprismUser().EQ(GeoprismUser.getCurrentUser()));
    }

    if (statuses != null)
    {
      List<Condition> conditions = new LinkedList<Condition>();

      final JSONArray array = new JSONArray(statuses);

      for (int i = 0; i < array.length(); i++)
      {
        conditions.add(query.getStatus().EQ(array.getString(i)));
      }

      if (conditions.size() > 0)
      {
        vQuery.AND(OR.get(conditions.toArray(new Condition[conditions.size()])));
      }
    }

    vQuery.GROUP_BY((AttributeUUID) query.getComponent(COMPONENT));

    vQuery.ORDER_BY_DESC(max);

    if (pageNumber != null && pageSize != null)
    {
      vQuery.restrictRows(pageSize, pageNumber);
    }

    List<String> components = new LinkedList<String>();

    try (OIterator<ValueObject> iterator = vQuery.getIterator(pageSize, pageNumber))
    {
      while (iterator.hasNext())
      {
        ValueObject vObject = iterator.next();
        String component = vObject.getValue(COMPONENT);

        components.add(component);
      }
    }

    return components;
  }

  public static JSONArray getCollectionTasks(String collectionId)
  {
    List<? extends WorkflowTask> tasks = getTasksForCollection(collectionId);

    JSONArray results = new JSONArray();

    for (WorkflowTask task : tasks)
    {
      results.put(task.toJSON());
    }

    return results;

  }

  public static List<? extends WorkflowTask> getTasksForCollection(String collectionId)
  {
    WorkflowTaskQuery query = new WorkflowTaskQuery(new QueryFactory());
    query.WHERE(query.getComponent().EQ(collectionId));
    query.ORDER_BY_DESC(query.getCreateDate());

    try (OIterator<? extends WorkflowTask> iterator = query.getIterator())
    {
      return iterator.getAll();
    }
  }

}
