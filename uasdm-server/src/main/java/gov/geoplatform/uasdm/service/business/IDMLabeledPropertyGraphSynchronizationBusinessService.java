package gov.geoplatform.uasdm.service.business;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import gov.geoplatform.uasdm.graph.Site;
import net.geoprism.graph.LabeledPropertyGraphSynchronization;
import net.geoprism.registry.service.business.LabeledPropertyGraphSynchronizationBusinessService;
import net.geoprism.registry.service.business.LabeledPropertyGraphSynchronizationBusinessServiceIF;

@Service
@Primary
public class IDMLabeledPropertyGraphSynchronizationBusinessService extends LabeledPropertyGraphSynchronizationBusinessService implements LabeledPropertyGraphSynchronizationBusinessServiceIF
{
  @Override
  public void executeNoAuth(LabeledPropertyGraphSynchronization synchronization)
  {
    super.executeNoAuth(synchronization);

    List<Site> sites = Site.getAll();

    for (Site site : sites)
    {
      site.assignHierarchyParents(synchronization);
    }
  }
}
