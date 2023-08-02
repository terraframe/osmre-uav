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

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.GeoObjectType;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.system.metadata.MdEdge;
import com.runwaysdk.system.metadata.MdVertex;

import gov.geoplatform.uasdm.GenericException;
import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.HierarchyTypeSnapshot;
import net.geoprism.graph.LabeledPropertyGraphSynchronization;
import net.geoprism.graph.LabeledPropertyGraphType;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.graph.TreeStrategyConfiguration;
import net.geoprism.graph.adapter.HTTPConnector;
import net.geoprism.graph.adapter.RegistryConnectorBuilderIF;
import net.geoprism.graph.adapter.RegistryConnectorFactory;
import net.geoprism.graph.adapter.RegistryConnectorIF;
import net.geoprism.graph.adapter.exception.HTTPException;

public class LabeledPropertyGraphSynchronizationService
{
  @Request(RequestType.SESSION)
  public JsonArray getAll(String sessionId)
  {
    return LabeledPropertyGraphSynchronization.getAll();
  }

  @Request(RequestType.SESSION)
  public JsonObject apply(String sessionId, JsonObject json)
  {
    LabeledPropertyGraphSynchronization synchronization = LabeledPropertyGraphSynchronization.fromJSON(json);
    synchronization.apply();

    return synchronization.toJSON();
  }

  @Request(RequestType.SESSION)
  public void remove(String sessionId, String oid)
  {
    LabeledPropertyGraphSynchronization synchronization = LabeledPropertyGraphSynchronization.get(oid);

    if (synchronization != null)
    {
      synchronization.delete();
    }
  }

  @Request(RequestType.SESSION)
  public void execute(String sessionId, String oid)
  {
    // TODO Decide how to handle self-signed HTTPS certs
    RegistryConnectorFactory.setBuilder(new RegistryConnectorBuilderIF()
    {

      @Override
      public RegistryConnectorIF build(String url)
      {

        return new HTTPConnector(url)
        {
          @Override
          public synchronized void initialize()
          {
            try
            {

              CloseableHttpClient httpClient = HttpClients.custom().setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build()).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();
              this.setClient(httpClient);
            }
            catch (Exception e)
            {
              throw new RuntimeException(e);
            }
          }
        };
      }
    });

    try
    {
      LabeledPropertyGraphSynchronization synchronization = LabeledPropertyGraphSynchronization.get(oid);

      if (synchronization != null)
      {
        synchronization.execute();
      }
    }
    catch (ProgrammingErrorException e)
    {
      if (e.getCause() != null && e.getCause() instanceof HTTPException)
      {
        GenericException exception = new GenericException(e);
        exception.setUserMessage("Unable to communicate with the remote server. Please ensure the remote server is available and try again.");
        throw exception;
      }
      else
      {
        throw e;
      }
    }
  }

  @Request(RequestType.SESSION)
  public JsonObject get(String sessionId, String oid)
  {
    return LabeledPropertyGraphSynchronization.get(oid).toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject newInstance(String sessionId)
  {
    return new LabeledPropertyGraphSynchronization().toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject page(String sessionId, JsonObject criteria)
  {
    return LabeledPropertyGraphSynchronization.page(criteria).toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject updateRemoteVersion(String sessionId, String oid, String versionId, Integer versionNumber)
  {
    LabeledPropertyGraphSynchronization synchronization = LabeledPropertyGraphSynchronization.get(oid);
    synchronization.updateRemoteVersion(versionId, versionNumber);

    return synchronization.toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject roots(String sessionId, String oid, Boolean includeRoot)
  {
    LabeledPropertyGraphSynchronization synchronization = LabeledPropertyGraphSynchronization.get(oid);
    LabeledPropertyGraphTypeVersion version = synchronization.getVersion();
    LabeledPropertyGraphType type = version.getGraphType();
    TreeStrategyConfiguration config = (TreeStrategyConfiguration) type.toStrategyConfiguration();
    String typeCode = config.getTypeCode();
    String code = config.getCode();

    GeoObjectTypeSnapshot snapshot = version.getSnapshot(typeCode);
    MdVertex mdVertex = snapshot.getGraphMdVertex();

    JsonArray array = new JsonArray();

    StringBuffer sql = new StringBuffer();
    sql.append("SELECT FROM " + mdVertex.getDbClassName());
    sql.append(" WHERE code = :code");

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(sql.toString());
    query.setParameter("code", code);

    query.getResults().forEach(result -> {
      GeoObject geoObject = snapshot.toGeoObject(result);

      if (includeRoot)
      {
        array.add(geoObject.toJSON());
      }
      else
      {
        array.addAll(this.children(oid, code, geoObject.getUid()));
      }
    });

    JsonArray metadata = new JsonArray();

    version.getTypes().stream().filter(t -> !t.getIsAbstract()).forEach(t -> {
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
    LabeledPropertyGraphSynchronization synchronization = LabeledPropertyGraphSynchronization.get(synchronizationId);
    LabeledPropertyGraphTypeVersion version = synchronization.getVersion();
    GeoObjectTypeSnapshot type = version.getRootType();
    MdVertex mdVertex = type.getGraphMdVertex();
    HierarchyTypeSnapshot hierarchy = version.getHierarchies().get(0);
    MdEdge mdEdge = hierarchy.getGraphMdEdge();

    StringBuffer statement = new StringBuffer();
    statement.append("SELECT FROM " + mdVertex.getDbClassName());
    statement.append(" WHERE oid = :oid");

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString());
    query.setParameter("oid", oid);

    VertexObject result = query.getSingleResult();

    MdVertexDAOIF childVertex = (MdVertexDAOIF) result.getMdClass();

    GeoObjectTypeSnapshot childType = GeoObjectTypeSnapshot.get(version, childVertex);

    GeoObject geoObject = childType.toGeoObject(result);

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

      GeoObjectTypeSnapshot parentType = GeoObjectTypeSnapshot.get(version, parentVertex);

      GeoObject parentObject = parentType.toGeoObject(parent);

      parents.add(parentObject.toJSON());

    });

    return parents;
  }

  @Request(RequestType.SESSION)
  public JsonObject select(String sessionId, String oid, String parentType, String parentId, Boolean includeMetadata)
  {
    LabeledPropertyGraphSynchronization synchronization = LabeledPropertyGraphSynchronization.get(oid);
    LabeledPropertyGraphTypeVersion version = synchronization.getVersion();
    GeoObjectTypeSnapshot snapshot = version.getSnapshot(parentType);

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
    return getObject(version.getSnapshot(typeCode), uid);
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
    LabeledPropertyGraphSynchronization synchronization = LabeledPropertyGraphSynchronization.get(oid);
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

    HierarchyTypeSnapshot hierarchy = version.getHierarchies().get(0);
    MdEdgeDAOIF mdEdge = MdEdgeDAO.get(hierarchy.getGraphMdEdgeOid());

    List<VertexObject> children = parent.getChildren(mdEdge, VertexObject.class);
    HashMap<String, GeoObjectType> cache = new HashMap<String, GeoObjectType>();

    children.stream().map(child -> {
      MdVertexDAOIF childVertex = (MdVertexDAOIF) child.getMdClass();

      if (!cache.containsKey(childVertex.getOid()))
      {
        GeoObjectTypeSnapshot childType = GeoObjectTypeSnapshot.get(version, childVertex);
        GeoObjectType geoObjectType = childType.toGeoObjectType();

        cache.put(childVertex.getOid(), geoObjectType);
      }

      GeoObjectType type = cache.get(childVertex.getOid());

      return GeoObjectTypeSnapshot.toGeoObject(child, type);
    }).sorted((a, b) -> {
      return a.getDisplayLabel().getValue().compareTo(b.getDisplayLabel().getValue());
    }).forEach(geoObject -> {
      array.add(geoObject.toJSON());
    });

    return array;
  }

}
