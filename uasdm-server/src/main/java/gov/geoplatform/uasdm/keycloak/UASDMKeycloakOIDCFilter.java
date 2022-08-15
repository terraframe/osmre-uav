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
package gov.geoplatform.uasdm.keycloak;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

import org.apache.http.impl.client.DefaultHttpClient;
import org.keycloak.adapters.AdapterDeploymentContext;
import org.keycloak.adapters.AuthenticatedActionsHandler;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.NodesRegistrationManagement;
import org.keycloak.adapters.OidcKeycloakAccount;
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
import org.keycloak.representations.IDToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.RunwayException;
import com.runwaysdk.constants.ClientConstants;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.web.WebClientSession;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.IDMSessionServiceDTO;
import net.geoprism.ClientConfigurationService;
import net.geoprism.RoleViewDTO;
import net.geoprism.SessionFilter;
import net.geoprism.account.LocaleSerializer;

/**
 * 
 * This KeyCloak filter is implemented as per the official Keycloak
 * documentation at:
 * https://www.keycloak.org/docs/latest/securing_apps/#_servlet_filter_adapter
 * 
 * It was decided to use a filter instead of a controller / servlet paradigm for
 * the following reasons: 1. This filter heavily extends and leverages the
 * official Keycloak adapters and follows keycloak recommended practice 2. As a
 * result, we can support more usecases and leverage any security fixes / etc.
 * 
 * Keep in mind this as a result makes the code a little less clean than if we
 * were to use a servlet or controller paradigm.
 * 
 * @author rrowlands
 *
 */
public class UASDMKeycloakOIDCFilter extends KeycloakOIDCFilter
{
  private static final Logger  logger          = LoggerFactory.getLogger(UASDMKeycloakOIDCFilter.class);

  private static Boolean keycloakEnabled = AppProperties.isKeycloakEnabled();

  /**
   * It is not clear to me at all how to get backchannel logouts to work. Their documentation appears incomplete,
   * since there is conflicting information. The Java Servlet Filter Adapter documentation
   *   (https://www.keycloak.org/docs/latest/securing_apps/#_servlet_filter_adapter) says that if you set
   * the Admin Url in the Keycloak Client configuration it will be used for backchannel logouts. However, this is
   * contradicted by the tooltip withnin the Keycloak app when hovering over (?) for the backchannel logout url,
   * which states that backchannel logouts will not happen unless that url is specified. The documentation does not
   * specify what is a valid URL for that field.
   * 
   * After looking at their source code, it appears that the PreAuthActionsHandler can handle a url with an ending
   * of 'k_logout'. When this is specified in the keycloak client configuration backchannel logout url
   * (as  https://localhost:8443/uasdm/keycloak/k_logout)
   * 
   * The error, which is documented here, in Logs B:
   * https://stackoverflow.com/questions/66002777/spring-boot-single-sign-out-using-keycloak
   * 
   * Occurs, which I am unable to make sense of. This makes me think that the backchannel logout feature is legitimately
   * buggy. I will therefore not be supporting backchannel logouts at this time.
   */
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

  @Override
  public void init(final FilterConfig filterConfig) throws ServletException
  {
    if (keycloakEnabled)
    {
      InputStream config = AppProperties.getKeycloakConfig();
      
      if (config == null)
      {
        logger.error("Unable to find keycloak configuration. Keycloak has been disabled on this server.");
        
        keycloakEnabled = false;
        
        return;
      }
      
      KeycloakDeployment kd = KeycloakDeploymentBuilder.build(config);
      deploymentContext = new AdapterDeploymentContext(kd);

      filterConfig.getServletContext().setAttribute(AdapterDeploymentContext.class.getName(), deploymentContext);
      nodesRegistrationManagement = new NodesRegistrationManagement();
    }
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException
  {
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;

    if (skip(request))
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
//    ((DefaultHttpClient) deployment.getClient()).setParams(null);; // Was required for Keycloak v12.0.2
    AuthOutcome outcome = authenticator.authenticate();
    if (outcome == AuthOutcome.AUTHENTICATED)
    {
      logger.trace("AUTHENTICATED");
      if (facade.isEnded())
      {
        // chain.doFilter(req, res);
        return;
      }
      AuthenticatedActionsHandler actions = new AuthenticatedActionsHandler(deployment, facade);
      if (actions.handledRequest())
      {
        // chain.doFilter(req, res);
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
        catch (Throwable t)
        {
          tokenStore.logout();

          Locale locale = CommonProperties.getDefaultLocale();

          Locale[] locales = this.localesToArray(wrapper.getLocales());

          if (locales.length > 0)
          {
            locale = locales[0];
          }

          String errorMessage = RunwayException.localizeThrowable(t, locale);

          try
          {
            errorMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8.name());
          }
          catch (Throwable t2)
          {
            throw new ProgrammingErrorException(t2);
          }

          String url = "/project/management#/login/" + errorMessage;

          response.sendRedirect(buildRedirectUrl(request, url));

          return;
        }
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

    SerializableKeycloakAccount account = null;
    CookieAwareHttpServletResponse cookieAwareResp = new CookieAwareHttpServletResponse(response);
    if (request.getRequestURI().endsWith("session/logout"))
    {
      HttpSession httpSession = request.getSession(false);
      if (httpSession != null)
      {
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
              if (account != null)
              {
                account.getKeycloakSecurityContext().logout(deployment);
              }
            }
          }
        }
      }
    }
  }

  private boolean logIn(HttpServletRequestWrapper wrapper, HttpServletResponse resp) throws IOException
  {
    HttpSession session = wrapper.getSession(false);
    KeycloakAccount account = null;
    if (session != null)
    {
      account = (KeycloakAccount) session.getAttribute(KeycloakAccount.class.getName());
      if (account == null)
      {
        account = (KeycloakAccount) wrapper.getAttribute(KeycloakAccount.class.getName());
      }
    }
    if (account == null)
    {
      account = (KeycloakAccount) wrapper.getAttribute(KeycloakAccount.class.getName());
    }

    if (account != null)
    {
      JsonObject userJson = new JsonObject();

      String userId = account.getPrincipal().getName();
      userJson.addProperty(KeycloakConstants.USERJSON_USERID, userId);

      String username = userId;
      if (account instanceof OidcKeycloakAccount)
      {
        IDToken token = ( (OidcKeycloakAccount) account ).getKeycloakSecurityContext().getIdToken();

        if (token != null)
        {
          String preferred = token.getPreferredUsername();

          if (preferred != null && preferred.length() > 0)
          {
            username = preferred;
          }

          userJson.addProperty(KeycloakConstants.USERJSON_EMAIL, token.getEmail());

          userJson.addProperty(KeycloakConstants.USERJSON_FIRSTNAME, token.getGivenName());

          userJson.addProperty(KeycloakConstants.USERJSON_LASTNAME, token.getFamilyName());

          userJson.addProperty(KeycloakConstants.USERJSON_PHONENUMBER, token.getPhoneNumber());
        }
      }

      userJson.addProperty(KeycloakConstants.USERJSON_USERNAME, username);

      Set<String> roles = account.getRoles();

      return this.loginAsIdmUser(wrapper, resp, userJson, roles, wrapper.getLocales());
    }
    else
    {
      logger.error("Expected KeycloakAccount to exist at this point. Unable to log user in.");
      return false;
    }
  }

  private boolean loginAsIdmUser(HttpServletRequestWrapper req, HttpServletResponse resp, JsonObject userJson, Set<String> keycloakRoles, Enumeration<Locale> enumLocales) throws IOException
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

      String cgrSessionJsonString = IDMSessionServiceDTO.keycloakLogin(clientRequest, userJson.toString(), jsonRoles, LocaleSerializer.serialize(locales));

      JsonObject cgrSessionJson = (JsonObject) JsonParser.parseString(cgrSessionJsonString);
      final String sessionId = cgrSessionJson.get("sessionId").getAsString();
      // final String username = cgrSessionJson.get("username").getAsString();

      WebClientSession clientSession = WebClientSession.getExistingSession(sessionId, locales);
      clientRequest = clientSession.getRequest();

      req.getSession().setMaxInactiveInterval(CommonProperties.getSessionTime());
      req.getSession().setAttribute(ClientConstants.CLIENTSESSION, clientSession);
      req.getSession().setAttribute(ClientConstants.CLIENTREQUEST, clientRequest);

      JsonArray roles = JsonParser.parseString( ( RoleViewDTO.getCurrentRoles(clientRequest) )).getAsJsonArray();

      this.loggedInCookieResponse(req, resp, userJson, roles, clientRequest, enumLocales);

      return true;
    }
    finally
    {
      anonClientSession.logout();
    }
  }

  private void loggedInCookieResponse(HttpServletRequestWrapper req, HttpServletResponse resp, JsonObject userJson, JsonArray roles, ClientRequestIF clientRequest, Enumeration<Locale> enumLocales) throws IOException
  {
    JsonObject jo = new JsonObject();

    JsonArray installedLocalesArr = new JsonArray();
    Set<Locale> installedLocales = LocalizationFacade.getInstalledLocales();
    for (Locale loc : installedLocales)
    {
      JsonObject locObj = new JsonObject();
      locObj.addProperty("language", loc.getDisplayLanguage());
      locObj.addProperty("country", loc.getDisplayCountry());
      locObj.addProperty("name", loc.getDisplayName());
      locObj.addProperty("variant", loc.getDisplayVariant());

      installedLocalesArr.add(locObj);
    }

    JsonArray roleDisplayLabels = JsonParser.parseString( ( RoleViewDTO.getCurrentRoleDisplayLabels(clientRequest) )).getAsJsonArray();

    jo.addProperty("loggedIn", clientRequest.isLoggedIn());
    jo.add("roles", roles);
    jo.add("roleDisplayLabels", roleDisplayLabels);
    jo.addProperty("userName", userJson.get(KeycloakConstants.USERJSON_USERNAME).getAsString());
    jo.addProperty("version", ClientConfigurationService.getServerVersion());
    jo.add("installedLocales", installedLocalesArr);
    jo.addProperty("externalProfile", true);

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
  }

  private String buildRedirectUrl(HttpServletRequest req, String url)
  {
    String contextPath = req.getContextPath();

    if (contextPath.equals("") || contextPath.length() == 0)
    {
      contextPath = "/";
    }

    String finalUrl = url;

    if (url.startsWith("/"))
    {
      finalUrl = req.getContextPath() + url;
    }
    else
    {
      finalUrl = req.getContextPath() + "/" + url;
    }

    return finalUrl;
  }

  protected boolean skip(HttpServletRequest request)
  {
    // String uri = stripSlashes(request.getRequestURI());
    //
    // if (uri.equals("") || uri.equals(stripSlashes(request.getContextPath())))
    // {
    // return true;
    // }

    return !keycloakEnabled || SessionFilter.isPublic(request) || SessionFilter.pathAllowed(request);
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

  public class CookieAwareHttpServletResponse extends HttpServletResponseWrapper
  {

    private List<Cookie> cookies = new ArrayList<Cookie>();

    public CookieAwareHttpServletResponse(HttpServletResponse aResponse)
    {
      super(aResponse);
    }

    @Override
    public void addCookie(Cookie aCookie)
    {
      cookies.add(aCookie);
      super.addCookie(aCookie);
    }

    public List<Cookie> getCookies()
    {
      return Collections.unmodifiableList(cookies);
    }

  }
}
