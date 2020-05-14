package gov.geoplatform.uasdm.ws;

import org.json.JSONObject;

import com.runwaysdk.session.SessionIF;

public class NotificationMessage implements Runnable
{
  private String     userId;

  private JSONObject message;

  public NotificationMessage(SessionIF session, JSONObject message)
  {
    this(session.getUser().getOid(), message);
  }

  public NotificationMessage(String userId, JSONObject message)
  {
    super();
    this.userId = userId;
    this.message = message;
  }

  public String getUserId()
  {
    return userId;
  }

  public void setUserId(String userId)
  {
    this.userId = userId;
  }

  public JSONObject getMessage()
  {
    return message;
  }

  public void setMessage(JSONObject message)
  {
    this.message = message;
  }

  @Override
  public void run()
  {
    NotificationEndpoint.broadcast(this.userId, this.message);
  }

}
