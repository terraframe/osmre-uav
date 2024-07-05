package gov.geoplatform.uasdm.service;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionIF;

import gov.geoplatform.uasdm.GenericException;
import gov.geoplatform.uasdm.graph.UasComponent;
import gov.geoplatform.uasdm.graph.UserAccessEntity;

public class UserAccessEntityService
{
  @Request(RequestType.SESSION)
  public void grantAccess(String sessionId, String componentId, String userId)
  {
    UasComponent component = UasComponent.get(componentId);

    if (component == null)
    {
      GenericException exception = new GenericException();
      exception.setUserMessage("A component does not exist with the id [" + componentId + "]");
      throw exception;
    }

    SessionIF session = Session.getCurrentSession();

    if (session == null || !session.getUser().getOid().equals(component.getOwnerOid()))
    {
      GenericException exception = new GenericException();
      exception.setUserMessage("Only the owner may grant access to a component");
      throw exception;
    }

    UserAccessEntity entity = UserAccessEntity.getOrCreate(userId);

    entity.addUserHasAccessChild(component).apply();
  }

  @Request(RequestType.SESSION)
  public void removeAccess(String sessionId, String componentId, String userId)
  {
    UasComponent component = UasComponent.get(componentId);

    if (component == null)
    {
      GenericException exception = new GenericException();
      exception.setUserMessage("A component does not exist with the id [" + componentId + "]");
      throw exception;
    }

    SessionIF session = Session.getCurrentSession();

    if (session == null || !session.getUser().getOid().equals(component.getOwnerOid()))
    {
      GenericException exception = new GenericException();
      exception.setUserMessage("Only the owner may remove access from a component");
      throw exception;
    }

    UserAccessEntity entity = UserAccessEntity.getOrCreate(userId);

    entity.removeUserHasAccessChild(component);
  }

}
