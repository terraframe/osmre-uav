package gov.geoplatform.uasdm.graph;

import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Test;

import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.bus.UasComponent;

public class FeatureTest
{

  @Test
  @Request
  public void testBBOX()
  {
    JSONArray bbox = UasComponent.bbox();

    Assert.assertNotNull(bbox);
  }
}
