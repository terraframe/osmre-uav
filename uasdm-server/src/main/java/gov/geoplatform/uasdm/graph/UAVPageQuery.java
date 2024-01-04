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

import java.util.List;
import java.util.stream.Collectors;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.json.JSONObject;

import com.runwaysdk.business.graph.GraphQuery;

import net.geoprism.registry.Organization;

public class UAVPageQuery extends AbstractGraphPageQuery<UAV, UAVPageView>
{
  public UAVPageQuery(JSONObject criteria)
  {
    super(UAV.CLASS, criteria);
  }

  protected List<UAVPageView> getResults(final GraphQuery<UAV> query)
  {
    List<UAV> results = query.getResults();

    return results.stream().map(uav -> new UAVPageView(uav)).collect(Collectors.toList());
  }

  protected String getColumnName(String attributeName)
  {
    if (attributeName.equals(UAV.BUREAU))
    {
      return UAV.BUREAU + "." + Bureau.NAME;
    }
    else if (attributeName.equals(UAV.ORGANIZATION))
    {
      return UAV.ORGANIZATION + "." + Organization.DISPLAYLABEL + "." + LocalizedValue.DEFAULT_LOCALE;
    }
    else if (attributeName.equals(UAV.PLATFORM))
    {
      return UAV.PLATFORM + "." + Platform.NAME;
    }

    return attributeName;
  }

}
