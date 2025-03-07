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
package gov.geoplatform.uasdm;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.runwaysdk.business.rbac.Authenticate;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.SingleActor;

import gov.geoplatform.uasdm.model.JSONSerializable;
import gov.geoplatform.uasdm.model.Page;
import gov.geoplatform.uasdm.service.SessionEventService;
import net.geoprism.registry.Organization;

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
    object.put(SessionEventLog.EVENTDATE, Util.formatIso8601(this.getEventDate(), true));

    if (!StringUtils.isBlank(this.getOrganizationOid()))
    {
      object.put(SessionEventLog.ORGANIZATION, this.getOrganization().getDisplayLabel().getValue());
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

    if (eventType.equals(SessionEventService.EventType.LOGIN_SUCCESS.name()))
    {
      final SingleActor user = SingleActor.get(userId);
      final UserInfo info = UserInfo.getByUser(user);

      log.setGeoprismUser(user);

      if (info != null)
      {
        try (OIterator<? extends Organization> it = info.getAllOrganization())
        {
          if (it.hasNext())
          {
            log.setOrganization(it.next());
          }
        }
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
