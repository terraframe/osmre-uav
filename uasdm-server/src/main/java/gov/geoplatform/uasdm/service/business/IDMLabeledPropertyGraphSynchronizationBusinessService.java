package gov.geoplatform.uasdm.service.business;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import gov.geoplatform.uasdm.graph.Site;
import net.geoprism.graph.LabeledPropertyGraphSynchronization;
import net.geoprism.graph.LabeledPropertyGraphSynchronizationQuery;
import net.geoprism.graph.LabeledPropertyGraphType;
import net.geoprism.graph.LabeledPropertyGraphTypeQuery;
import net.geoprism.registry.model.ServerOrganization;
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

  public JsonArray getForOrganization(ServerOrganization organization)
  {
    QueryFactory factory = new QueryFactory();

    LabeledPropertyGraphTypeQuery query = new LabeledPropertyGraphTypeQuery(factory);
    query.WHERE(query.get(LabeledPropertyGraphType.ORGANIZATION).EQ(organization.getOrganization().getOid()));

    LabeledPropertyGraphSynchronizationQuery sQuery = new LabeledPropertyGraphSynchronizationQuery(factory);
    sQuery.WHERE(sQuery.getGraphType().EQ(query));

    JsonArray array = new JsonArray();

    try (OIterator<? extends LabeledPropertyGraphSynchronization> iterator = sQuery.getIterator())
    {
      iterator.forEach(i -> array.add(i.toJSON()));
    }

    return array;

  }
}
