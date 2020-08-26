package gov.geoplatform.uasdm.ws;

import org.json.JSONObject;

public class GlobalNotificationMessage extends NotificationMessage
{
  public GlobalNotificationMessage(MessageType type, JSONObject content)
  {
    super(type, content);
  }

  @Override
  public void run()
  {
    NotificationEndpoint.broadcast(this.getMessage());
  }

}
