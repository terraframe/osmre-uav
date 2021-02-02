package gov.geoplatform.uasdm.keycloak;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.keycloak.adapters.AuthenticatedActionsHandler;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.PreAuthActionsHandler;
import org.keycloak.adapters.servlet.FilterRequestAuthenticator;
import org.keycloak.adapters.servlet.KeycloakOIDCFilter;
import org.keycloak.adapters.servlet.OIDCFilterSessionStore;
import org.keycloak.adapters.servlet.OIDCServletHttpFacade;
import org.keycloak.adapters.spi.AuthChallenge;
import org.keycloak.adapters.spi.AuthOutcome;
import org.keycloak.adapters.spi.KeycloakAccount;
import org.keycloak.adapters.spi.UserSessionManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.constants.ClientConstants;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.web.WebClientSession;

import gov.geoplatform.uasdm.IDMSessionServiceDTO;
import net.geoprism.RoleViewDTO;
import net.geoprism.account.LocaleSerializer;

public class UASDMKeycloakOIDCFilter extends KeycloakOIDCFilter
{
  private static final Logger logger = LoggerFactory.getLogger(UASDMKeycloakOIDCFilter.class);

  private void logoutAll()
  {
    logger.debug("**************** Logout all sessions");
    
    if (idMapper != null)
    {
      idMapper.clear();
    }
  }

  private void logoutSessions(List<String> ids)
  {
    logger.debug("**************** logoutHttpSessions");
    
    for (String id : ids)
    {
      logger.trace("removed idMapper: " + id);
      
      if (idMapper != null)
      {
        idMapper.removeSession(id);
      }
    }
  }
  
  private void logIn(HttpServletRequestWrapper wrapper, HttpServletResponse resp)
  {
    HttpSession session = wrapper.getSession(false);
    KeycloakAccount account = null;
    if (session != null) {
        account = (KeycloakAccount) session.getAttribute(KeycloakAccount.class.getName());
        if (account == null) {
            account = (KeycloakAccount) wrapper.getAttribute(KeycloakAccount.class.getName());
        }
    }
    if (account == null) {
        account = (KeycloakAccount) wrapper.getAttribute(KeycloakAccount.class.getName());
    }
    
    if (account != null)
    {
      String username = account.getPrincipal().getName();
      Set<String> roles = account.getRoles();
      
      this.loginAsIdmUser(wrapper, resp, username, roles, wrapper.getLocales());
    }
    else
    {
      logger.error("Expected KeycloakAccount to exist at this point. Unable to log user in.");
    }
  }
  
  private void loginAsIdmUser(HttpServletRequestWrapper req, HttpServletResponse resp, String keycloakUsername, Set<String> keycloakRoles, Enumeration<Locale> enumLocales)
  {
    Locale[] locales = this.localesToArray(enumLocales);
    
    WebClientSession clientSession = WebClientSession.createAnonymousSession(locales);
    
    try
    {
      ClientRequestIF clientRequest = clientSession.getRequest();
      
      String jsonRoles = new GsonBuilder().create().toJson(keycloakRoles);
      
      String cgrSessionJsonString = IDMSessionServiceDTO.keycloakLogin(clientRequest, keycloakUsername, jsonRoles, LocaleSerializer.serialize(locales));
      
      JsonObject cgrSessionJson = (JsonObject) JsonParser.parseString(cgrSessionJsonString);
      final String sessionId = cgrSessionJson.get("sessionId").getAsString();
//      final String username = cgrSessionJson.get("username").getAsString();
      
      clientSession.logout();
      
      clientSession = WebClientSession.getExistingSession(sessionId, locales);
      clientRequest = clientSession.getRequest();

      req.getSession().setMaxInactiveInterval(CommonProperties.getSessionTime());
      req.getSession().setAttribute(ClientConstants.CLIENTSESSION, clientSession);
      req.setAttribute(ClientConstants.CLIENTREQUEST, clientRequest);
      
      JsonArray roles = JsonParser.parseString((RoleViewDTO.getCurrentRoles(clientRequest))).getAsJsonArray();

      this.loggedInCookieResponse(req, resp, roles, clientRequest);
    }
    catch (Throwable t)
    {
      
    }
    finally
    {
      clientSession.logout();
    }
  }
  
  private void loggedInCookieResponse(HttpServletRequestWrapper req, HttpServletResponse resp, JsonArray roles, ClientRequestIF clientRequest) throws IOException
  {
    JsonObject jo = new JsonObject();
    
    jo.addProperty("loggedIn", clientRequest.isLoggedIn());
    jo.add("roles", roles);

    String path = req.getContextPath();

    if (path.equals("") || path.length() == 0)
    {
      path = "/";
    }

    final String value = URLEncoder.encode(jo.toString(), "UTF-8");
    
    Cookie cookie = new Cookie("user", value);
    cookie.setMaxAge(-1);
    cookie.setPath(path);

    resp.addCookie(cookie);

    String encoding = ( req.getCharacterEncoding() != null ? req.getCharacterEncoding() : "UTF-8" );

    resp.setStatus(200);
    resp.setContentType("application/json");

    OutputStream ostream = resp.getOutputStream();

    try
    {
      ostream.write(jo.toString().getBytes(encoding));

      ostream.flush();
    }
    finally
    {
      ostream.close();
    }
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException
  {
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;

    if (shouldSkip(request))
    {
      chain.doFilter(req, res);
      return;
    }

    OIDCServletHttpFacade facade = new OIDCServletHttpFacade(request, response);
    KeycloakDeployment deployment = deploymentContext.resolveDeployment(facade);
    if (deployment == null || !deployment.isConfigured())
    {
      logger.debug("KeyCloak integration not configured.");
      chain.doFilter(req, res);
      return;
    }

    PreAuthActionsHandler preActions = new PreAuthActionsHandler(new UserSessionManagement()
    {
      @Override
      public void logoutAll()
      {
        UASDMKeycloakOIDCFilter.this.logoutAll();
      }

      @Override
      public void logoutHttpSessions(List<String> ids)
      {
        UASDMKeycloakOIDCFilter.this.logoutSessions(ids);
      }
    }, deploymentContext, facade);

    if (preActions.handleRequest())
    {
      return;
    }

    nodesRegistrationManagement.tryRegister(deployment);
    OIDCFilterSessionStore tokenStore = new OIDCFilterSessionStore(request, facade, 100000, deployment, idMapper);
    tokenStore.checkCurrentToken();

    FilterRequestAuthenticator authenticator = new FilterRequestAuthenticator(deployment, tokenStore, facade, request, 8443);
    AuthOutcome outcome = authenticator.authenticate();
    if (outcome == AuthOutcome.AUTHENTICATED)
    {
      logger.trace("AUTHENTICATED");
      if (facade.isEnded())
      {
        chain.doFilter(req, res);
        return;
      }
      AuthenticatedActionsHandler actions = new AuthenticatedActionsHandler(deployment, facade);
      if (actions.handledRequest())
      {
//        chain.doFilter(req, res);
        return;
      }
      else
      {
        HttpServletRequestWrapper wrapper = tokenStore.buildWrapper();
        
        logIn(wrapper, response);
        
        chain.doFilter(wrapper, res);
        
        return;
      }
    }

    if (request.getRequestURI().endsWith("keycloak/loginRedirect"))
    {
      AuthChallenge challenge = authenticator.getChallenge();
      if (challenge != null)
      {
        logger.trace("challenge");
        challenge.challenge(facade);
        return;
      }
    }
    
    chain.doFilter(req, res);
  }

  /**
   * Decides whether this {@link Filter} should skip the given
   * {@link HttpServletRequest} based on the configured
   * {@link KeycloakOIDCFilter#skipPattern}. Patterns are matched against the
   * {@link HttpServletRequest#getRequestURI() requestURI} of a request without
   * the context-path. A request for {@code /myapp/index.html} would be tested
   * with {@code /index.html} against the skip pattern. Skipped requests will
   * not be processed further by {@link KeycloakOIDCFilter} and immediately
   * delegated to the {@link FilterChain}.
   *
   * @param request
   *          the request to check
   * @return {@code true} if the request should not be handled, {@code false}
   *         otherwise.
   */
  private boolean shouldSkip(HttpServletRequest request)
  {

    if (skipPattern == null)
    {
      return false;
    }

    String requestPath = request.getRequestURI().substring(request.getContextPath().length());
    return skipPattern.matcher(requestPath).matches();
  }
  
  public Locale[] localesToArray(Enumeration<Locale> locales)
  {
    List<Locale> llLocales = new LinkedList<Locale>();

    while (locales.hasMoreElements())
    {
      llLocales.add(locales.nextElement());
    }

    return llLocales.toArray(new Locale[llLocales.size()]);
  }
}
