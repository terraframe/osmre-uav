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
package gov.geoplatform.uasdm;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.runwaysdk.constants.ClientRequestIF;

import gov.geoplatform.uasdm.service.SessionEventService;
import net.geoprism.ClientConfigurationIF;
import net.geoprism.DefaultClientConfiguration;
import net.geoprism.GeoprismApplication;
import net.geoprism.SessionEvent;
import net.geoprism.localization.LocalizationFacadeDTO;

public class ProjectClientConfiguration extends DefaultClientConfiguration implements ClientConfigurationIF
{

  @Override
  public List<GeoprismApplication> getApplications(ClientRequestIF request)
  {
    GeoprismApplication management = new GeoprismApplication();
    management.setId("projects");
    management.setLabel(LocalizationFacadeDTO.getFromBundles(request, "projects.landing"));
    management.setSrc("net/geoprism/images/dm_icon.svg");
    management.setUrl("project/management");

    List<GeoprismApplication> applications = new LinkedList<GeoprismApplication>();
    applications.add(management);

    return applications;
  }

  /*
   * Expose public endpoints to allow non-logged in users to hit controller
   * endpoints
   */
  @Override
  public Set<String> getPublicEndpoints()
  {
    Set<String> endpoints = super.getPublicEndpoints();
    endpoints.add("uasdm-account/inviteComplete");
    endpoints.add("uasdm-account/newInstance");
    endpoints.add("project/management");
    endpoints.add("project/configuration");    
    endpoints.add("websocket-notifier/notify");
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
  public void handleSessionEvent(SessionEvent event)
  {
    SessionEventService service = new SessionEventService();
    service.handleSessionEvent(event);
  }
}
