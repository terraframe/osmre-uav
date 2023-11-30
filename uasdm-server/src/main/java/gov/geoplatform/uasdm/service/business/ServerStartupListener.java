package gov.geoplatform.uasdm.service.business;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

import net.geoprism.registry.Organization;
import net.geoprism.registry.OrganizationQuery;
import net.geoprism.registry.cache.ServerOrganizationCache;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.service.request.CacheProviderIF;

@Service
public class ServerStartupListener implements ApplicationListener<ContextRefreshedEvent>
{
  @Autowired
  private CacheProviderIF provider;

  @Request
  public synchronized void populateCache()
  {
    ServerOrganizationCache cache = this.provider.getServerCache();
    cache.rebuild();

    try
    {
      OrganizationQuery oQ = new OrganizationQuery(new QueryFactory());
      OIterator<? extends Organization> it3 = oQ.getIterator();

      try
      {
        while (it3.hasNext())
        {
          Organization organization = it3.next();

          cache.addOrganization(ServerOrganization.get(organization));
        }
      }
      finally
      {
        it3.close();
      }
    }
    catch (com.runwaysdk.dataaccess.cache.DataNotFoundException e)
    {
      // skip for now
    }
  }

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event)
  {
    this.populateCache();
  }

}
