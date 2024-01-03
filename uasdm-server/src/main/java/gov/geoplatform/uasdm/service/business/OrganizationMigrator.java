package gov.geoplatform.uasdm.service.business;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import gov.geoplatform.uasdm.SessionEventLog;
import gov.geoplatform.uasdm.SessionEventLogQuery;
import gov.geoplatform.uasdm.UserInfo;
import gov.geoplatform.uasdm.UserInfoQuery;
import gov.geoplatform.uasdm.UserInvite;
import gov.geoplatform.uasdm.UserInviteQuery;
import gov.geoplatform.uasdm.bus.Bureau;
import gov.geoplatform.uasdm.bus.CollectionReport;
import gov.geoplatform.uasdm.bus.CollectionReportQuery;
import gov.geoplatform.uasdm.graph.Site;
import net.geoprism.registry.Organization;
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
//    this.updateUserInvites();
    this.updateCollectionReports();
    this.updateSessionEventLog();
  }

  private void updateCollectionReports()
  {
    CollectionReportQuery query = new CollectionReportQuery(new QueryFactory());
    query.WHERE(query.getOrganization().EQ((String) null));

    try (OIterator<? extends CollectionReport> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        CollectionReport report = iterator.next();

        Bureau bureau = report.getBureau();

        if (bureau != null)
        {
          ServerOrganization organization = this.getOrganization(bureau);

          report.appLock();
          report.setOrganization(organization.getOrganization());
          report.setBureauName(organization.getDisplayLabel().getValue());
          report.apply();
        }
      }
    }
  }

  private void updateSessionEventLog()
  {
    SessionEventLogQuery query = new SessionEventLogQuery(new QueryFactory());
    query.WHERE(query.getOrganization().EQ((String) null));

    try (OIterator<? extends SessionEventLog> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        SessionEventLog log = iterator.next();

        Bureau bureau = log.getBureau();

        if (bureau != null)
        {
          ServerOrganization organization = this.getOrganization(bureau);

          log.appLock();
          log.setOrganization(organization.getOrganization());
          log.apply();
        }
      }
    }
  }

  private void updateSites()
  {
    List<Site> sites = Site.getAll();

    for (Site site : sites)
    {
      if (StringUtils.isBlank(site.getObjectValue(Site.ORGANIZATION)))
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
    query.WHERE(query.getOrganization().EQ((String) null));

    try (OIterator<? extends UserInvite> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        UserInvite info = iterator.next();

        Bureau invite = info.getBureau();

        if (invite != null)
        {
          ServerOrganization organization = this.getOrganization(invite);

          info.appLock();
          info.setOrganization(organization.getOrganization());
          info.apply();
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
