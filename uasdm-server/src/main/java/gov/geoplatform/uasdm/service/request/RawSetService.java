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
package gov.geoplatform.uasdm.service.request;

import java.util.Collections;
import java.util.List;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;

import gov.geoplatform.uasdm.graph.RawSet;
import gov.geoplatform.uasdm.graph.UserAccessEntity;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.ComponentFacade;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.view.Converter;
import gov.geoplatform.uasdm.view.RawSetView;
import gov.geoplatform.uasdm.ws.MessageType;
import gov.geoplatform.uasdm.ws.NotificationFacade;
import gov.geoplatform.uasdm.ws.UserNotificationMessage;

@Service
public class RawSetService
{
  @Request(RequestType.SESSION)
  public void remove(String sessionId, String oid)
  {
    RawSet set = ComponentFacade.getRawSet(oid);

    UasComponentIF component = set.getComponent();
    UserAccessEntity.validateAccess(component);

    set.delete();
  }

//  @Request(RequestType.SESSION)
//  public List<RawSetView> getRawSets(String sessionId, RawSetCriteria criteria)
//  {
//    List<RawSetView> array = new LinkedList<>();
//
//    List<ComponentRawSetDTO> dtos = null;
//
//    if (criteria.getType().equals(RawSetCriteria.SITE))
//    {
//      final UasComponentIF parent = ComponentFacade.getComponent(criteria.getId());
//      dtos = parent.getDerivedRawSets(criteria.getSortField(), criteria.getSortOrder());
//    }
//    else
//    {
//      dtos = ComponentFacade.getRawSets(criteria);
//    }
//
//    for (ComponentRawSetDTO dto : dtos)
//    {
//      UasComponentIF component = dto.getComponent();
//
//      List<UasComponentIF> components = component.getAncestors();
//      Collections.reverse(components);
//      components.add(component);
//
//      JSONArray sets = dto.getRawSets().stream().map(set -> {
//        return Converter.toView(set, components).toJSON();
//      }).collect(Collector.of(JSONArray::new, JSONArray::put, JSONArray::put));
//
//      if (component instanceof CollectionIF)
//      {
//        JSONObject object = new JSONObject();
//        object.put("componentId", component.getOid());
//        object.put("sets", sets);
//        object.put("componentType", component.getClass().getSimpleName().toLowerCase());
//        array.put(object);
//      }
//      else
//      {
//        for (int i = 0; i < sets.length(); ++i)
//        {
//          JSONObject joRawSet = sets.getJSONObject(i);
//
//          JSONArray newRawSets = new JSONArray();
//          newRawSets.put(joRawSet);
//
//          JSONObject object = new JSONObject();
//          object.put("componentId", component.getOid());
//          object.put("sets", newRawSets);
//          object.put("componentType", component.getClass().getSimpleName().toLowerCase());
//          array.put(object);
//        }
//      }
//    }
//
//    return array;
//  }
//
//  @Request(RequestType.SESSION)
//  public RawSetDetailView getRawSetDetail(String sessionId, String id, Integer pageNumber, Integer pageSize)
//  {
//    RawSet set = ComponentFacade.getRawSet(id);
//
//    UasComponentIF component = set.getComponent();
//    UserAccessEntity.validateAccess(component);
//
//    List<UasComponentIF> components = component.getAncestors();
//    Collections.reverse(components);
//    components.add(component);
//
//    final List<DocumentIF> generated = set.getGeneratedFromDocuments();
//
//    return Converter.toDetailView(set, components, generated, pageNumber, pageSize);
//  }

  @Request(RequestType.SESSION)
  public RawSetView togglePublish(String sessionId, String id)
  {
    RawSet set = ComponentFacade.getRawSet(id);

    UasComponentIF component = set.getComponent();
    UserAccessEntity.validateAccess(component);

    set.togglePublished();

    List<UasComponentIF> components = component.getAncestors();
    Collections.reverse(components);
    components.add(component);

    return Converter.toView(set, components);
  }

  @Request(RequestType.SESSION)
  public void toggleLock(String sessionId, String id)
  {
    RawSet set = ComponentFacade.getRawSet(id);

    UasComponentIF component = set.getComponent();
    UserAccessEntity.validateAccess(component);

    set.toggleLock();
  }

  @Request(RequestType.SESSION)
  public RawSetView create(String sessionId, String collectionId, String setName)
  {
    CollectionIF collection = ComponentFacade.getCollection(collectionId);

    RawSet set = collection.createRawSetIfNotExist(setName);

    List<UasComponentIF> components = collection.getAncestors();
    Collections.reverse(components);
    components.add(collection);

    JSONObject data = new JSONObject();
    data.put("collection", collection.getOid());

    NotificationFacade.queue(new UserNotificationMessage(Session.getCurrentSession(), MessageType.PRODUCT_GROUP_CHANGE, data));

    return Converter.toView(set, components);
  }

}
