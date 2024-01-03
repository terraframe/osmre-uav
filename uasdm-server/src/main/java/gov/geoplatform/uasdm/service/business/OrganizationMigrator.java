package gov.geoplatform.uasdm.service.business;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import gov.geoplatform.uasdm.UserInfo;
import gov.geoplatform.uasdm.UserInfoQuery;
import gov.geoplatform.uasdm.UserInvite;
import gov.geoplatform.uasdm.UserInviteQuery;
import gov.geoplatform.uasdm.bus.Bureau;
import gov.geoplatform.uasdm.graph.Site;
import net.geoprism.registry.Organization;
import net.geoprism.registry.graph.GraphOrganization;
import net.geoprism.registry.model.ServerOrganization;

public class OrganizationMigrator
{
  // Cached ServerOrganization objects by the bureau name
  private Map<String, ServerOrganization> cache;

  // Mapping of bureau name to organization code
  private Map<String, String>             mapping;

  public OrganizationMigrator()
  {
    this.cache = new HashMap<String, ServerOrganization>();
    this.mapping = new HashMap<String, String>();
  }

  public void run()
  {
    try (CSVReader reader = new CSVReader(new InputStreamReader(this.getClass().getResourceAsStream("/bureau-mapping.csv"))))
    {
      String[] line = null;

      while ( ( line = reader.readNext() ) != null)
      {
        mapping.put(line[0], line[1]);
      }
    }
    catch (IOException | CsvValidationException e)
    {
      throw new ProgrammingErrorException(e);
    }

    this.updateSites();
    this.updateUserInfos();
    this.updateUserInvites();
  }

  private void updateSites()
  {
    List<Site> sites = Site.getAll();

    for (Site site : sites)
    {
      if (site.getOrganization() == null)
      {
        Bureau bureau = site.getBureau();

        if (bureau != null)
        {
          ServerOrganization organization = this.getOrganization(bureau);

          site.setOrganization(organization.getGraphOrganization());
          site.apply();
        }
      }
    }
  }

  private void updateUserInfos()
  {
    UserInfoQuery query = new UserInfoQuery(new QueryFactory());
    try (OIterator<? extends UserInfo> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        UserInfo info = iterator.next();
        
        try (OIterator<? extends Organization> it = info.getAllOrganization())
        {
          List<? extends Organization> orgs = it.getAll();

          if (orgs.size() == 0)
          {
            Bureau bureau = info.getBureau();

            if (bureau != null)
            {
              ServerOrganization organization = this.getOrganization(bureau);

              info.addOrganization(organization.getOrganization()).apply();
            }
          }
        }
      }
    }
  }

  private void updateUserInvites()
  {
    UserInviteQuery query = new UserInviteQuery(new QueryFactory());
    try (OIterator<? extends UserInvite> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        UserInvite info = iterator.next();

        if (info.getOrganization() == null)
        {
          Bureau bureau = info.getBureau();

          if (bureau != null)
          {
            ServerOrganization organization = this.getOrganization(bureau);

            info.appLock();
            info.setOrganization(organization.getOrganization());
            info.apply();
          }
        }
      }
    }
  }

  private ServerOrganization getOrganization(Bureau bureau)
  {
    String key = bureau.getName();

    if (!cache.containsKey(key))
    {
      String code = this.mapping.get(key);

      ServerOrganization organization = ServerOrganization.getByCode(code, true);
      cache.put(key, organization);
    }

    ServerOrganization organization = cache.get(key);
    return organization;
  }

}
