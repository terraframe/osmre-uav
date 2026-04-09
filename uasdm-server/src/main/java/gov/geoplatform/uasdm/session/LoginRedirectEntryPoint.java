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
