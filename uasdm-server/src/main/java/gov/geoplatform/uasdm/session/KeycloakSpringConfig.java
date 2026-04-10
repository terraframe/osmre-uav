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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KeycloakSpringConfig
{
  private String registrationId;
  private String clientId;
  private String clientSecret;
  private String clientName;
  private String authorizationGrantType;
  private String redirectUri;
  private String[] scope;
  private String issuerUri;
  private String authorizationUri;
  private String tokenUri;
  private String userInfoUri;
  private String jwkSetUri;
  private String userNameAttributeName;
  private String clientAuthenticationMethod;

  public String getClientAuthenticationMethod()
  {
    return clientAuthenticationMethod;
  }

  public void setClientAuthenticationMethod(String clientAuthenticationMethod)
  {
    this.clientAuthenticationMethod = clientAuthenticationMethod;
  }

  public String getRegistrationId()
  {
    return registrationId;
  }

  public void setRegistrationId(String registrationId)
  {
    this.registrationId = registrationId;
  }

  public String getClientId()
  {
    return clientId;
  }

  public void setClientId(String clientId)
  {
    this.clientId = clientId;
  }

  public String getClientSecret()
  {
    return clientSecret;
  }

  public void setClientSecret(String clientSecret)
  {
    this.clientSecret = clientSecret;
  }

  public String getClientName()
  {
    return clientName;
  }

  public void setClientName(String clientName)
  {
    this.clientName = clientName;
  }

  public String getAuthorizationGrantType()
  {
    return authorizationGrantType;
  }

  public void setAuthorizationGrantType(String authorizationGrantType)
  {
    this.authorizationGrantType = authorizationGrantType;
  }

  public String getRedirectUri()
  {
    return redirectUri;
  }

  public void setRedirectUri(String redirectUri)
  {
    this.redirectUri = redirectUri;
  }

  public String[] getScope()
  {
    return scope;
  }

  public void setScope(String[] scope)
  {
    this.scope = scope;
  }

  public String getIssuerUri()
  {
    return issuerUri;
  }

  public void setIssuerUri(String issuerUri)
  {
    this.issuerUri = issuerUri;
  }

  public String getAuthorizationUri()
  {
    return authorizationUri;
  }

  public void setAuthorizationUri(String authorizationUri)
  {
    this.authorizationUri = authorizationUri;
  }

  public String getTokenUri()
  {
    return tokenUri;
  }

  public void setTokenUri(String tokenUri)
  {
    this.tokenUri = tokenUri;
  }

  public String getUserInfoUri()
  {
    return userInfoUri;
  }

  public void setUserInfoUri(String userInfoUri)
  {
    this.userInfoUri = userInfoUri;
  }

  public String getJwkSetUri()
  {
    return jwkSetUri;
  }

  public void setJwkSetUri(String jwkSetUri)
  {
    this.jwkSetUri = jwkSetUri;
  }

  public String getUserNameAttributeName()
  {
    return userNameAttributeName;
  }

  public void setUserNameAttributeName(String userNameAttributeName)
  {
    this.userNameAttributeName = userNameAttributeName;
  }
}