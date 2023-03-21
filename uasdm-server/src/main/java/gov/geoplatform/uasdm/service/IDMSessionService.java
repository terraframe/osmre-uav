package gov.geoplatform.uasdm.service;

import java.util.Set;

import org.springframework.stereotype.Component;

import net.geoprism.session.SessionService;

@Component
public class IDMSessionService extends SessionService
{
  // TODO : Don't autowire components here because this service is directly instantiated
  
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
//    endpoints.add("api/invite-user/initiate");
//    endpoints.add("api/invite-user/complete");
    endpoints.add("api/forgotpassword/initiate");
    endpoints.add("api/forgotpassword/complete");
    endpoints.add("api/uasdm-account/inviteComplete");
    endpoints.add("api/uasdm-account/newInstance");
    endpoints.add("project/management");
    endpoints.add("project/configuration");    
//    endpoints.add("websocket-notifier/notify");
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
}
