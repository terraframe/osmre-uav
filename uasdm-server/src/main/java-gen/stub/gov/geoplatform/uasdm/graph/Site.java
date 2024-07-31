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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Point;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdClassDAOIF;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
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
import gov.geoplatform.uasdm.view.AttributeOrganizationType;
import gov.geoplatform.uasdm.view.AttributeType;
import gov.geoplatform.uasdm.view.EqCondition;
import gov.geoplatform.uasdm.view.Option;
import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.LabeledPropertyGraphSynchronization;
import net.geoprism.graph.LabeledPropertyGraphSynchronizationQuery;
import net.geoprism.graph.LabeledPropertyGraphType;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.registry.lpg.StrategyConfiguration;
import net.geoprism.registry.lpg.TreeStrategyConfiguration;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.service.business.LabeledPropertyGraphTypeVersionBusinessServiceIF;
import net.geoprism.spring.ApplicationContextHolder;

public class Site extends SiteBase implements SiteIF
{
  public static final long    serialVersionUID = 2038909434;

  private static final String CHILD_EDGE       = "gov.geoplatform.uasdm.graph.SiteHasProject";

  public Site()
  {
    super();
  }

  @Override
  public ServerOrganization getServerOrganization()
  {
    return ServerOrganization.getByGraphId(this.getObjectValue(ORGANIZATION));
  }

  @Override
  @Transaction
  public void applyWithParent(UasComponentIF parent)
  {
    boolean isNew = this.isNew();

    if (isNew && isDuplicateSiteName(this.getOid(), this.getName()))
    {
      DuplicateSiteException e = new DuplicateSiteException();
      e.setFolderName(this.getName());

      throw e;
    }

    // Organization organization = this.getOrganization();
    //
    // if (bureauOid != null && bureauOid.length() > 0)
    // {
    // Bureau bureau = Bureau.get(bureauOid);
    //
    // if (bureau == null)
    // {
    // throw new ProgrammingErrorException("Bad oid for bureau value [" +
    // bureauOid + "]");
    // }
    // }

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
    LabeledPropertyGraphTypeVersionBusinessServiceIF service = ApplicationContextHolder.getBean(LabeledPropertyGraphTypeVersionBusinessServiceIF.class);

    LabeledPropertyGraphTypeVersion version = synchronization.getVersion();
    MdEdge synchronizationEdge = SynchronizationEdge.get(version).getGraphEdge();

    MdVertex graphMdVertex = service.getRootType(version).getGraphMdVertex();

    service.getHierarchies(version).forEach(hierarchy -> {
      // MdEdge hierarchyEdge = hierarchy.getGraphMdEdge();

      StringBuffer sql = new StringBuffer();
      sql.append("SELECT FROM " + graphMdVertex.getDbClassName());
      sql.append(" WHERE ST_WITHIN(ST_GeomFromText(:wkt), geometry) = true");

      // sql.append(" WHERE outE('" + hierarchyEdge.getDbClassName() +
      // "').size() == 0");
      // sql.append(" AND ST_WITHIN(ST_GeomFromText(:wkt), geometry) = true");

      Point point = this.getGeoPoint();

      if (point != null)
      {

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

              // If the type of the current object is the same type as the
              // current
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
      LabeledPropertyGraphTypeVersionBusinessServiceIF service = ApplicationContextHolder.getBean(LabeledPropertyGraphTypeVersionBusinessServiceIF.class);
      String rootType = ( (TreeStrategyConfiguration) config ).getTypeCode();

      Queue<GeoObjectTypeSnapshot> queue = new LinkedList<>();
      queue.add(service.getSnapshot(version, rootType));

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
    AttributeOrganizationType attributeType = (AttributeOrganizationType) AttributeType.create(this.getMdAttributeDAO(Site.ORGANIZATION));

//    AttributeType otherAttributeType = AttributeType.create(this.getMdAttributeDAO(Site.OTHERBUREAUTXT));
//    otherAttributeType.setCondition(Site.getBureauCondition());
//    otherAttributeType.setRequired(true);

    List<AttributeType> list = super.attributes();
    list.add(attributeType);
//    list.add(otherAttributeType);
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
    return this.isModified(Site.BUREAU) || this.isModified(Site.ORGANIZATION);
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
  protected List<String> buildProductExpandClause()
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
    SiteQuery query = new SiteQuery(conditions);

    return query.getResults().stream().map(a -> (SiteIF) a).collect(Collectors.toList());
  }

  public static List<String> expandClause()
  {
    final MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.SITE_HAS_PROJECT);

    List<String> out = new ArrayList<String>(Arrays.asList("OUT('" + mdEdge.getDBClassName() + "')"));
    out.addAll(Project.expandClause());
    return out;
  }

  public static List<Site> getAll()
  {
    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Site.CLASS);

    GraphQuery<Site> query = new GraphQuery<Site>("SELECT FROM " + mdVertex.getDBClassName());
    return query.getResults();
  }
}
