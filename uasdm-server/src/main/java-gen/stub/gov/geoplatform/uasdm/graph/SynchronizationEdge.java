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

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.metadata.MdEdge;

import net.geoprism.graph.LabeledPropertyGraphTypeVersion;

public class SynchronizationEdge extends SynchronizationEdgeBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -1169089664;

  public SynchronizationEdge()
  {
    super();
  }

  @Override
  public void delete()
  {
    MdEdge graphEdge = this.getGraphEdge();

    super.delete();

    if (graphEdge != null)
    {
      graphEdge.delete();
    }
  }

  public static SynchronizationEdge get(LabeledPropertyGraphTypeVersion version)
  {
    SynchronizationEdgeQuery query = new SynchronizationEdgeQuery(new QueryFactory());
    query.WHERE(query.getVersion().EQ(version));

    try (OIterator<? extends SynchronizationEdge> it = query.getIterator())
    {
      if (it.hasNext())
      {
        return it.next();
      }
    }

    return null;
  }

}
