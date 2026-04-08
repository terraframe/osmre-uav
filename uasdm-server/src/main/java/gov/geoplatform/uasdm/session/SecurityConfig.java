package gov.geoplatform.uasdm.session;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import jakarta.servlet.http.HttpServletResponse;
import net.geoprism.registry.service.request.SessionServiceIF;

@Configuration
@EnableWebSecurity
public class SecurityConfig
{
  @Bean
  SecurityFilterChain appSecurity(
      HttpSecurity http,
      SessionServiceIF sessionService,
      BridgeSessionFilter bridgeSessionFilter,
      BasicWebClientSessionAuthenticationProvider basicProvider,
      LoginRedirectEntryPoint loginRedirectEntryPoint,
      OidcBootstrapSuccessHandler oidcBootstrapSuccessHandler) throws Exception
  {
    http
      .csrf(csrf -> csrf.disable())

      .authenticationProvider(basicProvider)

      .authorizeHttpRequests(auth -> auth
        .requestMatchers(this.publicMatchers(sessionService)).permitAll()
        .requestMatchers(this.allowedAssetMatchers(sessionService)).permitAll()
        .anyRequest().authenticated()
      )

      .httpBasic(basic -> basic
        .authenticationEntryPoint(loginRedirectEntryPoint)
      )

      .oauth2Login(oauth -> oauth
        .authorizationEndpoint(auth -> auth
          .baseUri("/keycloak/authorization")
        )
        .redirectionEndpoint(redir -> redir
          .baseUri("/keycloak/loginRedirect/*")
        )
        .successHandler(oidcBootstrapSuccessHandler)
        
        .failureHandler((request, response, exception) -> {
          exception.printStackTrace();
          response.sendError(HttpServletResponse.SC_UNAUTHORIZED, exception.getMessage());
        })
      )

      .exceptionHandling(ex -> ex
        .authenticationEntryPoint(loginRedirectEntryPoint)
      )

      .addFilterAfter(bridgeSessionFilter, SecurityContextHolderFilter.class);

    return http.build();
  }

  private RequestMatcher[] publicMatchers(SessionServiceIF sessionService)
  {
    return new RequestMatcher[] {
      request -> sessionService.isPublic(request)
    };
  }

  private RequestMatcher[] allowedAssetMatchers(SessionServiceIF sessionService)
  {
    return new RequestMatcher[] {
      request -> sessionService.pathAllowed(request)
    };
  }
}
