package gov.geoplatform.uasdm.graph;

import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;

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

}
