package gov.geoplatform.uasdm;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONObject;

import com.runwaysdk.business.rbac.Authenticate;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import gov.geoplatform.uasdm.model.JSONSerializable;
import gov.geoplatform.uasdm.model.Page;
import net.geoprism.GeoprismUser;
import net.geoprism.SessionEvent;

public class SessionEventLog extends SessionEventLogBase implements JSONSerializable
{
  private static final long serialVersionUID = -13815594;

  public SessionEventLog()
  {
    super();
  }

  @Override
  public JSONObject toJSON()
  {
    final JSONObject object = new JSONObject();
    object.put(SessionEventLog.USERNAME, this.getUsername());
    object.put(SessionEventLog.EVENTTYPE, this.getEventType());
    object.put(SessionEventLog.EVENTDATE, this.getEventDate());

    if (this.getBureauOid() != null && this.getBureauOid().length() > 0)
    {
      object.put(SessionEventLog.BUREAU, this.getBureau().getName());
    }

    return object;
  }

  @Authenticate
  public static void log(String eventType, String username, String userId)
  {

    final SessionEventLog log = new SessionEventLog();
    log.setUsername(username);
    log.setEventDate(new Date());
    log.setEventType(eventType);

    if (eventType.equals(SessionEvent.EventType.LOGIN_SUCCESS.name()))
    {
      final GeoprismUser user = GeoprismUser.get(userId);
      final UserInfo info = UserInfo.getByUser(user);

      log.setGeoprismUser(user);

      if (info != null)
      {
        log.setBureau(info.getBureau());
      }
    }

    log.apply();

  }

  public static Page<SessionEventLog> page(Integer pageNumber, Integer pageSize)
  {
    final SessionEventLogQuery query = new SessionEventLogQuery(new QueryFactory());
    query.ORDER_BY_DESC(query.getEventDate());
    query.restrictRows(pageSize, pageNumber);

    try (final OIterator<? extends SessionEventLog> it = query.getIterator())
    {
      List<SessionEventLog> results = new LinkedList<SessionEventLog>(it.getAll());

      return new Page<SessionEventLog>(query.getCount(), pageNumber, pageSize, results);
    }
  }

}
