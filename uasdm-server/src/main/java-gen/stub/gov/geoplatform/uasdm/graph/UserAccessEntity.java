package gov.geoplatform.uasdm.graph;

import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionIF;

import gov.geoplatform.uasdm.model.EdgeType;

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

  public static UserAccessEntity getForUser(String userId)
  {
    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(UserAccessEntity.CLASS);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName());
    statement.append(" WHERE user = :user");

    GraphQuery<UserAccessEntity> query = new GraphQuery<UserAccessEntity>(statement.toString());
    query.setParameter("user", userId);

    return query.getSingleResult();
  }

  public static UserAccessEntity getOrCreate(String userId)
  {
    UserAccessEntity entity = UserAccessEntity.getForUser(userId);

    if (entity == null)
    {
      entity = new UserAccessEntity();
      entity.setUserId(userId);
      entity.apply();
    }

    return entity;
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
      MdVertexDAOIF mdClass = MdVertexDAO.getMdVertexDAO(UasComponent.CLASS);
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

}
