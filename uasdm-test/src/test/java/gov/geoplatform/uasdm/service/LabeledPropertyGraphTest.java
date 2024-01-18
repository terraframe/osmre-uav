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
package gov.geoplatform.uasdm.service;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.metadata.MdEdge;
import com.runwaysdk.system.metadata.MdVertex;

import gov.geoplatform.uasdm.Area51DataTest;
import gov.geoplatform.uasdm.InstanceTestClassListener;
import gov.geoplatform.uasdm.SpringInstanceTestClassRunner;
import gov.geoplatform.uasdm.TestConfig;
import gov.geoplatform.uasdm.graph.SynchronizationEdge;
import gov.geoplatform.uasdm.mock.MockRegistryConnectionBuilder;
import gov.geoplatform.uasdm.test.Area51DataSet;
import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.HierarchyTypeSnapshot;
import net.geoprism.graph.LabeledPropertyGraphSynchronization;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.registry.lpg.adapter.RegistryConnectorFactory;
import net.geoprism.registry.lpg.adapter.RegistryConnectorIF;
import net.geoprism.registry.service.business.LabeledPropertyGraphSynchronizationBusinessServiceIF;
import net.geoprism.registry.service.business.LabeledPropertyGraphTypeVersionBusinessServiceIF;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class LabeledPropertyGraphTest extends Area51DataTest
{
  @Autowired
  private LabeledPropertyGraphSynchronizationBusinessServiceIF service;

  @Autowired
  private LabeledPropertyGraphTypeVersionBusinessServiceIF     vService;

  @Before
  @Request
  public void setUp()
  {
    testData.setUpInstanceData();

    testData.logIn();
  }

  @Test
  @Request
  public void testSynchronization() throws Exception
  {
    // https://localhost:8444/georegistry/
    testData.execute(() -> {
      String url = "https://localhost:8443/georegistry/";

      RegistryConnectorFactory.setBuilder(new MockRegistryConnectionBuilder());

      try (RegistryConnectorIF connector = RegistryConnectorFactory.getConnector(url))
      {
        LabeledPropertyGraphSynchronization synchronization = new LabeledPropertyGraphSynchronization();
        synchronization.setUrl(url);
        synchronization.setRemoteType("TEST");
        synchronization.getDisplayLabel().setValue("Test");
        synchronization.setRemoteEntry("TEST");
        synchronization.setForDate(new Date());
        synchronization.setRemoteVersion("TEST");
        synchronization.setVersionNumber(0);
        synchronization.apply();

        try
        {
          this.service.execute(synchronization);

          LabeledPropertyGraphTypeVersion version = synchronization.getVersion();

          List<GeoObjectTypeSnapshot> vertices = this.vService.getTypes(version);

          Assert.assertEquals(11, vertices.size());

          List<HierarchyTypeSnapshot> edges = this.vService.getHierarchies(version);

          Assert.assertEquals(1, edges.size());

          // Test that the database instance is populated
          GeoObjectTypeSnapshot graphVertex = vertices.get(0);
          MdVertex mdVertex = graphVertex.getGraphMdVertex();

          HierarchyTypeSnapshot graphEdge = edges.get(0);
          MdEdge mdEdge = graphEdge.getGraphMdEdge();

          List<VertexObject> results = new GraphQuery<VertexObject>("SELECT FROM " + mdVertex.getDbClassName()).getResults();

          Assert.assertEquals(13, results.size());

          VertexObject result = results.get(0);

          List<VertexObject> children = result.getChildren(mdEdge.definesType(), VertexObject.class);

          Assert.assertEquals(2, children.size());

          SynchronizationEdge edge = SynchronizationEdge.get(version);

          Assert.assertNotNull(edge);

          List<EdgeObject> siteEdges = new GraphQuery<EdgeObject>("SELECT FROM " + edge.getGraphEdge().getDbClassName()).getResults();

          Assert.assertEquals(1, siteEdges.size());
        }
        finally
        {
          this.service.delete(synchronization);
        }
      }
    });

  }

}
