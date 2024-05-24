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

//    VectorTileBuilder builder = new VectorTileBuilder(synchronization, "Region");
//    byte[] tile = builder.writeVectorTiles(envelope, bounds);
//
//    Assert.assertTrue(tile.length > 0);
  }
}
