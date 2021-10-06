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

import org.json.JSONObject;

import com.runwaysdk.business.graph.GraphQuery;

public class SensorPageQuery extends AbstractGraphPageQuery<Sensor, SensorPageView>
{
  public SensorPageQuery(JSONObject criteria)
  {
    super(Sensor.CLASS, criteria);
  }

  protected List<SensorPageView> getResults(final GraphQuery<Sensor> query)
  {
    List<Sensor> results = query.getResults();

    return results.stream().map(sensor -> new SensorPageView(sensor)).collect(Collectors.toList());
  }

  protected String getColumnName(String attributeName)
  {
    if (attributeName.equals(Sensor.SENSORTYPE))
    {
      return Sensor.SENSORTYPE + "." + SensorType.NAME;
    }

    return attributeName;
  }

}
