package gov.geoplatform.uasdm.bus;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.UserInfo;
import gov.geoplatform.uasdm.view.SiteObject;
import junit.framework.Assert;

public class UserInfoTest
{
  @Test
  @Request
  public void testPage()
  {
    JSONObject page = UserInfo.page(10, 1);

    Assert.assertNotNull(page);

    Assert.assertEquals(10, page.getInt("pageSize"));
    Assert.assertEquals(1, page.getInt("pageNumber"));
    Assert.assertTrue(page.getInt("count") > 0);

    JSONArray resultSet = page.getJSONArray("resultSet");

    Assert.assertTrue(resultSet.length() > 0);
  }

}
