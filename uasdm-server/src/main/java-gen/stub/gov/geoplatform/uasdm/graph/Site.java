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
package gov.geoplatform.uasdm.graph;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKTWriter;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdClassDAOIF;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.metadata.MdEdge;
import com.runwaysdk.system.metadata.MdVertex;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.Bureau;
import gov.geoplatform.uasdm.bus.DuplicateSiteException;
import gov.geoplatform.uasdm.model.EdgeType;
import gov.geoplatform.uasdm.model.SiteIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.view.AttributeListType;
import gov.geoplatform.uasdm.view.AttributeType;
import gov.geoplatform.uasdm.view.EqCondition;
import gov.geoplatform.uasdm.view.Option;
import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.HierarchyTypeSnapshot;
import net.geoprism.graph.LabeledPropertyGraphSynchronization;
import net.geoprism.graph.LabeledPropertyGraphSynchronizationQuery;
import net.geoprism.graph.LabeledPropertyGraphType;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.graph.StrategyConfiguration;
import net.geoprism.graph.TreeStrategyConfiguration;

public class Site extends SiteBase implements SiteIF
{
  public static final long    serialVersionUID = 2038909434;

  private static final String CHILD_EDGE       = "gov.geoplatform.uasdm.graph.SiteHasProject";

  public Site()
  {
    super();
  }

  @Override
  public void applyWithParent(UasComponentIF parent)
  {
    boolean isNew = this.isNew();

    if (isNew && isDuplicateSiteName(this.getOid(), this.getName()))
    {
      DuplicateSiteException e = new DuplicateSiteException();
      e.setFolderName(this.getName());

      throw e;
    }

    String bureauOid = this.getBureauOid();

    if (bureauOid != null && bureauOid.length() > 0)
    {
      Bureau bureau = Bureau.get(bureauOid);

      if (bureau == null)
      {
        throw new ProgrammingErrorException("Bad oid for bureau value [" + bureauOid + "]");
      }
    }

    super.applyWithParent(parent);

    // Assign the site to a leaf node in each labeled property graph
    LabeledPropertyGraphSynchronizationQuery query = new LabeledPropertyGraphSynchronizationQuery(new QueryFactory());

    try (OIterator<? extends LabeledPropertyGraphSynchronization> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        LabeledPropertyGraphSynchronization synchronization = iterator.next();

        this.assignHierarchyParents(synchronization);
      }
    }
  }

  public void assignHierarchyParents(LabeledPropertyGraphSynchronization synchronization)
  {
    this.assignHierarchyParents(synchronization, new HashMap<>());
  }

  public void assignHierarchyParents(LabeledPropertyGraphSynchronization synchronization, Map<String, Object> cache)
  {
    LabeledPropertyGraphTypeVersion version = synchronization.getVersion();
    MdEdge synchronizationEdge = SynchronizationEdge.get(version).getGraphEdge();

    MdVertex graphMdVertex = version.getRootType().getGraphMdVertex();

    version.getHierarchies().forEach(hierarchy -> {
      // MdEdge hierarchyEdge = hierarchy.getGraphMdEdge();

      StringBuffer sql = new StringBuffer();
      sql.append("SELECT FROM " + graphMdVertex.getDbClassName());
      sql.append(" WHERE ST_WITHIN(ST_GeomFromText(:wkt), geometry) = true");

      // sql.append(" WHERE outE('" + hierarchyEdge.getDbClassName() +
      // "').size() == 0");
      // sql.append(" AND ST_WITHIN(ST_GeomFromText(:wkt), geometry) = true");

      Point point = this.getGeoPoint();

      GraphQuery<VertexObject> gQuery = new GraphQuery<VertexObject>(sql.toString());
      gQuery.setParameter("wkt", point.toText());

      List<VertexObject> results = gQuery.getResults();

      MdVertex lowestType = null;

      /*
       * Assign the site to the lowest level that contains that site
       */

      // Get the list of types ordered by the reverse of a flattened breadth
      // first visit of the hierarchy tree
      String key = "ordered-" + version.getOid();

      if (!cache.containsKey(key))
      {
        cache.put(key, getOrderedTypes(synchronization, version));
      }

      List<GeoObjectTypeSnapshot> orderedTypes = (List<GeoObjectTypeSnapshot>) cache.get(key);

      for (GeoObjectTypeSnapshot type : orderedTypes)
      {
        // Once a lowest type has been select we don't need to try any other
        // types
        if (lowestType == null)
        {
          MdVertex actualVertex = type.getGraphMdVertex();

          for (VertexObject result : results)
          {
            MdClassDAOIF mdClass = result.getMdClass();

            // If the type of the current object is the same type as the current
            // lowest available type
            // then assign that type as the selected lowest type
            if (lowestType == null && actualVertex.getOid().equals(mdClass.getOid()))
            {
              lowestType = actualVertex;
            }

            // Assign the site to all geo objects of the selected lowest type
            if (lowestType != null && lowestType.getOid().equals(mdClass.getOid()))
            {
              result.addChild(this, synchronizationEdge.definesType()).apply();
            }
          }
        }
      }
    });
  }

  private List<GeoObjectTypeSnapshot> getOrderedTypes(LabeledPropertyGraphSynchronization synchronization, LabeledPropertyGraphTypeVersion version)
  {
    List<GeoObjectTypeSnapshot> types = new LinkedList<GeoObjectTypeSnapshot>();

    LabeledPropertyGraphType graphType = synchronization.getGraphType();
    StrategyConfiguration config = graphType.toStrategyConfiguration();

    if (config instanceof TreeStrategyConfiguration)
    {
      String rootType = ( (TreeStrategyConfiguration) config ).getTypeCode();

      Queue<GeoObjectTypeSnapshot> queue = new LinkedList<>();
      queue.add(version.getSnapshot(rootType));

      while (!queue.isEmpty())
      {
        GeoObjectTypeSnapshot snapshot = queue.poll();

        types.add(snapshot);

        try (OIterator<? extends GeoObjectTypeSnapshot> it = snapshot.getAllChildSnapshot())
        {
          while (it.hasNext())
          {
            queue.offer(it.next());
          }
        }
      }
    }

    Collections.reverse(types);

    return types;
  }

  public List<VertexObject> getHierarchyObjects()
  {
    StringBuffer statement = new StringBuffer();
    statement.append("SELECT EXPAND(in()) FROM :rid");

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString());
    query.setParameter("rid", this.getRID());

    return query.getResults();
  }

  @Override
  public List<AttributeType> attributes()
  {
    AttributeListType attributeType = (AttributeListType) AttributeType.create(this.getMdAttributeDAO(Site.BUREAU));
    attributeType.setOptions(Site.getBureauOptions());

    AttributeType otherAttributeType = AttributeType.create(this.getMdAttributeDAO(Site.OTHERBUREAUTXT));
    otherAttributeType.setCondition(Site.getBureauCondition());
    otherAttributeType.setRequired(true);

    List<AttributeType> list = super.attributes();
    list.add(attributeType);
    list.add(otherAttributeType);
    list.add(AttributeType.create(this.getMdAttributeDAO(Site.GEOPOINT)));

    return list;
  }

  @Override
  public String getSolrIdField()
  {
    return "siteId";
  }

  @Override
  public String getSolrNameField()
  {
    return "siteName";
  }

  @Override
  public Project createDefaultChild()
  {
    return new Project();
  }

  protected boolean needsUpdate()
  {
    return this.isModified(Site.BUREAU);
  }

  @Request
  public static EqCondition getBureauCondition()
  {
    return new EqCondition(Site.BUREAU, Bureau.getByKey(Bureau.OTHER).getOid());
  }

  @Request
  public static List<Option> getBureauOptions()
  {
    return Bureau.getOptions();
  }

  @Override
  public List<AbstractWorkflowTask> getTasks()
  {
    return new LinkedList<AbstractWorkflowTask>();
  }

  @Override
  protected MdEdgeDAOIF getParentMdEdge()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  protected MdEdgeDAOIF getChildMdEdge()
  {
    return MdEdgeDAO.getMdEdgeDAO(CHILD_EDGE);
  }

  @Override
  public List<UasComponentIF> getParents()
  {
    return new LinkedList<UasComponentIF>();
  }

  @Override
  protected String buildProductExpandClause()
  {
    return Site.expandClause();
  }

  public static boolean isDuplicateSiteName(String oid, String name)
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Site.CLASS);

    final StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName());
    statement.append(" WHERE " + NAME + " = :name");

    if (oid != null)
    {
      statement.append(" AND " + OID + " != :oid");
    }

    final GraphQuery<Site> query = new GraphQuery<Site>(statement.toString());
    query.setParameter("name", name);

    if (oid != null)
    {
      query.setParameter("oid", oid);
    }

    return ( query.getResults().size() > 0 );
  }

  public static List<SiteIF> getSites(String conditions, String sort)
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Site.CLASS);

    final TreeMap<String, Object> parameters = new TreeMap<String, Object>();

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName());

    if (conditions != null && conditions.length() > 0)
    {
      JSONObject cObject = new JSONObject(conditions);
      
      boolean isFirst = true;

      /*
       * select from ( traverse out('g_0__operational', 'ha_0__graph_271118')
       * from #474:1 ) where @class = 'site0'
       */
      if (cObject.has("hierarchy") && !cObject.isNull("hierarchy"))
      {
        JSONObject hierarchy = cObject.getJSONObject("hierarchy");
        String oid = hierarchy.getString(LabeledPropertyGraphSynchronization.OID);
        String uid = hierarchy.getString("uid");

        LabeledPropertyGraphSynchronization synchronization = LabeledPropertyGraphSynchronization.get(oid);
        LabeledPropertyGraphTypeVersion version = synchronization.getVersion();
        GeoObjectTypeSnapshot rootType = version.getRootType();
        HierarchyTypeSnapshot hierarchyType = version.getHierarchies().get(0);

        SynchronizationEdge synchronizationEdge = SynchronizationEdge.get(version);
        MdEdge siteEdge = synchronizationEdge.getGraphEdge();

        GraphQuery<VertexObject> query = new GraphQuery<VertexObject>("SELECT FROM " + rootType.getGraphMdVertex().getDbClassName() + " WHERE uuid = :uuid");
        query.setParameter("uuid", uid);

        VertexObject object = query.getSingleResult();

        statement = new StringBuilder();
        statement.append("SELECT FROM (");
        statement.append(" TRAVERSE OUT('" + hierarchyType.getGraphMdEdge().getDbClassName() + "', '" + siteEdge.getDbClassName() + "') FROM :rid");
        statement.append(") WHERE @class = 'site0'");

        parameters.put("rid", object.getRID());
        
        isFirst = false;
      }

      JSONArray array = cObject.getJSONArray("array");

      for (int i = 0; i < array.length(); i++)
      {
        JSONObject condition = array.getJSONObject(i);

        String field = condition.getString("field");

        if (field.equalsIgnoreCase("bounds"))
        {
          // {"_sw":{"lng":-90.55128715174949,"lat":20.209904454730363},"_ne":{"lng":-32.30032930862288,"lat":42.133128793454745}}
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
          statement.append(" ST_WITHIN(geoPoint, ST_GeomFromText(:wkt)) = true");

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
            statement.append(" " + mdAttribute.getColumnName() + " = :" + mdAttribute.getColumnName());

            parameters.put(mdAttribute.getColumnName(), value);
          }

        }
        else
        {
          MdVertexDAOIF mdClass = MdVertexDAO.getMdVertexDAO(UasComponent.CLASS);

          MdAttributeDAOIF mdAttribute = mdClass.definesAttribute(field);

          if (mdAttribute != null)
          {
            String value = condition.getString("value");

            statement.append(isFirst ? " WHERE" : " AND");
            statement.append(" " + mdAttribute.getColumnName() + " = :" + mdAttribute.getColumnName());

            parameters.put(mdAttribute.getColumnName(), value);
          }
        }
        
        isFirst = false;
      }
    }

    if (sort != null && sort.length() > 0)
    {
      MdVertexDAOIF mdClass = MdVertexDAO.getMdVertexDAO(sort.equalsIgnoreCase(Site.BUREAU) ? Site.CLASS : UasComponent.CLASS);

      MdAttributeDAOIF mdAttribute = mdClass.definesAttribute(sort);

      statement.append(" ORDER BY " + mdAttribute.getColumnName());
    }
    else
    {
      MdVertexDAOIF mdClass = MdVertexDAO.getMdVertexDAO(UasComponent.CLASS);

      MdAttributeDAOIF mdAttribute = mdClass.definesAttribute(UasComponent.NAME);

      statement.append(" ORDER BY " + mdAttribute.getColumnName());
    }

    final GraphQuery<SiteIF> query = new GraphQuery<SiteIF>(statement.toString(), parameters);

    return query.getResults();
  }

  public static String expandClause()
  {
    final MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.SITE_HAS_PROJECT);

    return "OUT('" + mdEdge.getDBClassName() + "')." + Project.expandClause();
  }

  public static List<Site> getAll()
  {
    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Site.CLASS);

    GraphQuery<Site> query = new GraphQuery<Site>("SELECT FROM " + mdVertex.getDBClassName());
    return query.getResults();
  }
}
