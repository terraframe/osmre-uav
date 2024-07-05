package gov.geoplatform.uasdm.service;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

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
    
    UserAccessEntity entity = UserAccessEntity.getOrCreate(userId);
    
    entity.addUserHasAccessChild(component).apply();
  }
  
}
