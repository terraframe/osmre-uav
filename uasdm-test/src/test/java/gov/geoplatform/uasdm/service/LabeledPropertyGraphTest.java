package gov.geoplatform.uasdm.service;

import java.util.List;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.metadata.MdEdge;
import com.runwaysdk.system.metadata.MdVertex;

import gov.geoplatform.uasdm.TestConfig;
import gov.geoplatform.uasdm.test.Area51DataSet;
import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.HierarchyTypeSnapshot;
import net.geoprism.graph.LabeledPropertyGraphSynchronization;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.graph.adapter.HTTPConnector;
import net.geoprism.graph.adapter.RegistryBridge;
import net.geoprism.graph.adapter.RegistryConnectorBuilderIF;
import net.geoprism.graph.adapter.RegistryConnectorFactory;
import net.geoprism.graph.adapter.RegistryConnectorIF;
import net.geoprism.graph.service.LabeledPropertyGraphTypeServiceIF;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringJUnit4ClassRunner.class)
public class LabeledPropertyGraphTest
{
  private static Area51DataSet              testData;

  private static boolean                    isSetup = false;

  @Autowired
  private LabeledPropertyGraphTypeServiceIF service;

  public static void setUpClass()
  {
    testData = new Area51DataSet();
    testData.setUpSuiteData();

    isSetup = true;
  }

  public static void cleanUpClass()
  {
    if (testData != null)
    {
      testData.tearDownMetadata();
    }

  }

  @Before
  @Request
  public void setUp()
  {
    if (!isSetup)
    {
      setUpClass();
    }

    testData.setUpInstanceData();

    testData.logIn();
  }

  @Test
  @Request
  public void testSynchronization() throws Exception
  {
    testData.execute(() -> {
      String url = "https://localhost:8443/georegistry/";

      RegistryConnectorFactory.setBuilder(new RegistryConnectorBuilderIF()
      {

        @Override
        public RegistryConnectorIF build(String url)
        {

          return new HTTPConnector(url)
          {
            @Override
            public synchronized void initialize()
            {
              try
              {

                CloseableHttpClient httpClient = HttpClients.custom().setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build()).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();
                this.setClient(httpClient);
              }
              catch (Exception e)
              {
                throw new RuntimeException(e);
              }
            }
          };
        }
      });

      try (RegistryConnectorIF connector = RegistryConnectorFactory.getConnector(url))
      {
        RegistryBridge bridge = new RegistryBridge(connector);
        JsonArray types = bridge.getTypes().getJsonArray();
        JsonObject type = types.get(0).getAsJsonObject();
        String remoteType = type.get("oid").getAsString();

        JsonArray entries = bridge.getEntries(remoteType).getJsonObject().get("entries").getAsJsonArray();
        JsonObject entry = entries.get(0).getAsJsonObject();
        String remoteEntry = entry.get("oid").getAsString();

        JsonArray versions = bridge.getVersions(remoteEntry).getJsonArray();
        JsonObject version = versions.get(0).getAsJsonObject();
        String remoteVersion = version.get("oid").getAsString();

        LabeledPropertyGraphSynchronization synchronization = new LabeledPropertyGraphSynchronization();
        synchronization.setUrl(url);
        synchronization.setRemoteType(remoteType);
        synchronization.setRemoteEntry(remoteEntry);
        synchronization.setRemoteVersion(remoteVersion);
        synchronization.setVersionNumber(0);
        synchronization.apply();

        try
        {
          synchronization.execute();

          LabeledPropertyGraphTypeVersion localVersion = synchronization.getVersion();

          List<GeoObjectTypeSnapshot> vertices = localVersion.getTypes();

          Assert.assertEquals(6, vertices.size());
          
          List<HierarchyTypeSnapshot> edges = localVersion.getHierarchies();

          Assert.assertEquals(1, edges.size());
          
          // Test that the database instance is populated
          GeoObjectTypeSnapshot graphVertex = vertices.get(0);
          MdVertex mdVertex = graphVertex.getGraphMdVertex();

          HierarchyTypeSnapshot graphEdge = edges.get(0);
          MdEdge mdEdge = graphEdge.getGraphMdEdge();

          GraphQuery<VertexObject> query = new GraphQuery<VertexObject>("SELECT FROM " + mdVertex.getDbClassName());
          List<VertexObject> results = query.getResults();

          Assert.assertTrue(results.size() > 0);

          VertexObject result = results.get(0);

          List<VertexObject> children = result.getChildren(mdEdge.definesType(), VertexObject.class);

          Assert.assertTrue(children.size() > 0);
        }
        finally
        {

          synchronization.delete();
        }
      }
    });

  }

}
