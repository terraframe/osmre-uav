package gov.geoplatform.uasdm.bus;

import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Test;

import com.runwaysdk.session.Request;

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
