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
package gov.geoplatform.uasdm.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

import com.google.gson.JsonElement;
import com.runwaysdk.ClientSession;
import com.runwaysdk.RunwayException;
import com.runwaysdk.RunwayExceptionDTO;
import com.runwaysdk.constants.ClientConstants;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.request.ServletRequestIF;
import com.runwaysdk.session.InvalidLoginException;
import com.runwaysdk.session.InvalidLoginExceptionDTO;
import com.runwaysdk.web.WebClientSession;

import gov.geoplatform.uasdm.controller.body.LoginBody;
import net.geoprism.rbac.RoleView;
import net.geoprism.registry.controller.RunwaySpringController;
import net.geoprism.registry.service.LoginGuardServiceIF;
import net.geoprism.registry.service.request.RoleServiceIF;
import net.geoprism.registry.service.request.SessionServiceIF;

@Controller
@Validated
@RequestMapping("/api/session")
public class SessionController extends RunwaySpringController
{
  @Autowired
  protected SessionServiceIF    service;

  @Autowired
  protected RoleServiceIF       roleService;

  @Autowired
  protected LoginGuardServiceIF loginGuard;

  @PostMapping("/login")
  public ResponseEntity<String> login(HttpServletRequest servletReq, @Valid @RequestBody LoginBody body) throws UnsupportedEncodingException
  {
    loginGuard.guardLogin(servletReq);

    String username = body.getUsername();
    String password = body.getPassword();

    if (username != null)
    {
      username = username.trim();
    }

    Locale[] locales = this.getLocales();

    ClientRequestIF clientRequest = loginWithLocales(username, password, locales);

    Set<RoleView> roles = this.roleService.getCurrentRoles(clientRequest.getSessionId(), true);

    JsonElement cookieValue = this.service.getCookieInformation(clientRequest.getSessionId(), roles);

    this.addCookie(cookieValue);

    JsonElement response = this.service.getLoginResponse(clientRequest.getSessionId(), roles);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  public ClientRequestIF loginWithLocales(String username, String password, Locale[] locales)
  {
    try
    {
      WebClientSession clientSession = WebClientSession.createUserSession(username, password, locales);
      ClientRequestIF clientRequest = clientSession.getRequest();

      this.getRequest().getSession().setMaxInactiveInterval(CommonProperties.getSessionTime());
      this.getRequest().getSession().setAttribute(ClientConstants.CLIENTSESSION, clientSession);
      this.getRequest().setAttribute(ClientConstants.CLIENTREQUEST, clientRequest);

      this.service.onLoginSuccess(username, clientRequest.getSessionId());

      return clientRequest;
    }
    catch (RuntimeException e)
    {
      this.service.onLoginFailure(username);

      if (IsInvalidLoginException(e))
      {
        final String xfHeader = this.getRequest().getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || !xfHeader.contains(this.getRequest().getRemoteAddr()))
        {
          loginGuard.loginFailed(this.getRequest().getRemoteAddr());
        }
        else
        {
          loginGuard.loginFailed(xfHeader.split(",")[0]);
        }
      }

      throw e;
    }
  }

  private boolean IsInvalidLoginException(RuntimeException e)
  {
    if (e instanceof InvalidLoginException || e instanceof InvalidLoginExceptionDTO)
    {
      return true;
    }

    if (e instanceof RunwayExceptionDTO)
    {
      return ( (RunwayExceptionDTO) e ).getType().equals(InvalidLoginExceptionDTO.CLASS);
    }

    if (e instanceof RunwayException)
    {
      return ( (RunwayException) e ).getClass().getName().equals(InvalidLoginExceptionDTO.CLASS);
    }

    return false;
  }

  @GetMapping("/logout")
  public RedirectView logout() throws IOException
  {
    ClientSession session = (ClientSession) this.getRequest().getSession().getAttribute(ClientConstants.CLIENTSESSION);

    // process which logs the user out.
    if (session != null)
    {
      session.logout();
    }

    this.getRequest().getSession().removeAttribute(ClientConstants.CLIENTSESSION);
    this.getRequest().getSession().invalidate();

    Cookie cookie = new Cookie("user", "");
    cookie.setMaxAge(0);
    cookie.setPath(this.getRequest().getContextPath());

    this.getResponse().addCookie(cookie);

    final String cp = this.getRequest().getContextPath();
    return new RedirectView(cp.equals("") ? "/" : cp);
  }

  public void createSession(ServletRequestIF req, String sessionId, Locale[] locales)
  {
    WebClientSession clientSession = WebClientSession.getExistingSession(sessionId, locales);
    ClientRequestIF clientRequest = clientSession.getRequest();

    req.getSession().setMaxInactiveInterval(CommonProperties.getSessionTime());
    req.getSession().setAttribute(ClientConstants.CLIENTSESSION, clientSession);
    req.setAttribute(ClientConstants.CLIENTREQUEST, clientRequest);
  }

  public Locale[] getLocales()
  {
    Enumeration<Locale> enumeration = this.getRequest().getLocales();
    List<Locale> locales = new LinkedList<Locale>();

    while (enumeration.hasMoreElements())
    {
      locales.add(enumeration.nextElement());
    }

    return locales.toArray(new Locale[locales.size()]);
  }

  public void addCookie(JsonElement cookieValue) throws UnsupportedEncodingException
  {
    String path = this.getRequest().getContextPath();

    if (path.equals("") || path.length() == 0)
    {
      path = "/";
    }

    final String value = URLEncoder.encode(cookieValue.toString(), "UTF-8");

    Cookie cookie = new Cookie("user", value);
    cookie.setMaxAge(-1);
    cookie.setPath(path);

    this.getResponse().addCookie(cookie);
  }
}
