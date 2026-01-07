/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.geoplatform.uasdm.service.request;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;

import gov.geoplatform.uasdm.graph.RawSet;
import gov.geoplatform.uasdm.graph.UserAccessEntity;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.ComponentFacade;
import gov.geoplatform.uasdm.model.ComponentRawSet;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.view.CollectionCriteria;
import gov.geoplatform.uasdm.view.ComponentRawSetView;
import gov.geoplatform.uasdm.view.Converter;
import gov.geoplatform.uasdm.view.CreateRawSetView;
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

  @Request(RequestType.SESSION)
  public List<ComponentRawSetView> getAll(String sessionId, CollectionCriteria criteria)
  {
    List<ComponentRawSetView> array = new LinkedList<>();

    List<ComponentRawSet> dtos = null;

    if (criteria.getType().equals(CollectionCriteria.SITE))
    {
      final UasComponentIF parent = ComponentFacade.getComponent(criteria.getId());
      dtos = parent.getDerivedRawSets(criteria.getSortField(), criteria.getSortOrder());
    }
    else
    {
      dtos = ComponentFacade.getRawSets(criteria);
    }

    for (ComponentRawSet dto : dtos)
    {
      UasComponentIF component = dto.getComponent();

      List<UasComponentIF> components = component.getAncestors();
      Collections.reverse(components);
      components.add(component);

      List<RawSetView> views = dto.getRawSets().stream().map(set -> {
        return Converter.toView(set, components);
      }).collect(Collectors.toList());

      if (component instanceof CollectionIF)
      {
        ComponentRawSetView object = new ComponentRawSetView();
        object.setComponentId(component.getOid());
        object.setSets(views);
        object.setComponentType(component.getClass().getSimpleName().toLowerCase());

        array.add(object);
      }
      else
      {
        views.forEach(view -> {
          ComponentRawSetView object = new ComponentRawSetView();
          object.setComponentId(component.getOid());
          object.setSets(Arrays.asList(view));
          object.setComponentType(component.getClass().getSimpleName().toLowerCase());

          array.add(object);
        });
      }
    }

    return array;
  }

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
  public RawSetView create(String sessionId, CreateRawSetView view)
  {
    CollectionIF collection = ComponentFacade.getCollection(view.getCollectionId());

    RawSet set = collection.createRawSetIfNotExist(view);

    List<UasComponentIF> components = collection.getAncestors();
    Collections.reverse(components);
    components.add(collection);

    JSONObject data = new JSONObject();
    data.put("collection", collection.getOid());

    NotificationFacade.queue(new UserNotificationMessage(Session.getCurrentSession(), MessageType.PRODUCT_GROUP_CHANGE, data));

    return Converter.toView(set, components);
  }

}
