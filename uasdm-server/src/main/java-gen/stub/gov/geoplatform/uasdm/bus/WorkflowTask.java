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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.Pair;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.ValueObject;
import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.query.Condition;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.OR;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.ValueQuery;

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

  public static long getUserWorkflowTasksCount(String statuses)
  {
    WorkflowTaskQuery query = new WorkflowTaskQuery(new QueryFactory());
    query.WHERE(query.getGeoprismUser().EQ(GeoprismUser.getCurrentUser()));

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
      query.WHERE(query.getGeoprismUser().EQ(GeoprismUser.getCurrentUser()));

//    if (statuses != null)
//    {
//      List<Condition> conditions = new LinkedList<Condition>();
//
//      final JSONArray array = new JSONArray(statuses);
//
//      for (int i = 0; i < array.length(); i++)
//      {
//        conditions.add(query.getStatus().EQ(array.getString(i)));
//      }
//
//      if (conditions.size() > 0)
//      {
//        query.AND(OR.get(conditions.toArray(new Condition[conditions.size()])));
//      }
//    }

      query.AND(query.getComponent().IN(components.toArray(new String[components.size()])));

      query.ORDER_BY_ASC(query.getComponent());
      query.ORDER_BY_ASC(query.getLastUpdateDate());

//    if (pageNumber != null && pageSize != null)
//    {
//      query.restrictRows(pageSize, pageNumber);
//    }
//
//    final long count = query.getCount();

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
    // Runway doesn't support doing a count distinct query so I am circumventing
    // the ORM api and doing a direct query
    StringBuilder builder = new StringBuilder();
    builder.append("SELECT count (DISTINCT workflow_task_2.component) AS component_3\n");
    builder.append("FROM abstract_workflow_task abstract_workflow_task_4,\n");
    builder.append("     workflow_task workflow_task_2 \n");
    builder.append("WHERE workflow_task_2.oid = abstract_workflow_task_4.oid\n");
    builder.append("AND abstract_workflow_task_4.geoprism_user = '" + GeoprismUser.getCurrentUser().getOid() + "' ");

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
          builder.append(" abstract_workflow_task_4.status = '" + array.getString(i) + "'");
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

  public static List<String> getUserWorkflowComponents(String statuses, Integer pageNumber, Integer pageSize)
  {
    ValueQuery vQuery = new ValueQuery(new QueryFactory());
    WorkflowTaskQuery query = new WorkflowTaskQuery(vQuery);

    vQuery.SELECT_DISTINCT(query.getComponent());
    vQuery.WHERE(query.getGeoprismUser().EQ(GeoprismUser.getCurrentUser()));

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

//    vQuery.ORDER_BY_ASC(query.getLastUpdateDate());

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
