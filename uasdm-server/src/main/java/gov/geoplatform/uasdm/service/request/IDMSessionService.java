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
package gov.geoplatform.uasdm.service.request;

import java.util.Set;

import gov.geoplatform.uasdm.service.SessionEventService;
import org.commongeoregistry.adapter.metadata.OrganizationDTO;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionIF;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.UserInfo;
import net.geoprism.rbac.RoleView;
import net.geoprism.registry.Organization;
import net.geoprism.registry.service.request.SessionService;

@Service
@Primary
public class IDMSessionService extends SessionService
{
  // TODO : Don't autowire components here because this service is directly
  // instantiated

  /*
   * Expose public endpoints to allow non-logged in users to hit controller
   * endpoints
   */
  @Override
  public Set<String> getPublicEndpoints()
  {
    Set<String> endpoints = super.getPublicEndpoints();
    
    if (!AppProperties.requireKeycloakLogin())
    {
      endpoints.add("api/session/login");
      endpoints.add("api/forgotpassword/initiate");
      endpoints.add("api/forgotpassword/complete");
      endpoints.add("api/uasdm-account/inviteComplete");
      endpoints.add("api/uasdm-account/newInstance");
    }
    
    // Public tile service endpoints
    if (AppProperties.getExposePublicTileEndpoints())
    {
      endpoints.add("api/cog/tilejson.json");
      endpoints.add("api/cog/tiles");
    }
    
    endpoints.add("api/session/ologin");
    endpoints.add("api/session/logout");
    endpoints.add("project/management");
    endpoints.add("project/configuration");
    // endpoints.add("websocket-notifier/notify");
    return endpoints;
  }

  @Override
  public String getHomeUrl()
  {
    return "/project/management";
  }

  @Override
  public String getLoginUrl()
  {
    return "/project/management#/login";
  }

  @Override
  public void onLoginSuccess(String username, String sessionId)
  {
    new SessionEventService().logSuccessfulLogin(sessionId, username);
  }

  @Override
  public void onLoginFailure(String username)
  {
    new SessionEventService().logFailureLogin(username);
  }

  @Override
  @Request(RequestType.SESSION)
  public JsonElement getLoginResponse(String sessionId, Set<RoleView> roles)
  {
    JsonObject response = super.getLoginResponse(sessionId, roles).getAsJsonObject();
    this.injectIdmInfo(response);

    return response;
  }

  @Override
  @Request(RequestType.SESSION)
  public JsonElement getCookieInformation(String sessionId, Set<RoleView> roles)
  {
    JsonObject response = super.getCookieInformation(sessionId, roles).getAsJsonObject();
    this.injectIdmInfo(response);

    return response;
  }

  private void injectIdmInfo(JsonObject response)
  {
    // Add the bureau to the response object
    SessionIF session = Session.getCurrentSession();

    if (session != null)
    {
      SingleActorDAOIF user = session.getUser();

      if (user != null)
      {
        UserInfo userInfo = UserInfo.getUserInfo(user.getOid());

        if (userInfo != null)
        {
          try (OIterator<? extends Organization> organizations = userInfo.getAllOrganization())
          {
            if (organizations.hasNext())
            {
              Organization organization = organizations.next();
              OrganizationDTO dto = organization.toDTO();

              JsonObject object = dto.toJSON();
              object.remove(OrganizationDTO.JSON_LOCALIZED_CONTACT_INFO);
              object.remove(OrganizationDTO.JSON_ENABLED);
              object.remove(OrganizationDTO.JSON_PARENT_CODE);
              object.remove(OrganizationDTO.JSON_PARENT_LABEL);

              response.add("organization", object);
            }
          }
        }
      }
    }
  }
}
