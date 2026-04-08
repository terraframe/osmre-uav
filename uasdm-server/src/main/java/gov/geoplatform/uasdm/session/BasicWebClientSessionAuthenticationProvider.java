package gov.geoplatform.uasdm.session;

import java.util.Locale;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

import com.runwaysdk.session.InvalidLoginExceptionDTO;
import com.runwaysdk.web.WebClientSession;

import net.geoprism.registry.service.LoginBruteForceGuardService;

@Component
public class BasicWebClientSessionAuthenticationProvider implements AuthenticationProvider
{
  private final LoginBruteForceGuardService loginGuard;

  public BasicWebClientSessionAuthenticationProvider(LoginBruteForceGuardService loginGuard)
  {
    this.loginGuard = loginGuard;
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException
  {
    String username = authentication.getName();
    String password = String.valueOf(authentication.getCredentials());

    try
    {
      Locale[] locales = new Locale[] { Locale.getDefault() };
      WebClientSession clientSession = WebClientSession.createUserSession(username, password, locales);

      UsernamePasswordAuthenticationToken result =
          UsernamePasswordAuthenticationToken.authenticated(
              username,
              null,
              AuthorityUtils.NO_AUTHORITIES);

      result.setDetails(clientSession);
      return result;
    }
    catch (InvalidLoginExceptionDTO e)
    {
      throw new BadCredentialsException("Invalid credentials", e);
    }
  }

  @Override
  public boolean supports(Class<?> authentication)
  {
    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
