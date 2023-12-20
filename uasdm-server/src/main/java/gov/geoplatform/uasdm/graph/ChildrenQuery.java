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
package gov.geoplatform.uasdm.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTWriter;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDateDAOIF;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;

import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.model.EdgeType;
import gov.geoplatform.uasdm.model.UasComponentIF;
import net.geoprism.registry.Organization;
import net.geoprism.registry.model.ServerOrganization;

public class ChildrenQuery
{
  private static class QueryBucket
  {
    private String      className;

    private JSONArray   conditions;

    private String      outEdge;

    private Set<String> fields;

    public QueryBucket()
    {
      this.conditions = new JSONArray();
      this.fields = new TreeSet<>();
    }

    public void addCondition(JSONObject condition)
    {
      this.conditions.put(condition);
    }

    public boolean hasCondition()
    {
      return this.conditions.length() > 0;
    }

    public boolean supports(String field)
    {
      return this.fields.contains(field);
    }

    public static QueryBucket build(String className, String outEdge, String... fields)
    {

      QueryBucket bucket = new QueryBucket();
      bucket.className = className;

      if (outEdge != null)
      {
        MdEdgeDAOIF mdEdgeDAO = MdEdgeDAO.getMdEdgeDAO(outEdge);
        bucket.outEdge = mdEdgeDAO.getDBClassName();
      }

      for (String field : fields)
      {
        bucket.fields.add(field);
      }

      return bucket;
    }
  }

  private final TreeMap<String, Object> parameters = new TreeMap<String, Object>();

  private StringBuilder                 statement  = new StringBuilder();

  private String                        conditions;

  private UasComponent                  component;

  public ChildrenQuery(UasComponent component, String conditions)
  {
    this.component = component;
    this.conditions = conditions;

    this.process();
  }

  public StringBuilder getStatement()
  {
    return statement;
  }

  public TreeMap<String, Object> getParameters()
  {
    return parameters;
  }

  public List<UasComponentIF> getResults()
  {
    if (this.component.getType().equals(Collection.CLASS))
    {
      return new LinkedList<>();
    }

    final GraphQuery<UasComponentIF> query = new GraphQuery<UasComponentIF>(statement.toString(), parameters);

    return query.getResults();

  }

  private final void process()
  {
    JSONObject cObject = ( conditions != null && conditions.length() > 0 ) ? new JSONObject(conditions) : new JSONObject();

    List<QueryBucket> buckets = getBuckets(cObject);

    int first = buckets.size() - 1;

    for (int i = first; i >= 0; i--)
    {
      QueryBucket bucket = buckets.get(i);

      String edgeName = bucket.outEdge;

      if (edgeName != null && i != first)
      {
        this.statement.append("SELECT EXPAND ( IN('" + bucket.outEdge + "')) FROM (\n");
      }
      else
      {
        this.statement.append("SELECT DISTINCT * FROM (\n");
      }
    }

    this.processFromClause(cObject, buckets);

    for (int i = 0; i < buckets.size(); i++)
    {
      QueryBucket bucket = buckets.get(i);

      boolean isFirst = true;

      this.statement.append(")");

      // Add the conditions

      JSONArray array = bucket.conditions;

      for (int j = 0; j < array.length(); j++)
      {
        JSONObject condition = array.getJSONObject(j);

        String field = condition.getString("field");

        if (field.equalsIgnoreCase("bounds"))
        {
          JSONObject object = condition.getJSONObject("value");

          JSONObject sw = object.getJSONObject("_sw");
          JSONObject ne = object.getJSONObject("_ne");

          double x1 = sw.getDouble("lng");
          double x2 = ne.getDouble("lng");
          double y1 = sw.getDouble("lat");
          double y2 = ne.getDouble("lat");

          Envelope envelope = new Envelope(x1, x2, y1, y2);
          WKTWriter writer = new WKTWriter();
          GeometryFactory factory = new GeometryFactory();
          Geometry geometry = factory.toGeometry(envelope);

          statement.append(isFirst ? " WHERE" : " AND");
          statement.append(" ST_WITHIN(geoPoint, ST_GeomFromText(:wkt)) = true" + "\n");

          parameters.put("wkt", writer.write(geometry));
        }
        else if (field.equalsIgnoreCase(Site.BUREAU))
        {
          MdVertexDAOIF mdClass = MdVertexDAO.getMdVertexDAO(field.equalsIgnoreCase(Site.BUREAU) ? Site.CLASS : UasComponent.CLASS);

          MdAttributeDAOIF mdAttribute = mdClass.definesAttribute(field);

          if (mdAttribute != null)
          {
            String value = condition.getString("value");

            statement.append(isFirst ? " WHERE" : " AND");
            statement.append(" " + mdAttribute.getColumnName() + " = :" + mdAttribute.getColumnName() + "\n");

            parameters.put(mdAttribute.getColumnName(), value);
          }
        }
        else if (field.equalsIgnoreCase(Site.ORGANIZATION))
        {
          MdVertexDAOIF mdClass = MdVertexDAO.getMdVertexDAO(field.equalsIgnoreCase(Site.ORGANIZATION) ? Site.CLASS : UasComponent.CLASS);

          MdAttributeDAOIF mdAttribute = mdClass.definesAttribute(field);

          if (mdAttribute != null)
          {
            JSONObject value = condition.getJSONObject("value");
            String code = value.getString(Organization.CODE);
            ServerOrganization organization = ServerOrganization.getByCode(code);

            statement.append(isFirst ? " WHERE" : " AND");
            statement.append(" " + mdAttribute.getColumnName() + " = :" + mdAttribute.getColumnName() + "\n");

            parameters.put(mdAttribute.getColumnName(), organization.getGraphOrganization().getRID());
          }
        }        
        else if (field.equalsIgnoreCase(Collection.SENSOR))
        {
          MdVertexDAOIF collection = MdVertexDAO.getMdVertexDAO(Collection.CLASS);
          MdAttributeDAOIF sensor = collection.definesAttribute(Collection.COLLECTIONSENSOR);

          MdVertexDAOIF mdClass = MdVertexDAO.getMdVertexDAO(Sensor.CLASS);
          MdAttributeDAOIF mdAttribute = mdClass.definesAttribute(Sensor.NAME);

          String value = condition.getString("value");

          statement.append(isFirst ? " WHERE" : " AND");
          statement.append(" " + sensor.getColumnName() + "." + mdAttribute.getColumnName() + " = :" + Collection.SENSOR + "\n");

          parameters.put(Collection.SENSOR, value);
        }
        else if (field.equalsIgnoreCase(Collection.UAV))
        {
          MdVertexDAOIF collection = MdVertexDAO.getMdVertexDAO(Collection.CLASS);
          MdAttributeDAOIF sensor = collection.definesAttribute(Collection.UAV);

          MdVertexDAOIF mdClass = MdVertexDAO.getMdVertexDAO(UAV.CLASS);
          MdAttributeDAOIF mdAttribute = mdClass.definesAttribute(UAV.FAANUMBER);

          String value = condition.getString("value");

          statement.append(isFirst ? " WHERE" : " AND");
          statement.append(" " + sensor.getColumnName() + "." + mdAttribute.getColumnName() + " = :" + Collection.UAV + "\n");

          parameters.put(Collection.UAV, value);
        }
        else if (field.equalsIgnoreCase(UAV.PLATFORM))
        {
          MdVertexDAOIF collection = MdVertexDAO.getMdVertexDAO(Collection.CLASS);
          MdAttributeDAOIF sensor = collection.definesAttribute(Collection.UAV);

          MdVertexDAOIF mdClass = MdVertexDAO.getMdVertexDAO(UAV.CLASS);
          MdAttributeDAOIF mdAttribute = mdClass.definesAttribute(UAV.PLATFORM);

          MdVertexDAOIF platform = MdVertexDAO.getMdVertexDAO(Platform.CLASS);
          MdAttributeDAOIF platformName = platform.definesAttribute(Platform.NAME);

          String value = condition.getString("value");

          statement.append(isFirst ? " WHERE" : " AND");
          statement.append(" " + sensor.getColumnName() + "." + mdAttribute.getColumnName() + "." + platformName.getColumnName() + " = :" + UAV.PLATFORM + "\n");

          parameters.put(UAV.PLATFORM, value);
        }
        else if (field.equalsIgnoreCase(UasComponent.OWNER))
        {
          MdVertexDAOIF mdClass = MdVertexDAO.getMdVertexDAO(UasComponent.CLASS);
          MdAttributeDAOIF mdAttribute = mdClass.definesAttribute(UasComponent.OWNER);

          String value = condition.getString("value");

          statement.append(isFirst ? " WHERE" : " AND");
          statement.append(" " + mdAttribute.getColumnName() + " = :" + UasComponent.OWNER + "\n");

          parameters.put(UasComponent.OWNER, value);
        }

        // Date last modified

        else
        {
          MdVertexDAOIF mdClass = MdVertexDAO.getMdVertexDAO(bucket.className);

          MdAttributeDAOIF mdAttribute = mdClass.definesAttribute(field);

          if (mdAttribute != null)
          {
            if (mdAttribute instanceof MdAttributeDateDAOIF)
            {
              Date value = Util.parseIso8601GenericException(condition.getString("value"), false);

              statement.append(isFirst ? " WHERE" : " AND");
              statement.append(" " + mdAttribute.getColumnName() + " = :" + mdAttribute.getColumnName() + "\n");

              parameters.put(mdAttribute.getColumnName(), value);

            }
            else
            {
              String value = condition.getString("value");

              statement.append(isFirst ? " WHERE" : " AND");
              statement.append(" " + mdAttribute.getColumnName() + " = :" + mdAttribute.getColumnName() + "\n");

              parameters.put(mdAttribute.getColumnName(), value);
            }
          }
        }

        isFirst = false;
      }

    }

    MdVertexDAOIF mdClass = MdVertexDAO.getMdVertexDAO(UasComponent.CLASS);

    MdAttributeDAOIF mdAttribute = mdClass.definesAttribute(UasComponent.NAME);

    statement.append(" ORDER BY " + mdAttribute.getColumnName());
  }

  private List<QueryBucket> getBuckets(JSONObject cObject)
  {
    List<QueryBucket> buckets = new ArrayList<QueryBucket>();
    buckets.add(QueryBucket.build(Collection.CLASS, EdgeType.MISSION_HAS_COLLECTION, Collection.COLLECTIONDATE, Collection.SENSOR, Collection.UAV, UAV.PLATFORM, UasComponent.OWNER));
    buckets.add(QueryBucket.build(Mission.CLASS, EdgeType.PROJECT_HAS_MISSION));
    buckets.add(QueryBucket.build(Project.CLASS, EdgeType.SITE_HAS_PROJECT, Project.PROJECTTYPE));
    buckets.add(QueryBucket.build(Site.CLASS, null, Site.BUREAU, "bounds"));

    JSONArray array = cObject.has("array") ? cObject.getJSONArray("array") : new JSONArray();

    for (int i = 0; i < array.length(); i++)
    {
      JSONObject condition = array.getJSONObject(i);

      String field = condition.getString("field");

      for (QueryBucket bucket : buckets)
      {
        if (bucket.supports(field))
        {
          bucket.addCondition(condition);
        }
      }
    }

    List<QueryBucket> trimed = new ArrayList<QueryBucket>();

    boolean found = false;

    for (QueryBucket bucket : buckets)
    {
      if (found || this.includeBucket(bucket))
      {
        trimed.add(bucket);

        found = true;
      }
    }

    // Filter out all buckets which are at an equal or higher level of the
    // selected component
    return trimed.stream().filter(bucket -> {

      if (component.getType().equals(Project.CLASS))
      {
        return Arrays.asList(Mission.CLASS, Collection.CLASS).contains(bucket.className);
      }
      else if (component.getType().equals(Mission.CLASS))
      {
        return Arrays.asList(Collection.CLASS).contains(bucket.className);
      }
      else if (component.getType().equals(Collection.CLASS))
      {
        return false;
      }

      return !component.getType().equals(bucket.className);

    }).collect(Collectors.toList());
  }

  protected boolean includeBucket(QueryBucket bucket)
  {
    if (this.component.getType().equals(Site.CLASS) && bucket.className.equals(Project.CLASS))
    {
      return true;
    }
    else if (this.component.getType().equals(Project.CLASS) && bucket.className.equals(Mission.CLASS))
    {
      return true;
    }
    else if (this.component.getType().equals(Mission.CLASS) && bucket.className.equals(Collection.CLASS))
    {
      return true;
    }
    else if (this.component.getType().equals(Collection.CLASS))
    {
      return false;
    }

    return bucket.hasCondition();
  }

  private void processFromClause(JSONObject cObject, List<QueryBucket> buckets)
  {
    boolean hasConditions = buckets.size() > 0;

    if (hasConditions)
    {
      statement.append("SELECT EXPAND(");
      int first = buckets.size() - 1;

      boolean isFirst = true;

      for (int i = first; i >= 0; i--)
      {
        String edgeName = buckets.get(i).outEdge;

        if (edgeName != null)
        {
          if (!isFirst)
          {
            statement.append(".");
          }

          statement.append("OUT('" + edgeName + "')");

          isFirst = false;
        }
      }

      statement.append(")\n");
      statement.append(" FROM (\n");
    }

    statement.append("SELECT FROM :rid");

    if (hasConditions)
    {
      statement.append(")\n");
    }

    parameters.put("rid", this.component.getRID());
  }

}
