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
package gov.geoplatform.uasdm.service.business;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.system.metadata.MdVertex;

import gov.geoplatform.uasdm.service.IndexService;
import net.geoprism.registry.service.request.JsonGraphVersionPublisherService;
import net.geoprism.registry.service.request.JsonGraphVersionPublisherServiceIF;

@Service
@Primary
public class IDMJsonGraphVersionPublisherService extends JsonGraphVersionPublisherService implements JsonGraphVersionPublisherServiceIF
{
  
  @Override
  protected VertexObject publish(State state, MdVertex mdVertex, GeoObject geoObject)
  {
    VertexObject object = super.publish(state, mdVertex, geoObject);
    
    IndexService.createDocument(state.synchronization, object);

    return object;
  }
}
