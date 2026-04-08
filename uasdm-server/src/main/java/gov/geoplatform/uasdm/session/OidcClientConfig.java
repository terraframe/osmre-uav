package gov.geoplatform.uasdm.session;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.geoplatform.uasdm.AppProperties;

@Configuration
public class OidcClientConfig
{
  @Bean
  public KeycloakSpringConfig keycloakSpringConfig() throws IOException
  {
    ObjectMapper mapper = new ObjectMapper();

    try (InputStream is = AppProperties.getKeycloakSpringConfig())
    {
      if (is == null)
      {
        throw new IllegalStateException("Could not find keycloak-spring.json on the classpath");
      }

      return mapper.readValue(is, KeycloakSpringConfig.class);
    }
  }

  @Bean
  public ClientRegistrationRepository clientRegistrationRepository(KeycloakSpringConfig cfg)
  {
    ClientRegistration keycloak = ClientRegistration.withRegistrationId(cfg.getRegistrationId())
        .clientId(cfg.getClientId())
        .clientSecret(cfg.getClientSecret())
        .clientName(cfg.getClientName())
        .authorizationGrantType(new AuthorizationGrantType(cfg.getAuthorizationGrantType()))
        .clientAuthenticationMethod(new ClientAuthenticationMethod(cfg.getClientAuthenticationMethod()))
        .redirectUri(cfg.getRedirectUri())
        .scope(cfg.getScope())
        .issuerUri(cfg.getIssuerUri())
        .authorizationUri(cfg.getAuthorizationUri())
        .tokenUri(cfg.getTokenUri())
        .userInfoUri(cfg.getUserInfoUri())
        .jwkSetUri(cfg.getJwkSetUri())
        .userNameAttributeName(cfg.getUserNameAttributeName())
        .build();

    return new InMemoryClientRegistrationRepository(keycloak);
  }

  @Bean
  public OAuth2AuthorizedClientService authorizedClientService(
      ClientRegistrationRepository clientRegistrationRepository)
  {
    return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
  }
}