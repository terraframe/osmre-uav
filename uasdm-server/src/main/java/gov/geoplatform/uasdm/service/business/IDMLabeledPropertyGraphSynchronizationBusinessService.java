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
package gov.geoplatform.uasdm.service.business;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.SelectableReference;

import gov.geoplatform.uasdm.bus.LabeledPropertyGraphSynchronizationJob;
import gov.geoplatform.uasdm.bus.LabeledPropertyGraphSynchronizationJobQuery;
import gov.geoplatform.uasdm.graph.Site;
import net.geoprism.graph.LabeledPropertyGraphSynchronization;
import net.geoprism.graph.LabeledPropertyGraphSynchronizationQuery;
import net.geoprism.graph.LabeledPropertyGraphType;
import net.geoprism.graph.LabeledPropertyGraphTypeQuery;
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

  @Override
  @Transaction
  public void delete(LabeledPropertyGraphSynchronization synchronization)
  {
    LabeledPropertyGraphSynchronizationJobQuery query = new LabeledPropertyGraphSynchronizationJobQuery(new QueryFactory());
    query.WHERE(query.getSynchronization().EQ(synchronization));

    try (OIterator<? extends LabeledPropertyGraphSynchronizationJob> it = query.getIterator())
    {
      it.getAll().forEach(job -> job.delete());
    }

    super.delete(synchronization);
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
