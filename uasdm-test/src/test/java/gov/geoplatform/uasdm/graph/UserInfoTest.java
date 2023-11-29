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
package gov.geoplatform.uasdm.graph;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.SpringInstanceTestClassRunner;
import gov.geoplatform.uasdm.TestConfig;
import gov.geoplatform.uasdm.UserInfo;
import org.junit.Assert;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class UserInfoTest
{
  @Test
  @Request
  public void testPage()
  {
    JSONObject criteria = new JSONObject();
    criteria.put("first", 0);
    criteria.put("rows", 10);
    JSONObject page = UserInfo.page(criteria);

    Assert.assertNotNull(page);

    Assert.assertEquals(10, page.getInt("pageSize"));
    Assert.assertEquals(1, page.getInt("pageNumber"));
    Assert.assertTrue(page.getInt("count") > 0);

    JSONArray resultSet = page.getJSONArray("resultSet");

    Assert.assertTrue(resultSet.length() > 0);
  }

}
