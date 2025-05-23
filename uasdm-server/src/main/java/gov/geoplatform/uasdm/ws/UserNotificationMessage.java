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
package gov.geoplatform.uasdm.ws;

import org.json.JSONObject;

import com.runwaysdk.session.SessionIF;

public class UserNotificationMessage extends NotificationMessage
{
  private String userId;

  public UserNotificationMessage(SessionIF session, MessageType type, JSONObject content)
  {
    this(session.getUser().getOid(), type, content);
  }

  public UserNotificationMessage(String userId, MessageType type, JSONObject content)
  {
    super(type, content);

    this.userId = userId;
  }

  public String getUserId()
  {
    return userId;
  }

  public void setUserId(String userId)
  {
    this.userId = userId;
  }

  @Override
  public void run()
  {
    NotificationEndpoint.broadcast(this.userId, this.getMessage());
  }

}
