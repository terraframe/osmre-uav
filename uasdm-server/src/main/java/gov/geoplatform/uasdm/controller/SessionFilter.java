/**
 * Copyright (c) 2023 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism(tm).
 *
 * Geoprism(tm) is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * Geoprism(tm) is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package gov.geoplatform.uasdm.controller;

import java.io.IOException;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.runwaysdk.constants.ClientConstants;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.session.InvalidLoginExceptionDTO;
import com.runwaysdk.web.ServletUtility;
import com.runwaysdk.web.WebClientSession;

import net.geoprism.registry.service.LoginBruteForceGuardService;
import net.geoprism.registry.service.request.SessionServiceIF;

@Service
@Primary
public class SessionFilter implements Filter
{
  @Autowired
  protected SessionServiceIF            sessionService;

  @Autowired
  protected LoginBruteForceGuardService loginGuard;

  public void init(FilterConfig filterConfig) throws ServletException
  {
  }

  public void destroy()
  {
  }

  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException
  {
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;

    // response time logging
    req.setAttribute("startTime", (Long) ( new Date().getTime() ));

    HttpSession session = request.getSession();

    WebClientSession clientSession = (WebClientSession) session.getAttribute(ClientConstants.CLIENTSESSION);

    boolean loggedIn = clientSession != null && clientSession.getRequest() != null && clientSession.getRequest().isSessionValid();

    // Check if the endpoint is a public
    if (sessionService.isPublic(request))
    {
      if (!loggedIn)
      {
        Locale[] locales = ServletUtility.getLocales(request);

        clientSession = WebClientSession.createAnonymousSession(locales);

        request.getSession().setMaxInactiveInterval(CommonProperties.getSessionTime());
        request.getSession().setAttribute(ClientConstants.CLIENTSESSION, clientSession);
      }

      req.setAttribute(ClientConstants.CLIENTREQUEST, clientSession.getRequest());

      chain.doFilter(req, res);
      
      return;
    }
    // Its not, check if the user is logged in
    else if (loggedIn && !clientSession.getRequest().isPublicUser())
    {
      try
      {
        req.setAttribute(ClientConstants.CLIENTREQUEST, clientSession.getRequest());

        chain.doFilter(req, res);
      }
      catch (Throwable t)
      {
        while (t.getCause() != null && !t.getCause().equals(t))
        {
          t = t.getCause();
        }

        if (t instanceof RuntimeException)
        {
          throw (RuntimeException) t;
        }
        else
        {
          throw new RuntimeException(t);
        }
      }

      return;
    }
    // Determine if the request is for a public asset like a .png
    else if (sessionService.pathAllowed(request))
    {
      chain.doFilter(req, res);
      return;
    }
    // Determine if the request is using basic authentication
    else if (request.getHeader("Authorization") != null //
        && request.getHeader("Authorization").length() > 0 //
        && request.getHeader("Authorization").toLowerCase().startsWith("basic "))
    {
      loginGuard.guardLogin(request);

      try
      {
        // The credentials are provided in the headers of the request (known as
        // 'basic' http authentication). Useful for scripts and RESTful
        // requests.

        String authHeader = request.getHeader("Authorization");
        String encodedAuth = authHeader.split(" ")[1];
        String decodedAuth = new String(Base64.getDecoder().decode(encodedAuth));

        String username = decodedAuth.split(":")[0];
        String password = decodedAuth.split(":")[1];

        Locale[] locales = ServletUtility.getLocales(request);

        clientSession = WebClientSession.createUserSession(username, password, locales);

        request.getSession().setMaxInactiveInterval(CommonProperties.getSessionTime());
        request.getSession().setAttribute(ClientConstants.CLIENTSESSION, clientSession);

        req.setAttribute(ClientConstants.CLIENTREQUEST, clientSession.getRequest());

        try
        {
          chain.doFilter(req, res);
        }
        finally
        {
          clientSession.logout();
          req.removeAttribute(ClientConstants.CLIENTREQUEST);
          req.removeAttribute(ClientConstants.CLIENTSESSION);
          request.logout();
        }
        return;
      }
      catch (InvalidLoginExceptionDTO e)
      {
        final String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || !xfHeader.contains(request.getRemoteAddr()))
        {
          loginGuard.loginFailed(request.getRemoteAddr());
        }
        else
        {
          loginGuard.loginFailed(xfHeader.split(",")[0]);
        }

        response.setHeader("WWW-Authenticate", "BASIC");
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      }
    }
    // The user does not have access
    else
    {
      Cookie cookie = new Cookie("user", "");
      cookie.setMaxAge(0);
      cookie.setPath(request.getContextPath());

      response.addCookie(cookie);
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.addHeader("WWW-Authenticate", "FormBased");

      //

      // The user is not logged in
      // If we're asynchronous, we want to return a serialized exception
      RequestDispatcher dispatcher = request.getRequestDispatcher("/#login");
      dispatcher.forward(request, response);
    }
  }
}
