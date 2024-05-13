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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.metadata.MdEdge;
import gov.geoplatform.uasdm.LPGGeometry;
import gov.geoplatform.uasdm.LPGGeometryQuery;
import net.geoprism.graph.HierarchyTypeSnapshot;
import net.geoprism.registry.service.business.HierarchyTypeSnapshotBusinessServiceIF;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.springframework.beans.factory.annotation.Autowired;
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
  public void publish(State state, VertexObject parent, VertexObject child, MdEdge mdEdge) {
    parent.addChild(child, mdEdge.definesType()).apply();

    // Update the child in LPGGeometry table for fast creation of tiles
    LPGGeometryQuery query = new LPGGeometryQuery(new QueryFactory());
    query.WHERE(query.getVersion().EQ(state.getVersion()));
    query.WHERE(query.getLocationOid().EQ(child.getOid()));

    try(OIterator<? extends LPGGeometry> it = query.getIterator()) {
      if(it.hasNext()) {
        LPGGeometry geometry = it.next();
        geometry.appLock();
        geometry.setParent(parent.getObjectValue("uuid"));
        geometry.apply();
      }
    }
  }

  @Override
  protected VertexObject publish(State state, MdVertex mdVertex, GeoObject geoObject)
  {
    VertexObject object = super.publish(state, mdVertex, geoObject);

    // Index the object in elastic search for full text look up
    IndexService.createDocument(state.synchronization, object);

    // Create the child in LPGGeometry table for fast creation of tiles
    LPGGeometry geometry = new LPGGeometry();
    geometry.setVersion(state.version);
    geometry.setTypeCode(geoObject.getType().getCode());
    geometry.setLocationOid(object.getOid());
    geometry.setLocationCode(object.getObjectValue(GeoObject.CODE));
    geometry.setLocationLabel(object.getEmbeddedValue(GeoObject.DISPLAY_LABEL, LocalizedValue.DEFAULT_LOCALE));
    geometry.setLocationUuid(object.getObjectValue("uuid"));
    geometry.setGeometry(object.getObjectValue("geometry"));
    geometry.apply();

    return object;
  }
}
