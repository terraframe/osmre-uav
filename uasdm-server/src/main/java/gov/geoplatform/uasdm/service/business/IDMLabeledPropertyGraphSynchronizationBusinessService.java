package gov.geoplatform.uasdm.service.business;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.SelectableReference;

import gov.geoplatform.uasdm.graph.Site;
import net.geoprism.graph.LabeledPropertyGraphSynchronization;
import net.geoprism.graph.LabeledPropertyGraphSynchronizationQuery;
import net.geoprism.graph.LabeledPropertyGraphType;
import net.geoprism.graph.LabeledPropertyGraphTypeQuery;
import net.geoprism.registry.OrganizationQuery;
import net.geoprism.registry.graph.GraphOrganization;
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
    // Two phase query. The first phase traverses the graph tree to find all of
    // the child organizations of a node in the tree. The second phase then
    // queries postgres with the list of child organizations as criteria

    StringBuilder statement = new StringBuilder();
    statement.append("TRAVERSE OUT('organization_hierarchy') FROM :organization");

    GraphQuery<GraphOrganization> gQuery = new GraphQuery<GraphOrganization>(statement.toString());
    gQuery.setParameter("organization", organization.getGraphOrganization().getRID());

    String[] organizationIds = gQuery.getResults().stream().map(org -> org.getOrganizationOid()).toArray(String[]::new);

    QueryFactory factory = new QueryFactory();

    LabeledPropertyGraphTypeQuery query = new LabeledPropertyGraphTypeQuery(factory);
    query.WHERE( ( (SelectableReference) query.get(LabeledPropertyGraphType.ORGANIZATION) ).IN(organizationIds));

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
