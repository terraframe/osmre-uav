package gov.geoplatform.uasdm.session;

import java.io.IOException;
import java.util.Locale;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.runwaysdk.constants.ClientConstants;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.web.ServletUtility;
import com.runwaysdk.web.WebClientSession;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import net.geoprism.registry.service.request.SessionServiceIF;

@Component
public class BridgeSessionFilter extends OncePerRequestFilter
{
  private final SessionServiceIF sessionService;

  public BridgeSessionFilter(SessionServiceIF sessionService)
  {
    this.sessionService = sessionService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException
  {
    request.setAttribute("startTime", System.currentTimeMillis());

    HttpSession httpSession = request.getSession();
    WebClientSession clientSession =
        (WebClientSession) httpSession.getAttribute(ClientConstants.CLIENTSESSION);

    boolean loggedIn = clientSession != null
        && clientSession.getRequest() != null
        && clientSession.getRequest().isSessionValid();

    if (sessionService.isPublic(request))
    {
      if (!loggedIn)
      {
        Locale[] locales = ServletUtility.getLocales(request);
        clientSession = WebClientSession.createAnonymousSession(locales);

        httpSession.setMaxInactiveInterval(CommonProperties.getSessionTime());
        httpSession.setAttribute(ClientConstants.CLIENTSESSION, clientSession);
      }

      request.setAttribute(ClientConstants.CLIENTREQUEST, clientSession.getRequest());
      filterChain.doFilter(request, response);
      return;
    }

    if (loggedIn && !clientSession.getRequest().isPublicUser())
    {
      request.setAttribute(ClientConstants.CLIENTREQUEST, clientSession.getRequest());
      filterChain.doFilter(request, response);
      return;
    }

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth != null && auth.isAuthenticated() && auth.getDetails() instanceof WebClientSession basicSession)
    {
      httpSession.setMaxInactiveInterval(CommonProperties.getSessionTime());
      httpSession.setAttribute(ClientConstants.CLIENTSESSION, basicSession);
      request.setAttribute(ClientConstants.CLIENTREQUEST, basicSession.getRequest());

      try
      {
        filterChain.doFilter(request, response);
      }
      finally
      {
        basicSession.logout();
        httpSession.removeAttribute(ClientConstants.CLIENTSESSION);
        request.removeAttribute(ClientConstants.CLIENTREQUEST);
      }
      return;
    }

    filterChain.doFilter(request, response);
  }
}
