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
package gov.geoplatform.uasdm.session;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

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
import gov.geoplatform.uasdm.keycloak.KeycloakConstants;
import gov.geoplatform.uasdm.service.request.IDMSessionService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import net.geoprism.account.LocaleSerializer;
import net.geoprism.registry.service.business.RoleBusinessService;

@Component
public class OidcBootstrapSuccessHandler implements AuthenticationSuccessHandler
{
  @Override
  public void onAuthenticationSuccess(HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException
  {
    try
    {
      if (!(authentication.getPrincipal() instanceof OidcUser oidcUser))
      {
        throw new IllegalStateException("Expected OIDC principal but got: " + authentication.getPrincipal());
      }

      JsonObject userJson = buildUserJson(oidcUser);
      Set<String> keycloakRoles = extractRoles(authentication);

      loginAsIdmUser(request, response, userJson, keycloakRoles, request.getLocales());
    }
    catch (Throwable t)
    {
      Locale locale = CommonProperties.getDefaultLocale();

      Locale[] locales = localesToArray(request.getLocales());

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

      String url = "/#/login/" + errorMessage;
      response.sendRedirect(buildRedirectUrl(request, url));
    }
  }

  private JsonObject buildUserJson(OidcUser oidcUser)
  {
    JsonObject userJson = new JsonObject();

    String userId = firstNonBlank(
        oidcUser.getSubject(),
        oidcUser.getClaimAsString("sub"),
        oidcUser.getName());

    userJson.addProperty(KeycloakConstants.USERJSON_USERID, userId);

    String username = firstNonBlank(
        oidcUser.getPreferredUsername(),
        oidcUser.getClaimAsString("preferred_username"),
        userId);

    userJson.addProperty(KeycloakConstants.USERJSON_USERNAME, username);

    addIfNotNull(userJson, KeycloakConstants.USERJSON_EMAIL, oidcUser.getEmail());
    addIfNotNull(userJson, KeycloakConstants.USERJSON_FIRSTNAME, oidcUser.getGivenName());
    addIfNotNull(userJson, KeycloakConstants.USERJSON_LASTNAME, oidcUser.getFamilyName());
    addIfNotNull(userJson, KeycloakConstants.USERJSON_PHONENUMBER, oidcUser.getClaimAsString("phone_number"));

    return userJson;
  }

  private Set<String> extractRoles(Authentication authentication)
  {
    return authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .filter(a -> a != null && !a.isEmpty())
        .map(a -> a.startsWith("ROLE_") ? a.substring("ROLE_".length()) : a)
        .collect(Collectors.toSet());
  }

  private void loginAsIdmUser(HttpServletRequest req, HttpServletResponse resp, JsonObject userJson,
      Set<String> keycloakRoles, Enumeration<Locale> enumLocales) throws IOException
  {
    Locale[] locales = localesToArray(enumLocales);

    final HttpSession session = req.getSession();

    WebClientSession existingClientSession =
        (WebClientSession) session.getAttribute(ClientConstants.CLIENTSESSION);

    if (existingClientSession != null
        && session.getAttribute(ClientConstants.CLIENTREQUEST) instanceof ClientRequestIF clientRequest)
    {
      JsonArray roles =
          JsonParser.parseString(RoleBusinessService.getCurrentRoles(clientRequest.getSessionId())).getAsJsonArray();

      loggedInCookieResponse(req, resp, userJson, roles, clientRequest);
      return;
    }

    WebClientSession anonClientSession = WebClientSession.createAnonymousSession(locales);

    try
    {
      ClientRequestIF clientRequest = anonClientSession.getRequest();

      String jsonRoles = new GsonBuilder().create().toJson(keycloakRoles);

      String cgrSessionJsonString = IDMSessionServiceDTO.keycloakLogin(
          clientRequest,
          userJson.toString(),
          jsonRoles,
          LocaleSerializer.serialize(locales));

      JsonObject cgrSessionJson = JsonParser.parseString(cgrSessionJsonString).getAsJsonObject();
      final String sessionId = cgrSessionJson.get("sessionId").getAsString();

      WebClientSession clientSession = WebClientSession.getExistingSession(sessionId, locales);
      clientRequest = clientSession.getRequest();

      session.setMaxInactiveInterval(CommonProperties.getSessionTime());
      session.setAttribute(ClientConstants.CLIENTSESSION, clientSession);
      session.setAttribute(ClientConstants.CLIENTREQUEST, clientRequest);

      JsonArray roles =
          JsonParser.parseString(RoleBusinessService.getCurrentRoles(clientRequest.getSessionId())).getAsJsonArray();

      loggedInCookieResponse(req, resp, userJson, roles, clientRequest);
    }
    finally
    {
      anonClientSession.logout();
    }
  }

  private void loggedInCookieResponse(HttpServletRequest req, HttpServletResponse resp, JsonObject userJson,
      JsonArray roles, ClientRequestIF clientRequest) throws IOException
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

    JsonArray roleDisplayLabels =
        JsonParser.parseString(RoleBusinessService.getCurrentRoleDisplayLabels(clientRequest.getSessionId()))
            .getAsJsonArray();

    jo.addProperty("loggedIn", true);
    jo.add("roles", roles);
    jo.add("roleDisplayLabels", roleDisplayLabels);
    jo.addProperty("userName", userJson.get(KeycloakConstants.USERJSON_USERNAME).getAsString());
    jo.addProperty("version", new IDMSessionService().getServerVersion());
    jo.add("installedLocales", installedLocalesArr);
    jo.addProperty("externalProfile", true);

    String path = req.getContextPath();

    if (path == null || path.length() == 0)
    {
      path = "/";
    }

    final String value = URLEncoder.encode(jo.toString(), StandardCharsets.UTF_8.name());

    Cookie cookie = new Cookie("user", value);
    cookie.setMaxAge(-1);
    cookie.setPath(path);

    resp.addCookie(cookie);

    String contextPath = req.getContextPath();

    if (AppProperties.IsKeycloakNg2Dev())
    {
      contextPath = "";
    }

    if (contextPath == null || contextPath.length() == 0)
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
    String finalUrl;

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

  private Locale[] localesToArray(Enumeration<Locale> locales)
  {
    List<Locale> llLocales = new LinkedList<Locale>();

    while (locales.hasMoreElements())
    {
      llLocales.add(locales.nextElement());
    }

    return llLocales.toArray(new Locale[0]);
  }

  private void addIfNotNull(JsonObject json, String key, String value)
  {
    if (value != null)
    {
      json.addProperty(key, value);
    }
  }

  private String firstNonBlank(String... values)
  {
    for (String value : values)
    {
      if (value != null && !value.trim().isEmpty())
      {
        return value;
      }
    }

    return null;
  }
}