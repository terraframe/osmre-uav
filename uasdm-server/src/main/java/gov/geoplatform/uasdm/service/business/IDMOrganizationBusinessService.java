package gov.geoplatform.uasdm.service.business;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import gov.geoplatform.uasdm.UserInfo;
import gov.geoplatform.uasdm.UserInfoQuery;
import gov.geoplatform.uasdm.UserInvite;
import gov.geoplatform.uasdm.UserInviteQuery;
import gov.geoplatform.uasdm.bus.Bureau;
import gov.geoplatform.uasdm.graph.Site;
import net.geoprism.registry.Organization;
import net.geoprism.registry.OrganizationQuery;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.service.business.OrganizationBusinessService;
import net.geoprism.registry.service.business.OrganizationBusinessServiceIF;

@Service
@Primary
public class IDMOrganizationBusinessService extends OrganizationBusinessService implements OrganizationBusinessServiceIF
{
  public List<ServerOrganization> search(String text)
  {
    OrganizationQuery query = new OrganizationQuery(new QueryFactory());
    query.WHERE(query.getDisplayLabel().localize().LIKEi("%" + text + "%"));
    query.OR(query.getCode().LIKEi("%" + text + "%"));

    try (OIterator<? extends Organization> iterator = query.getIterator())
    {
      return iterator.getAll().stream().map(org -> ServerOrganization.get(org)).collect(Collectors.toList());
    }
  }

  public void patch()
  {
    Map<String, ServerOrganization> cache = new HashMap<>();

    this.updateSites(cache);
    this.updateUserInfos(cache);
    this.updateUserInvites(cache);
  }

  private void updateSites(Map<String, ServerOrganization> cache)
  {
    List<Site> sites = Site.getAll();

    for (Site site : sites)
    {
      Bureau bureau = site.getBureau();

      if (bureau != null)
      {
        String key = bureau.getName();

        if (!cache.containsKey(key))
        {
          cache.put(key, ServerOrganization.getByCode(key, true));
        }

        ServerOrganization organization = cache.get(key);

        site.setOrganization(organization.getGraphOrganization());
        site.apply();
      }
    }
  }

  private void updateUserInfos(Map<String, ServerOrganization> cache)
  {
    UserInfoQuery query = new UserInfoQuery(new QueryFactory());
    try (OIterator<? extends UserInfo> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        UserInfo info = iterator.next();

        Bureau bureau = info.getBureau();

        if (bureau != null)
        {
          String key = bureau.getName();

          if (!cache.containsKey(key))
          {
            cache.put(key, ServerOrganization.getByCode(key, true));
          }

          ServerOrganization organization = cache.get(key);

          info.addOrganization(organization.getOrganization()).apply();
        }
      }
    }
  }

  private void updateUserInvites(Map<String, ServerOrganization> cache)
  {
    UserInviteQuery query = new UserInviteQuery(new QueryFactory());
    try (OIterator<? extends UserInvite> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        UserInvite info = iterator.next();

        Bureau bureau = info.getBureau();

        if (bureau != null)
        {
          String key = bureau.getName();

          if (!cache.containsKey(key))
          {
            cache.put(key, ServerOrganization.getByCode(key, true));
          }

          ServerOrganization organization = cache.get(key);

          info.appLock();
          info.setOrganization(organization.getOrganization());
          info.apply();
        }
      }
    }
  }

}
