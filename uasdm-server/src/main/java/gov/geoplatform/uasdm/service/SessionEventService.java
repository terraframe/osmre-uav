package gov.geoplatform.uasdm.service;

import org.json.JSONObject;

import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionIF;

import gov.geoplatform.uasdm.SessionEventLog;
import net.geoprism.GeoprismUser;
import net.geoprism.SessionEvent;

public class SessionEventService
{

  public void handleSessionEvent(SessionEvent event)
  {
    if (event.getType().equals(SessionEvent.EventType.LOGIN_SUCCESS))
    {
      this.logSuccessfulLogin(event.getRequest().getSessionId(), event.getUsername());
    }
    else if (event.getType().equals(SessionEvent.EventType.LOGIN_FAILURE))
    {
      this.logFailureLogin(event.getUsername());
    }
  }

  @Request(RequestType.SESSION)
  private void logSuccessfulLogin(String sessionId, String username)
  {
    final SessionIF session = Session.getCurrentSession();
    final GeoprismUser user = (GeoprismUser) BusinessFacade.get(session.getUser());

    SessionEventLog.log(SessionEvent.EventType.LOGIN_SUCCESS.name(), username, user.getOid());
  }

  @Request
  private void logFailureLogin(String username)
  {
    SessionEventLog.log(SessionEvent.EventType.LOGIN_FAILURE.name(), username, null);
  }

  @Request(RequestType.SESSION)
  public JSONObject page(String sessionId, Integer pageNumber, Integer pageSize)
  {
    return SessionEventLog.page(pageNumber, pageSize).toJSON();
  }

}
