package gov.geoplatform.uasdm.graph;

import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Test;

import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.model.ComponentFacade;

public class FeatureTest
{

  @Test
  @Request
  public void testBBOX()
  {
    JSONArray bbox = ComponentFacade.bbox();

    Assert.assertNotNull(bbox);
  }
}
