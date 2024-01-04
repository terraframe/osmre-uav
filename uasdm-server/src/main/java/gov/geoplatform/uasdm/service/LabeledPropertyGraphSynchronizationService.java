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
package gov.geoplatform.uasdm.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
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
import gov.geoplatform.uasdm.service.business.IDMLabeledPropertyGraphSynchronizationBusinessService;
import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.HierarchyTypeSnapshot;
import net.geoprism.graph.LabeledPropertyGraphSynchronization;
import net.geoprism.graph.LabeledPropertyGraphType;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.registry.lpg.TreeStrategyConfiguration;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.service.business.GeoObjectTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.LabeledPropertyGraphTypeVersionBusinessServiceIF;

@Service
public class LabeledPropertyGraphSynchronizationService
{
  @Autowired
  private IDMLabeledPropertyGraphSynchronizationBusinessService synchornizationService;

  @Autowired
  private LabeledPropertyGraphTypeVersionBusinessServiceIF      versionService;

  @Autowired
  private GeoObjectTypeSnapshotBusinessServiceIF                typeService;

  @Request(RequestType.SESSION)
  public JsonArray getAll(String sessionId)
  {
    return this.synchornizationService.getAll();
  }

  @Request(RequestType.SESSION)
  public JsonArray getForOrganization(String sessionId, String orginzationCode)
  {
    ServerOrganization organization = ServerOrganization.getByCode(orginzationCode);

    return this.synchornizationService.getForOrganization(organization);
  }

  @Request(RequestType.SESSION)
  public JsonObject apply(String sessionId, JsonObject json)
  {
    LabeledPropertyGraphSynchronization synchronization = this.synchornizationService.apply(json);

    return synchronization.toJSON();
  }

  @Request(RequestType.SESSION)
  public void remove(String sessionId, String oid)
  {
    LabeledPropertyGraphSynchronization synchronization = this.synchornizationService.get(oid);

    if (synchronization != null)
    {
      this.synchornizationService.delete(synchronization);
    }
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

  @Request(RequestType.SESSION)
  public JsonObject get(String sessionId, String oid)
  {
    return this.synchornizationService.get(oid).toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject newInstance(String sessionId)
  {
    return new LabeledPropertyGraphSynchronization().toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject page(String sessionId, JsonObject criteria)
  {
    return this.synchornizationService.page(criteria).toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject updateRemoteVersion(String sessionId, String oid, String versionId, Integer versionNumber)
  {
    LabeledPropertyGraphSynchronization synchronization = this.synchornizationService.get(oid);

    this.synchornizationService.updateRemoteVersion(synchronization, versionId, versionNumber);

    return synchronization.toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject roots(String sessionId, String oid, Boolean includeRoot)
  {
    LabeledPropertyGraphSynchronization synchronization = this.synchornizationService.get(oid);
    LabeledPropertyGraphTypeVersion version = synchronization.getVersion();
    LabeledPropertyGraphType type = version.getGraphType();
    TreeStrategyConfiguration config = (TreeStrategyConfiguration) type.toStrategyConfiguration();
    String typeCode = config.getTypeCode();
    String code = config.getCode();

    GeoObjectTypeSnapshot snapshot = this.versionService.getSnapshot(version, typeCode);
    MdVertex mdVertex = snapshot.getGraphMdVertex();

    JsonArray array = new JsonArray();

    StringBuffer sql = new StringBuffer();
    sql.append("SELECT FROM " + mdVertex.getDbClassName());
    sql.append(" WHERE code = :code");

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(sql.toString());
    query.setParameter("code", code);

    query.getResults().forEach(result -> {
      GeoObject geoObject = this.typeService.toGeoObject(snapshot, result);

      if (includeRoot)
      {
        array.add(geoObject.toJSON());
      }
      else
      {
        array.addAll(this.children(oid, geoObject.getType().getCode(), geoObject.getUid()));
      }
    });

    JsonArray metadata = new JsonArray();

    this.versionService.getTypes(version).stream().filter(t -> !t.getIsAbstract()).forEach(t -> {
      metadata.add(t.toGeoObjectType().toJSON());
    });

    JsonObject response = new JsonObject();
    response.add("roots", array);
    response.add("metadata", metadata);

    return response;
  }

  @Request(RequestType.SESSION)
  public JsonObject getObject(String sessionId, String synchronizationId, String oid)
  {
    JsonObject object = new JsonObject();
    LabeledPropertyGraphSynchronization synchronization = this.synchornizationService.get(synchronizationId);
    LabeledPropertyGraphTypeVersion version = synchronization.getVersion();
    GeoObjectTypeSnapshot type = this.versionService.getRootType(version);
    MdVertex mdVertex = type.getGraphMdVertex();
    HierarchyTypeSnapshot hierarchy = this.versionService.getHierarchies(version).get(0);
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

    object.add("object", geoObject.toJSON());
    object.add("parents", getParents(version, mdEdge, result));

    return object;
  }

  private JsonArray getParents(LabeledPropertyGraphTypeVersion version, MdEdge mdEdge, VertexObject child)
  {
    JsonArray parents = new JsonArray();

    StringBuffer s = new StringBuffer();
    s.append("SELECT FROM (");
    s.append(" TRAVERSE in('" + mdEdge.getDbClassName() + "') FROM :rid");
    s.append(") WHERE $depth >= 1");

    GraphQuery<VertexObject> squery = new GraphQuery<VertexObject>(s.toString());
    squery.setParameter("rid", child.getRID());

    List<VertexObject> p = squery.getResults();
    Collections.reverse(p);

    // Remove the root object
    // TODO: Figure out a configurable way to determine if the root object
    // should be included or not
    p.remove(0);

    p.forEach(parent -> {
      MdVertexDAOIF parentVertex = (MdVertexDAOIF) parent.getMdClass();

      GeoObjectTypeSnapshot parentType = this.typeService.get(version, parentVertex);

      GeoObject parentObject = this.typeService.toGeoObject(parentType, parent);

      parents.add(parentObject.toJSON());

    });

    return parents;
  }

  @Request(RequestType.SESSION)
  public JsonObject select(String sessionId, String oid, String parentType, String parentId, Boolean includeMetadata)
  {
    LabeledPropertyGraphSynchronization synchronization = this.synchornizationService.get(oid);
    LabeledPropertyGraphTypeVersion version = synchronization.getVersion();
    GeoObjectTypeSnapshot snapshot = this.versionService.getSnapshot(version, parentType);

    VertexObject parent = this.getObject(snapshot, parentId);

    JsonArray children = this.children(version, parent);

    JsonObject object = new JsonObject();
    object.add("children", children);

    if (includeMetadata)
    {
      object.add("metadata", snapshot.toGeoObjectType().toJSON());
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
    sql.append("SELECT FROM " + mdVertex.getDbClassName());
    sql.append(" WHERE uuid = :uuid");

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(sql.toString());
    query.setParameter("uuid", uid);

    return query.getSingleResult();
  }

  private JsonArray children(String oid, String parentType, String parentId)
  {
    LabeledPropertyGraphSynchronization synchronization = this.synchornizationService.get(oid);
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

    HierarchyTypeSnapshot hierarchy = this.versionService.getHierarchies(version).get(0);
    MdEdgeDAOIF mdEdge = MdEdgeDAO.get(hierarchy.getGraphMdEdgeOid());

    List<VertexObject> children = parent.getChildren(mdEdge, VertexObject.class);
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
}
