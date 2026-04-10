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

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginRedirectEntryPoint implements AuthenticationEntryPoint
{
  @Override
  public void commence(HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException) throws IOException, ServletException
  {
    Cookie cookie = new Cookie("user", "");
    cookie.setMaxAge(0);

    String path = request.getContextPath();
    cookie.setPath((path == null || path.isEmpty()) ? "/" : path);

    response.addCookie(cookie);
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.addHeader("WWW-Authenticate", "FormBased");

    RequestDispatcher dispatcher = request.getRequestDispatcher("/loginRedirect.jsp");
    dispatcher.forward(request, response);
  }
}
