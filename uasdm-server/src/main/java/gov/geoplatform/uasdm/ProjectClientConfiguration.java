package gov.geoplatform.uasdm;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.runwaysdk.constants.ClientRequestIF;

import net.geoprism.ClientConfigurationIF;
import net.geoprism.DefaultClientConfiguration;
import net.geoprism.GeoprismApplication;
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
    endpoints.add("project/management");
    endpoints.add("uasdm-account/newInstance");
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
}
