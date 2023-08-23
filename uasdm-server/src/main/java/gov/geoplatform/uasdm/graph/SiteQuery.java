package gov.geoplatform.uasdm.graph;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTWriter;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDateDAOIF;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.system.metadata.MdEdge;

import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.model.EdgeType;
import gov.geoplatform.uasdm.model.SiteIF;
import net.geoprism.graph.HierarchyTypeSnapshot;
import net.geoprism.graph.LabeledPropertyGraphSynchronization;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;

public class SiteQuery
{
  private static class QueryBucket
  {
    private String      className;

    private JSONArray   conditions;

    private String      edgeName;

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

    public static QueryBucket build(String className, String edgeType, String... fields)
    {

      QueryBucket bucket = new QueryBucket();
      bucket.className = className;

      if (edgeType != null)
      {
        MdEdgeDAOIF mdEdgeDAO = MdEdgeDAO.getMdEdgeDAO(edgeType);
        bucket.edgeName = mdEdgeDAO.getDBClassName();
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

  public SiteQuery(String conditions)
  {
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

  public List<SiteIF> getSites()
  {
    final GraphQuery<SiteIF> query = new GraphQuery<SiteIF>(statement.toString(), parameters);

    return query.getResults();

  }

  private final void process()
  {
    // select EXPAND(
    // in('mission_has_collection0')[folderName = 'mm1'].
    // in('project_has_mission0')[shortName = 'PP1'].
    // in('site_has_project')[bureau = '458f2e85-78f6-4d28-81fd-51cd87000531'])
    // FROM (
    // SELECT FROM (
    // SELECT
    // EXPAND(OUT('site_has_project').OUT('project_has_mission0').OUT('mission_has_collection0'))
    // FROM (
    // SELECT FROM (
    // TRAVERSE OUT('g_0__forestoperational', 'ha_0__graph_340221') FROM #454:0
    // )
    // )
    // )
    // ) where uav = #382:0

    JSONObject cObject = ( conditions != null && conditions.length() > 0 ) ? new JSONObject(conditions) : new JSONObject();

    List<QueryBucket> buckets = getBuckets(cObject);

    int first = buckets.size() - 1;

    for (int i = first; i >= 0; i--)
    {
      String edgeName = buckets.get(i).edgeName;

      if (edgeName != null)
      {
        QueryBucket bucket = buckets.get(i);
        this.statement.append("SELECT EXPAND ( IN('" + bucket.edgeName + "')) FROM (\n");
      }
      else
      {
        this.statement.append("SELECT FROM (\n");
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
      if (found || bucket.hasCondition())
      {
        trimed.add(bucket);

        found = true;
      }
    }

    return trimed;
  }

  private boolean processFromClause(JSONObject cObject, List<QueryBucket> buckets)
  {
    /*
     * select from ( traverse out('g_0__operational', 'ha_0__graph_271118') from
     * #474:1 ) where @class = 'site0'
     */
    if (cObject.has("hierarchy") && !cObject.isNull("hierarchy"))
    {
      JSONObject hierarchy = cObject.getJSONObject("hierarchy");
      String oid = hierarchy.getString(LabeledPropertyGraphSynchronization.OID);
      String uid = hierarchy.getString("uid");

      LabeledPropertyGraphSynchronization synchronization = LabeledPropertyGraphSynchronization.get(oid);
      LabeledPropertyGraphTypeVersion version = synchronization.getVersion();
      HierarchyTypeSnapshot hierarchyType = version.getHierarchies().get(0);

      SynchronizationEdge synchronizationEdge = SynchronizationEdge.get(version);
      MdEdge siteEdge = synchronizationEdge.getGraphEdge();

      VertexObject object = version.getObject(uid);

      boolean hasConditions = buckets.size() > 0 && !buckets.get(0).className.equals(Site.CLASS);

      if (hasConditions)
      {
        statement.append("SELECT EXPAND(");
        int first = buckets.size() - 1;

        boolean isFirst = true;

        for (int i = first; i >= 0; i--)
        {
          String edgeName = buckets.get(i).edgeName;

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

      statement.append("SELECT FROM (\n");
      statement.append(" TRAVERSE OUT('" + hierarchyType.getGraphMdEdge().getDbClassName() + "', '" + siteEdge.getDbClassName() + "') FROM :rid\n");
      statement.append(") WHERE @class = 'site0'\n");

      if (hasConditions)
      {
        statement.append(")\n");
      }

      parameters.put("rid", object.getRID());

      return true;
    }

    String className = buckets.size() > 0 ? buckets.get(0).className : Site.CLASS;

    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(className);

    statement.append("SELECT FROM " + mdVertex.getDBClassName() + "\n");

    return false;
  }

}
