package gov.geoplatform.uasdm.service;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionIF;

import gov.geoplatform.uasdm.UserInfo;
import gov.geoplatform.uasdm.bus.Bureau;
import net.geoprism.rbac.RoleView;
import net.geoprism.session.SessionService;

@Component
public class IDMSessionService extends SessionService
{
  // TODO : Don't autowire components here because this service is directly
  // instantiated

  /*
   * Expose public endpoints to allow non-logged in users to hit controller
   * endpoints
   */
  @Override
  public Set<String> getPublicEndpoints()
  {
    Set<String> endpoints = super.getPublicEndpoints();
    endpoints.add("api/session/ologin");
    endpoints.add("api/session/login");
    endpoints.add("api/session/logout");
    // endpoints.add("api/invite-user/initiate");
    // endpoints.add("api/invite-user/complete");
    endpoints.add("api/forgotpassword/initiate");
    endpoints.add("api/forgotpassword/complete");
    endpoints.add("api/uasdm-account/inviteComplete");
    endpoints.add("api/uasdm-account/newInstance");
    endpoints.add("project/management");
    endpoints.add("project/configuration");
    // endpoints.add("websocket-notifier/notify");
    return endpoints;
  }

  @Override
  public String getHomeUrl()
  {
    return "/project/management";
  }

  @Override
  public String getLoginUrl()
  {
    return "/project/management#/login";
  }

  @Override
  public void onLoginSuccess(String username, String sessionId)
  {
    new SessionEventService().logSuccessfulLogin(sessionId, username);
  }

  @Override
  public void onLoginFailure(String username)
  {
    new SessionEventService().logFailureLogin(username);
  }

  @Override
  @Request(RequestType.SESSION)
  public JsonElement getLoginResponse(String sessionId, Set<RoleView> roles)
  {
    JsonObject response = super.getLoginResponse(sessionId, roles).getAsJsonObject();
    this.setBureauInformation(response);

    return response;
  }

  @Override
  @Request(RequestType.SESSION)
  public JsonElement getCookieInformation(String sessionId, Set<RoleView> roles)
  {
    JsonObject response = super.getCookieInformation(sessionId, roles).getAsJsonObject();
    this.setBureauInformation(response);

    return response;
  }

  private void setBureauInformation(JsonObject response)
  {
    // Add the bureau to the response object
    SessionIF session = Session.getCurrentSession();

    if (session != null)
    {
      SingleActorDAOIF user = session.getUser();

      if (user != null)
      {
        UserInfo userInfo = UserInfo.getUserInfo(user.getOid());

        if (userInfo != null)
        {
          Bureau bureau = userInfo.getBureau();

          if (bureau != null)
          {
            response.addProperty("bureau", bureau.getOid());
          }
        }
      }
    }
  }
}
