package gov.geoplatform.uasdm.tile;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.geom.Envelope;
import org.springframework.test.context.ContextConfiguration;

import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.SpringInstanceTestClassRunner;
import gov.geoplatform.uasdm.TestConfig;
import net.geoprism.graph.LabeledPropertyGraphSynchronization;
import net.geoprism.registry.tile.PublisherUtil;
import net.geoprism.registry.tile.VectorTileBuilder;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class VectorTileBuilderTest
{
  @Test
  @Request
  public void testBuildTile()
  {
    LabeledPropertyGraphSynchronization synchronization = LabeledPropertyGraphSynchronization.get("de832d05-7448-436a-a378-2ca88d0005c0");

    Envelope envelope = PublisherUtil.getEnvelope(14, 24, 6);
    Envelope bounds = PublisherUtil.getTileBounds(envelope);

    VectorTileBuilder builder = new VectorTileBuilder(synchronization, "Region");
    byte[] tile = builder.writeVectorTiles(envelope, bounds);

    Assert.assertTrue(tile.length > 0);
  }
}
