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
package gov.geoplatform.uasdm.service.request;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.graph.VertexObjectDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.metadata.MdEdge;
import com.runwaysdk.system.metadata.MdVertex;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.JobHistory;
import com.runwaysdk.system.scheduler.JobHistoryQuery;

import gov.geoplatform.uasdm.GenericException;
import gov.geoplatform.uasdm.bus.LabeledPropertyGraphSynchronizationJob;
import gov.geoplatform.uasdm.bus.LabeledPropertyGraphSynchronizationJobQuery;
import gov.geoplatform.uasdm.service.business.IDMHierarchyTypeSnapshotBusinessService;
import gov.geoplatform.uasdm.service.business.IDMLabeledPropertyGraphSynchronizationBusinessService;
import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.GraphTypeSnapshot;
import net.geoprism.graph.HierarchyTypeSnapshot;
import net.geoprism.graph.LabeledPropertyGraphSynchronization;
import net.geoprism.graph.LabeledPropertyGraphType;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.registry.JsonCollectors;
import net.geoprism.registry.lpg.TreeStrategyConfiguration;
import net.geoprism.registry.service.business.GeoObjectTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.LabeledPropertyGraphTypeVersionBusinessServiceIF;
import net.geoprism.registry.service.request.LabeledPropertyGraphSynchronizationService;

@Service
@Primary
public class IDMLabeledPropertyGraphSynchronizationService extends LabeledPropertyGraphSynchronizationService
{
  @Autowired
  private IDMLabeledPropertyGraphSynchronizationBusinessService synchronizationService;

  @Autowired
  private LabeledPropertyGraphTypeVersionBusinessServiceIF      versionService;

  @Autowired
  private GeoObjectTypeSnapshotBusinessServiceIF                typeService;

  @Autowired
  private IDMHierarchyTypeSnapshotBusinessService               hierarchyService;

  @Request(RequestType.SESSION)
  public JsonObject roots(String sessionId, String oid, Boolean includeRoot)
  {
    GeoObject parent = null;
    LabeledPropertyGraphSynchronization synchronization = this.synchronizationService.get(oid);
    LabeledPropertyGraphTypeVersion version = synchronization.getVersion();
    LabeledPropertyGraphType type = version.getGraphType();
    TreeStrategyConfiguration config = (TreeStrategyConfiguration) type.toStrategyConfiguration();
    String typeCode = config.getTypeCode();
    String code = config.getCode();

    GeoObjectTypeSnapshot snapshot = this.versionService.getSnapshot(version, typeCode);
    MdVertex mdVertex = snapshot.getGraphMdVertex();

    JsonArray array = new JsonArray();

    StringBuffer sql = new StringBuffer();
    sql.append("SELECT");
    sql.append(" @rid,");
    sql.append(" @version,");
    sql.append(" @class,");
    sql.append(" uid,");
    sql.append(" code,");
    sql.append(" oid,");
    sql.append(" lastUpdateDate,");
    sql.append(" displayLabel,");
    sql.append(" invalid,");
    sql.append(" exists,");
    sql.append(" createDate,");
    sql.append(" seq");
    sql.append(" FROM " + mdVertex.getDbClassName());
    sql.append(" WHERE code = :code");

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(sql.toString(), new TreeMap<>(), VertexObjectDAO.class);
    query.setParameter("code", code);

    List<VertexObject> results = query.getResults();

    for (VertexObject result : results)
    {
      parent = this.typeService.toGeoObject(snapshot, result);

      if (includeRoot)
      {
        array.add(parent.toJSON());
      }
      else
      {
        array.addAll(this.children(oid, parent.getType().getCode(), parent.getUid()));
      }
    }

    JsonArray metadata = new JsonArray();

    this.versionService.getTypes(version).stream().filter(t -> !t.getIsAbstract()).forEach(t -> {
      metadata.add(t.toGeoObjectType().toJSON());
    });

    JsonObject response = new JsonObject();
    response.add("roots", array);
    response.add("metadata", metadata);

    if (parent != null)
    {
      response.addProperty("parent", parent.getUid());
    }

    return response;
  }

  @Request(RequestType.SESSION)
  public JsonObject getObject(String sessionId, String synchronizationId, String oid)
  {
    JsonObject object = new JsonObject();
    LabeledPropertyGraphSynchronization synchronization = this.synchronizationService.get(synchronizationId);
    LabeledPropertyGraphTypeVersion version = synchronization.getVersion();
    GeoObjectTypeSnapshot type = this.versionService.getRootType(version);
    MdVertex mdVertex = type.getGraphMdVertex();
<<<<<<< HEAD
    GraphTypeSnapshot hierarchy = this.versionService.getGraphSnapshots(version).get(0);
=======
    HierarchyTypeSnapshot hierarchy = this.hierarchyService.get(version).get(0);
>>>>>>> refs/remotes/origin/master
    MdEdge mdEdge = hierarchy.getGraphMdEdge();

    StringBuffer statement = new StringBuffer();
    statement.append("SELECT FROM " + mdVertex.getDbClassName());
    statement.append(" WHERE oid = :oid");

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString());
    query.setParameter("oid", oid);

    VertexObject result = query.getSingleResult();

    MdVertexDAOIF childVertex = (MdVertexDAOIF) result.getMdClass();

    GeoObjectTypeSnapshot childType = this.typeService.get(version, childVertex);

    GeoObject geoObject = this.typeService.toGeoObject(childType, result);
    geoObject.setGeometry(null);

    object.add("object", geoObject.toJSON());
    object.add("parents", getParents(version, mdEdge, result));

    return object;
  }

  private JsonArray getParents(LabeledPropertyGraphTypeVersion version, MdEdge mdEdge, VertexObject child)
  {
    return this.getAncestors(version, mdEdge, child, 1, false).stream().map(o -> o.toJSON()).collect(JsonCollectors.toJsonArray());
  }

  public List<GeoObject> getAncestors(LabeledPropertyGraphTypeVersion version, MdEdge mdEdge, VertexObject child, Integer depth, boolean includeRoot)
  {
    StringBuffer s = new StringBuffer();
    s.append("SELECT FROM (");
    s.append(" TRAVERSE in('" + mdEdge.getDbClassName() + "') FROM :rid");
    s.append(") WHERE $depth >= " + depth);

    GraphQuery<VertexObject> squery = new GraphQuery<VertexObject>(s.toString());
    squery.setParameter("rid", child.getRID());

    List<VertexObject> p = squery.getResults();
    Collections.reverse(p);

    // Remove the root object
    if (!includeRoot)
    {
      p.remove(0);
    }

    return p.stream().map(parent -> {
      MdVertexDAOIF parentVertex = (MdVertexDAOIF) parent.getMdClass();

      GeoObjectTypeSnapshot parentType = this.typeService.get(version, parentVertex);

      return this.typeService.toGeoObject(parentType, parent);

    }).collect(Collectors.toList());
  }

  @Request(RequestType.SESSION)
  public JsonObject select(String sessionId, String oid, String parentType, String parentId, Boolean includeMetadata)
  {
    LabeledPropertyGraphSynchronization synchronization = this.synchronizationService.get(oid);
    LabeledPropertyGraphTypeVersion version = synchronization.getVersion();
    GeoObjectTypeSnapshot snapshot = this.versionService.getSnapshot(version, parentType);

    VertexObject parent = this.getObject(snapshot, parentId);

    JsonArray children = this.children(version, parent);

    JsonObject object = new JsonObject();
    object.add("children", children);
    object.addProperty("version", version.getOid());

    String envelope = this.getEnvelope(snapshot, parentId);

    if (!StringUtils.isBlank(envelope))
    {
      object.add("envelope", JsonParser.parseString(envelope));
    }

    if (includeMetadata)
    {
      JsonArray metadata = new JsonArray();

      this.versionService.getTypes(version).stream().filter(t -> !t.getIsAbstract()).forEach(t -> {
        metadata.add(t.toGeoObjectType().toJSON());
      });

      object.add("metadata", metadata);
    }

    return object;
  }

  private VertexObject getObject(LabeledPropertyGraphTypeVersion version, String typeCode, String uid)
  {
    GeoObjectTypeSnapshot snapshot = this.versionService.getSnapshot(version, typeCode);

    return getObject(snapshot, uid);
  }

  private VertexObject getObject(GeoObjectTypeSnapshot snapshot, String uid)
  {
    MdVertex mdVertex = snapshot.getGraphMdVertex();

    StringBuffer sql = new StringBuffer();
    sql.append("SELECT");
    sql.append(" @rid,");
    sql.append(" @version,");
    sql.append(" @class,");
    sql.append(" uid,");
    sql.append(" code,");
    sql.append(" oid,");
    sql.append(" lastUpdateDate,");
    sql.append(" displayLabel,");
    sql.append(" invalid,");
    sql.append(" exists,");
    sql.append(" createDate,");
    sql.append(" seq");
    sql.append(" FROM " + mdVertex.getDbClassName());
    sql.append(" WHERE uid = :uid");

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(sql.toString(), new TreeMap<>(), VertexObjectDAO.class);
    query.setParameter("uid", uid);

    return query.getSingleResult();
  }

  private String getEnvelope(GeoObjectTypeSnapshot snapshot, String uid)
  {
    MdVertex mdVertex = snapshot.getGraphMdVertex();

    StringBuffer sql = new StringBuffer();
    sql.append("SELECT ST_AsGeoJSON(ST_Envelope(geometry)) AS envelope");
    sql.append(" FROM " + mdVertex.getDbClassName());
    sql.append(" WHERE uid = :uid");

    GraphQuery<String> query = new GraphQuery<String>(sql.toString());
    query.setParameter("uid", uid);

    String result = query.getSingleResult();

    if (result != null)
    {
      return result;
    }

    return null;
  }

  private JsonArray children(String oid, String parentType, String parentId)
  {
    LabeledPropertyGraphSynchronization synchronization = this.synchronizationService.get(oid);
    LabeledPropertyGraphTypeVersion version = synchronization.getVersion();

    return this.children(version, parentType, parentId);
  }

  private JsonArray children(LabeledPropertyGraphTypeVersion version, String parentType, String parentId)
  {
    VertexObject parent = this.getObject(version, parentType, parentId);

    return children(version, parent);
  }

  private JsonArray children(LabeledPropertyGraphTypeVersion version, VertexObject parent)
  {
    JsonArray array = new JsonArray();

<<<<<<< HEAD
    GraphTypeSnapshot hierarchy = this.versionService.getGraphSnapshots(version).get(0);
=======
    HierarchyTypeSnapshot hierarchy = this.hierarchyService.get(version).get(0);
>>>>>>> refs/remotes/origin/master
    MdEdgeDAOIF mdEdge = MdEdgeDAO.get(hierarchy.getGraphMdEdgeOid());

    // List<VertexObject> children = parent.getChildren(mdEdge,
    // VertexObject.class);
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT");
    sql.append(" @rid,");
    sql.append(" @version,");
    sql.append(" @class,");
    sql.append(" uid,");
    sql.append(" code,");
    sql.append(" oid,");
    sql.append(" lastUpdateDate,");
    sql.append(" displayLabel,");
    sql.append(" invalid,");
    sql.append(" exists,");
    sql.append(" createDate,");
    sql.append(" seq");
    sql.append(" FROM (");
    sql.append("   SELECT EXPAND(out('" + mdEdge.getDBClassName() + "')) FROM :rid");
    sql.append(" )");

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(sql.toString(), new TreeMap<>(), VertexObjectDAO.class);
    query.setParameter("rid", parent.getRID());

    List<VertexObject> children = query.getResults();

    HashMap<String, GeoObjectType> cache = new HashMap<String, GeoObjectType>();

    children.stream().map(child -> {
      MdVertexDAOIF childVertex = (MdVertexDAOIF) child.getMdClass();

      if (!cache.containsKey(childVertex.getOid()))
      {
        GeoObjectTypeSnapshot childType = this.typeService.get(version, childVertex);
        GeoObjectType geoObjectType = childType.toGeoObjectType();

        cache.put(childVertex.getOid(), geoObjectType);
      }

      GeoObjectType type = cache.get(childVertex.getOid());

      return this.typeService.toGeoObject(child, type);
    }).sorted((a, b) -> {
      return a.getDisplayLabel().getValue().compareTo(b.getDisplayLabel().getValue());
    }).forEach(geoObject -> {
      array.add(geoObject.toJSON());
    });

    return array;
  }

  @Request(RequestType.SESSION)
  public void execute(String sessionId, String oid)
  {
    QueryFactory factory = new QueryFactory();

    LabeledPropertyGraphSynchronizationJobQuery query = new LabeledPropertyGraphSynchronizationJobQuery(factory);
    query.WHERE(query.getSynchronization().EQ(oid));

    JobHistoryQuery q = new JobHistoryQuery(factory);
    q.WHERE(q.getStatus().containsAny(AllJobStatus.NEW, AllJobStatus.QUEUED, AllJobStatus.RUNNING));
    q.AND(q.job(query));

    if (q.getCount() > 0)
    {
      throw new GenericException("This version has already been queued for publishing");
    }

    SingleActorDAOIF currentUser = Session.getCurrentSession().getUser();

    LabeledPropertyGraphSynchronizationJob job = new LabeledPropertyGraphSynchronizationJob();
    job.setRunAsUserId(currentUser.getOid());
    job.setSynchronizationId(oid);
    job.apply();

    job.start();
  }

  @Request(RequestType.SESSION)
  public JsonObject getStatus(String sessionId, String oid)
  {
    JsonObject response = new JsonObject();
    response.addProperty("status", "NON_EXISTENT");

    QueryFactory factory = new QueryFactory();

    LabeledPropertyGraphSynchronizationJobQuery query = new LabeledPropertyGraphSynchronizationJobQuery(factory);
    query.WHERE(query.getSynchronization().EQ(oid));

    JobHistoryQuery q = new JobHistoryQuery(factory);
    q.WHERE(q.job(query));
    q.ORDER_BY_DESC(q.getCreateDate());

    try (OIterator<? extends JobHistory> iterator = q.getIterator())
    {

      if (iterator.hasNext())
      {
        JobHistory job = iterator.next();
        response.addProperty("status", job.getStatus().get(0).getEnumName());

        String error = job.getErrorJson();

        if (!StringUtils.isBlank(error))
        {
          response.add("error", JsonParser.parseString(error));
        }
      }
    }

    return response;
  }
}
