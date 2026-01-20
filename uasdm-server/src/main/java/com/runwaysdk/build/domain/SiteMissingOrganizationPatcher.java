package com.runwaysdk.build.domain;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.cache.globalcache.ehcache.CacheShutdown;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.SingleActor;

import gov.geoplatform.uasdm.UserInfo;
import gov.geoplatform.uasdm.graph.Site;
import gov.geoplatform.uasdm.processing.report.CollectionReportFacade;
import gov.geoplatform.uasdm.service.IndexService;
import gov.geoplatform.uasdm.service.business.KnowStacBusinessService;
import net.geoprism.registry.Organization;
import net.geoprism.registry.graph.GraphOrganization;

public class SiteMissingOrganizationPatcher
{
  private static final Logger logger = LoggerFactory.getLogger(SiteMissingOrganizationPatcher.class);
  
  public static void main(String[] args)
  {
    try
    {
      try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(PatchConfig.class))
      {
        ConfigurableListableBeanFactory factory = context.getBeanFactory();
        SiteMissingOrganizationPatcher obj = (SiteMissingOrganizationPatcher) factory.initializeBean(new SiteMissingOrganizationPatcher(), "siteMissingOrganizationPatcher");
        obj.run();
      }
    }
    finally
    {
      if (args.length > 0 && Boolean.valueOf(args[0]))
      {
        IndexService.shutdown();
        CollectionReportFacade.finish();
        CacheShutdown.shutdown();
      }
    }
  }

  @Request
  public void run()
  {
    transaction();
  }

  @Transaction
  protected void transaction()
  {
    var orgUnspecified = Organization.getByCode("Unspecified");
    GraphOrganization unspecified = null;
    if (orgUnspecified != null)
      unspecified = GraphOrganization.get(orgUnspecified);
    
    var sites = this.getSites();
    
    logger.error("Updating " + sites.size() + " sites which do not have an organization");
    
    long updateCount = 0;
    for (Site site : sites) {
      SingleActor user = (SingleActor) site.getOwner();
      var userInfo = UserInfo.getByUser(user);
      
      var orgs = userInfo.getOrganizations();
      
      GraphOrganization go = unspecified;
      if (orgs != null && orgs.size() > 0)
        go = GraphOrganization.get(orgs.get(0));
      
      if (go != null) {
        site.setOrganization(go);
        site.apply();
        updateCount++;
      } else {
        throw new UnsupportedOperationException("Unable to find a valid organization for [" + site.getKey() + "]");
      }
    }
    
    logger.error("Successfully updated " + updateCount + " which did not have an organization");
  }

  private List<Site> getSites()
  {
    final String site = MdVertexDAO.getMdVertexDAO(Site.CLASS).getDBClassName();

    StringBuilder builder = new StringBuilder();
    builder.append("SELECT FROM " + site);
    builder.append(" WHERE organization is null");

    final GraphQuery<Site> query = new GraphQuery<Site>(builder.toString());

    return query.getResults();
  }
}
