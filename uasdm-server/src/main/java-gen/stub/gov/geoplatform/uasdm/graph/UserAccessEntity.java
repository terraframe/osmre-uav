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

import java.util.List;
import java.util.Optional;

import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionIF;
import com.runwaysdk.system.SingleActor;

import gov.geoplatform.uasdm.GenericException;
import gov.geoplatform.uasdm.model.EdgeType;
import gov.geoplatform.uasdm.model.UasComponentIF;

public class UserAccessEntity extends UserAccessEntityBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 1923961870;

  public UserAccessEntity()
  {
    super();
  }

  public EdgeObject grantAccess(UasComponent component)
  {
    EdgeObject edge = this.addUserHasAccessChild(component);
    edge.apply();

    return edge;
  }

  public static UserAccessEntity getOrCreate(String userId)
  {
    return getOrCreate(SingleActor.get(userId));
  }

  public static UserAccessEntity getOrCreate(SingleActor actor)
  {
    return UserAccessEntity.getForUser(actor).orElseGet(() -> {
      UserAccessEntity entity = new UserAccessEntity();
      entity.setUserId(actor.getOid());
      entity.apply();

      return entity;
    });
  }

  public static void validateAccess(String oid)
  {
    validateAccess(UasComponent.get(oid));
  }

  public static void validateAccess(UasComponentIF component)
  {
    if (component instanceof UasComponent && !hasAccess((UasComponent) component))
    {
      GenericException ex = new GenericException("User does not have access to component [" + component.getOid() + "]");
      ex.setUserMessage("Unable to find a component an id of [" + component.getOid() + "]");
      throw ex;
    }
  }

  public static boolean hasAccess(String componentId)
  {
    return hasAccess(UasComponent.get(componentId));
  }

  public static boolean hasAccess(UasComponent component)
  {
    if (!component.isPrivate())
    {
      return true;
    }

    SessionIF session = Session.getCurrentSession();

    if (session != null)
    {
      SingleActorDAOIF user = session.getUser();

      if (user.getOid().equals(component.getOwnerOid()))
      {
        return true;
      }

      // Add the filter for permissions
      MdEdgeDAOIF accessEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.USER_HAS_ACCESS);

      StringBuilder statement = new StringBuilder();
      statement.append("SELECT in('" + accessEdge.getDBClassName() + "')[user = :owner].size()");
      statement.append(" FROM :rid");

      GraphQuery<Integer> query = new GraphQuery<Integer>(statement.toString());
      query.setParameter("rid", component.getRID());
      query.setParameter("owner", user.getOid());

      return query.getSingleResult() > 0;
    }

    return false;
  }

  public static Optional<UserAccessEntity> getForUser(SingleActor user)
  {
    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(UserAccessEntity.CLASS);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName());
    statement.append(" WHERE user = :user");

    GraphQuery<UserAccessEntity> query = new GraphQuery<UserAccessEntity>(statement.toString());
    query.setParameter("user", user.getOid());

    return Optional.ofNullable(query.getSingleResult());
  }

  public static List<UserAccessEntity> getForComponent(UasComponent component)
  {
    MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.USER_HAS_ACCESS);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT EXPAND(in('" + mdEdge.getDBClassName() + "'))");
    statement.append(" FROM :rid");

    GraphQuery<UserAccessEntity> query = new GraphQuery<UserAccessEntity>(statement.toString());
    query.setParameter("rid", component.getRID());

    return query.getResults();
  }

  public boolean hasEdge(UasComponent component)
  {
    MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.USER_HAS_ACCESS);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT EXPAND(out('" + mdEdge.getDBClassName() + "')[oid = :oid])");
    statement.append(" FROM :rid");

    GraphQuery<UserAccessEntity> query = new GraphQuery<UserAccessEntity>(statement.toString());
    query.setParameter("rid", this.getRID());
    query.setParameter("oid", component.getOid());

    return query.getResults().size() > 0;
  }

}
