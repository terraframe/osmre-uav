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
package gov.geoplatform.uasdm.keycloak;

import java.io.InputStream;
import java.security.Principal;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.AdapterDeploymentContext;
import org.keycloak.adapters.AdapterTokenStore;
import org.keycloak.adapters.AdapterUtils;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.NodesRegistrationManagement;
import org.keycloak.adapters.OAuthRequestAuthenticator;
import org.keycloak.adapters.OIDCHttpFacade;
import org.keycloak.adapters.OidcKeycloakAccount;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.adapters.RequestAuthenticator;
import org.keycloak.adapters.servlet.FilterRequestAuthenticator;
import org.keycloak.adapters.servlet.OIDCServletHttpFacade;
import org.keycloak.adapters.spi.AuthOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.request.RequestDecorator;
import com.runwaysdk.request.ResponseDecorator;
import com.runwaysdk.request.ServletRequestIF;
import com.runwaysdk.request.ServletResponseIF;

import gov.geoplatform.uasdm.IDMSessionService;
import gov.geoplatform.uasdm.IDMSessionServiceDTO;

/*
 * Not used, only kept here for historical reference.
 */
public class IDMKeycloakAdapter
{
  /*
  private static IDMKeycloakAdapter instance;
  
  private static final Logger            logger = LoggerFactory.getLogger(IDMKeycloakAdapter.class);

  protected IDMSessionService            service;

  protected final KeycloakConfigResolver definedconfigResolver;

  protected AdapterDeploymentContext     deploymentContext;
  
  protected NodesRegistrationManagement nodesRegistrationManagement;
  
  protected AdapterTokenStore tokenStore;
  
  public static synchronized IDMKeycloakAdapter getInstance()
  {
    if (instance == null)
    {
      instance = new IDMKeycloakAdapter();
      instance.init();
    }
    
    return instance;
  }
  
  public IDMKeycloakAdapter()
  {
    this.service = new IDMSessionService();
    this.definedconfigResolver = null;
  }

  public IDMKeycloakAdapter(KeycloakConfigResolver resolver)
  {
    this.service = new IDMSessionService();
    this.definedconfigResolver = resolver;
  }

  private void init()
  {
    if (definedconfigResolver != null)
    {
      deploymentContext = new AdapterDeploymentContext(definedconfigResolver);
      logger.trace("Using {0} to resolve Keycloak configuration on a per-request basis.", definedconfigResolver.getClass());
    }
    
    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("keycloak.json");
    
    KeycloakDeployment kd;
    if (is == null) {
      logger.debug("No adapter configuration. Keycloak is unconfigured.");
      kd =  new KeycloakDeployment();
    }
    else
    {
      kd = KeycloakDeploymentBuilder.build(is);
    }
      
    deploymentContext = new AdapterDeploymentContext(kd);
    
    nodesRegistrationManagement = new NodesRegistrationManagement();
  }
  
  public class IDMRequestAuthenticator extends RequestAuthenticator
  {
    public IDMRequestAuthenticator(KeycloakDeployment deployment,
                                      AdapterTokenStore tokenStore,
                                      OIDCHttpFacade facade,
                                      HttpServletRequest request,
                                      int sslRedirectPort) {
        super(facade, deployment, tokenStore, sslRedirectPort);
    }

    @Override
    protected OAuthRequestAuthenticator createOAuthAuthenticator() {
        return new OAuthRequestAuthenticator(this, facade, deployment, sslRedirectPort, tokenStore);
    }

    @Override
    protected void completeOAuthAuthentication(final KeycloakPrincipal<RefreshableKeycloakSecurityContext> skp) {
        final RefreshableKeycloakSecurityContext securityContext = skp.getKeycloakSecurityContext();
        final Set<String> roles = AdapterUtils.getRolesFromSecurityContext(securityContext);
        OidcKeycloakAccount account = new OidcKeycloakAccount() {

            @Override
            public Principal getPrincipal() {
                return skp;
            }

            @Override
            public Set<String> getRoles() {
                return roles;
            }

            @Override
            public KeycloakSecurityContext getKeycloakSecurityContext() {
                return securityContext;
            }

        };

//        request.setAttribute(KeycloakSecurityContext.class.getName(), securityContext);
        this.tokenStore.saveAccountInfo(account);
    }

    @Override
    protected void completeBearerAuthentication(final KeycloakPrincipal<RefreshableKeycloakSecurityContext> principal, String method) {
        final RefreshableKeycloakSecurityContext securityContext = principal.getKeycloakSecurityContext();
        final Set<String> roles = AdapterUtils.getRolesFromSecurityContext(securityContext);
//        if (log.isLoggable(Level.FINE)) {
//            log.fine("Completing bearer authentication. Bearer roles: " + roles);
//        }
        
//        request.setAttribute(KeycloakSecurityContext.class.getName(), securityContext);
//        OidcKeycloakAccount account = new OidcKeycloakAccount() {
//
//            @Override
//            public Principal getPrincipal() {
//                return principal;
//            }
//
//            @Override
//            public Set<String> getRoles() {
//                return roles;
//            }
//
//            @Override
//            public KeycloakSecurityContext getKeycloakSecurityContext() {
//                return securityContext;
//            }
//
//        };
        // need this here to obtain UserPrincipal
//        request.setAttribute(KeycloakAccount.class.getName(), account);
        
        // TODO : Log into Runway?
//        IDMSessionServiceDTO.keycloakLogin(clientRequest, username, roles, locales)
    }

    @Override
    protected String changeHttpSessionId(boolean create) {
//        HttpSession session = request.getSession(create);
//        return session != null ? session.getId() : null;
      return null; // TODO
    }
  }
  
  public ResponseIF loginRedirect(ClientRequestIF clientRequest, ServletRequestIF request, ServletResponseIF response)
  {
    HttpServletRequest req = ( (RequestDecorator) request ).getRequest();
    HttpServletResponse resp = ( (ResponseDecorator) response ).getResponse();
    
    OIDCServletHttpFacade facade = new OIDCServletHttpFacade(req, resp);
    KeycloakDeployment deployment = deploymentContext.resolveDeployment(facade);
    if (deployment == null || !deployment.isConfigured())
    {
      throw new ProgrammingErrorException(""); // TODO better error messaage
    }
    
    nodesRegistrationManagement.tryRegister(deployment);
    
//    FilterRequestAuthenticator authenticator = new IDMRequestAuthenticator(deployment, tokenStore, facade, request, 8443);
//    AuthOutcome outcome = authenticator.authenticate();
//    if (outcome == AuthOutcome.AUTHENTICATED)
//    {
//      
//    }
    
    return null; // TODO
  }
  */
}
