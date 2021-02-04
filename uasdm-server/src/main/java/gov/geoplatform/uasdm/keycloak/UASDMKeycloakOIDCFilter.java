package gov.geoplatform.uasdm.keycloak;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
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
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

import org.keycloak.adapters.AuthenticatedActionsHandler;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.PreAuthActionsHandler;
import org.keycloak.adapters.servlet.FilterRequestAuthenticator;
import org.keycloak.adapters.servlet.KeycloakOIDCFilter;
import org.keycloak.adapters.servlet.OIDCFilterSessionStore;
import org.keycloak.adapters.servlet.OIDCFilterSessionStore.SerializableKeycloakAccount;
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
import com.runwaysdk.LocalizationFacade;
import com.runwaysdk.constants.ClientConstants;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.web.WebClientSession;

import gov.geoplatform.uasdm.IDMSessionServiceDTO;
import gov.geoplatform.uasdm.KeycloakNoValidRolesExceptionDTO;
import net.geoprism.ClientConfigurationService;
import net.geoprism.RoleViewDTO;
import net.geoprism.SessionFilter;
import net.geoprism.account.LocaleSerializer;

/**
 * 
 * This KeyCloak filter is implemented as per the official Keycloak documentation at:
 * https://www.keycloak.org/docs/latest/securing_apps/#_servlet_filter_adapter
 * 
 * It was decided to use a filter instead of a controller / servlet paradigm for the
 * following reasons:
 * 1. This filter heavily extends and leverages the official Keycloak adapters and
 *    follows keycloak recommended practice
 * 2. As a result, we can support more usecases and leverage any security fixes / etc.
 * 
 * Keep in mind this as a result makes the code a little less clean than if we were
 * to use a servlet or controller paradigm.
 * 
 * @author rrowlands
 *
 */
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
  
  private boolean logIn(HttpServletRequestWrapper wrapper, HttpServletResponse resp)
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
      
      return this.loginAsIdmUser(wrapper, resp, username, roles, wrapper.getLocales());
    }
    else
    {
      logger.error("Expected KeycloakAccount to exist at this point. Unable to log user in.");
      return false;
    }
  }
  
  private boolean loginAsIdmUser(HttpServletRequestWrapper req, HttpServletResponse resp, String keycloakUsername, Set<String> keycloakRoles, Enumeration<Locale> enumLocales)
  {
    Locale[] locales = this.localesToArray(enumLocales);
    
    final HttpSession session = req.getSession();
    WebClientSession existCS = (WebClientSession) session.getAttribute(ClientConstants.CLIENTSESSION);
    if (existCS != null && session.getAttribute(ClientConstants.CLIENTREQUEST) != null && session.getAttribute(ClientConstants.CLIENTREQUEST) instanceof ClientRequestIF)
    {
      return false;
    }
    
    WebClientSession anonClientSession = WebClientSession.createAnonymousSession(locales);
    
    try
    {
      ClientRequestIF clientRequest = anonClientSession.getRequest();
      
      String jsonRoles = new GsonBuilder().create().toJson(keycloakRoles);
      
      String cgrSessionJsonString = IDMSessionServiceDTO.keycloakLogin(clientRequest, keycloakUsername, jsonRoles, LocaleSerializer.serialize(locales));
      
      JsonObject cgrSessionJson = (JsonObject) JsonParser.parseString(cgrSessionJsonString);
      final String sessionId = cgrSessionJson.get("sessionId").getAsString();
//      final String username = cgrSessionJson.get("username").getAsString();
      
      WebClientSession clientSession = WebClientSession.getExistingSession(sessionId, locales);
      clientRequest = clientSession.getRequest();

      req.getSession().setMaxInactiveInterval(CommonProperties.getSessionTime());
      req.getSession().setAttribute(ClientConstants.CLIENTSESSION, clientSession);
      req.getSession().setAttribute(ClientConstants.CLIENTREQUEST, clientRequest);
      
      JsonArray roles = JsonParser.parseString((RoleViewDTO.getCurrentRoles(clientRequest))).getAsJsonArray();

      this.loggedInCookieResponse(req, resp, roles, clientRequest, enumLocales);
      
      return true;
    }
    catch (KeycloakNoValidRolesExceptionDTO e)
    {
      throw e;
    }
    catch (Throwable t)
    {
      throw new RuntimeException(t); // TODO
    }
    finally
    {
      anonClientSession.logout();
    }
  }
  
  private void loggedInCookieResponse(HttpServletRequestWrapper req, HttpServletResponse resp, JsonArray roles, ClientRequestIF clientRequest, Enumeration<Locale> enumLocales) throws IOException
  {
    JsonObject jo = new JsonObject();
    
    Locale[] locales = this.localesToArray(enumLocales);

    JsonArray installedLocalesArr = new JsonArray();
    List<Locale> installedLocales = LocalizationFacade.getInstalledLocales();
    for (Locale loc : installedLocales)
    {
      JsonObject locObj = new JsonObject();
      locObj.addProperty("language", loc.getDisplayLanguage());
      locObj.addProperty("country", loc.getDisplayCountry());
      locObj.addProperty("name", loc.getDisplayName());
      locObj.addProperty("variant", loc.getDisplayVariant());

      installedLocalesArr.add(locObj);
    }
    
    JsonArray roleDisplayLabels = JsonParser.parseString((RoleViewDTO.getCurrentRoleDisplayLabels(clientRequest))).getAsJsonArray();
    
    jo.addProperty("loggedIn", clientRequest.isLoggedIn());
    jo.add("roles", roles);
    jo.add("roleDisplayLabels", roleDisplayLabels);
    jo.addProperty("userName", "test"); // TODO 
    jo.addProperty("version", ClientConfigurationService.getServerVersion());
    jo.add("installedLocales", installedLocalesArr);

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
    
    String contextPath = req.getContextPath();
    if (contextPath.equals("") || contextPath.length() == 0)
    {
      contextPath = "/";
    }
    else
    {
      contextPath = contextPath + "/";
    }
    resp.sendRedirect(contextPath);

//    String encoding = ( req.getCharacterEncoding() != null ? req.getCharacterEncoding() : "UTF-8" );

//    resp.setStatus(200);
//    resp.setContentType("application/json");

//    OutputStream ostream = resp.getOutputStream();
//
//    try
//    {
//      ostream.write(jo.toString().getBytes(encoding));
//
//      ostream.flush();
//    }
//    finally
//    {
//      ostream.close();
//    }
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException
  {
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;

    if (skipPath(request))
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
//        chain.doFilter(req, res);
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
        
        try
        {
          if (logIn(wrapper, response))
          {
            return;
          }
        }
        catch (Exception e)
        {
          tokenStore.logout();
        }
      }
    }

//    if (request.getRequestURI().endsWith("keycloak/loginRedirect"))
//    {
      AuthChallenge challenge = authenticator.getChallenge();
      if (challenge != null)
      {
        logger.trace("challenge");
        challenge.challenge(facade);
        return;
      }
//    }
    
    SerializableKeycloakAccount account = null;
    CookieAwareHttpServletResponse cookieAwareResp = new CookieAwareHttpServletResponse(response);
    if (request.getRequestURI().endsWith("session/logout"))
    {
      HttpSession httpSession = request.getSession(false);
      if (httpSession != null) {
        account = (SerializableKeycloakAccount) httpSession.getAttribute(KeycloakAccount.class.getName());
      }
      
      chain.doFilter(req, cookieAwareResp);
    }
    else
    {
      chain.doFilter(req, res);
    }
    
    if (request.getRequestURI().endsWith("session/logout"))
    {
      if (cookieAwareResp.getCookies().size() > 0)
      {
        for (Cookie cookie : cookieAwareResp.getCookies())
        {
          if (cookie.getName().equals("user"))
          {
            String val = cookie.getValue();
            
            if (val == null || val.length() == 0)
            {
              if (account != null) {
                account.getKeycloakSecurityContext().logout(deployment);
              }
            }
          }
        }
      }
    }
  }
  
  protected boolean skipPath(HttpServletRequest request)
  {
    String uri = stripSlashes(request.getRequestURI());
    
    if (uri.equals("") || uri.equals(stripSlashes(request.getContextPath())))
    {
      return true;
    }
    
    return SessionFilter.isPublic(request) || SessionFilter.pathAllowed(request);
  }
  
  private String stripSlashes(String uri)
  {
    String ret = uri;
    
    if (ret.endsWith("/"))
    {
      ret = ret.substring(0, uri.length() - 1);
    }
    
    if (ret.startsWith("/"))
    {
      ret = ret.substring(1);
    }
    
    return ret;
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
  
  public class CookieAwareHttpServletResponse extends HttpServletResponseWrapper {

    private List<Cookie> cookies = new ArrayList<Cookie>();

    public CookieAwareHttpServletResponse (HttpServletResponse aResponse) {
        super (aResponse);
    }

    @Override
    public void addCookie (Cookie aCookie) {
        cookies.add (aCookie);
        super.addCookie(aCookie);
    }

    public List<Cookie> getCookies () {
        return Collections.unmodifiableList (cookies);
    }

} 
}
