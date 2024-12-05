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
package gov.geoplatform.uasdm.service;

import java.util.stream.Collector;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionIF;
import com.runwaysdk.system.SingleActor;

import gov.geoplatform.uasdm.GenericException;
import gov.geoplatform.uasdm.graph.UasComponent;
import gov.geoplatform.uasdm.graph.UserAccessEntity;
import net.geoprism.GeoprismUser;
import net.geoprism.GeoprismUserQuery;
import net.geoprism.account.ExternalProfile;
import net.geoprism.account.ExternalProfileQuery;

public class UserAccessEntityService
{
  @Request(RequestType.SESSION)
  public JSONArray listUsers(String sessionId, String componentId)
  {
    UasComponent component = UasComponent.getWithAccessControl(componentId).orElseThrow(() -> {
      GenericException exception = new GenericException();
      exception.setUserMessage("A component does not exist with the id [" + componentId + "]");
      throw exception;
    });

    SessionIF session = Session.getCurrentSession();

    if (session == null || !session.getUser().getOid().equals(component.getOwnerOid()))
    {
      GenericException exception = new GenericException();
      exception.setUserMessage("Only the owner see the list of users");
      throw exception;
    }

    return UserAccessEntity.getForComponent(component).stream().map(entity -> {
      SingleActor actor = SingleActor.get(entity.getUserOid());

      return toObject(component, actor);
    }).filter(o -> o != null).collect(Collector.of(JSONArray::new, JSONArray::put, JSONArray::put));
  }

  @Request(RequestType.SESSION)
  public JSONObject grantAccess(String sessionId, String componentId, String identifier)
  {
    UasComponent component = UasComponent.getWithAccessControl(componentId).orElseThrow(() -> {
      GenericException exception = new GenericException();
      exception.setUserMessage("A component does not exist with the id [" + componentId + "]");
      throw exception;
    });

    SessionIF session = Session.getCurrentSession();

    if (session == null || !session.getUser().getOid().equals(component.getOwnerOid()))
    {
      GenericException exception = new GenericException();
      exception.setUserMessage("Only the owner may grant access to a component");
      throw exception;
    }

    SingleActor actor = getActor(identifier);

    UserAccessEntity entity = UserAccessEntity.getOrCreate(actor);

    if (entity.hasEdge(component))
    {
      GenericException exception = new GenericException();
      exception.setUserMessage("The user already has access to the component");
      throw exception;
    }

    entity.addUserHasAccessChild(component).apply();

    return toObject(component, actor);
  }

  @Request(RequestType.SESSION)
  public void removeAccess(String sessionId, String componentId, String identifier)
  {
    UasComponent component = UasComponent.getWithAccessControl(componentId).orElseThrow(() -> {
      GenericException exception = new GenericException();
      exception.setUserMessage("A component does not exist with the id [" + componentId + "]");
      throw exception;
    });

    SessionIF session = Session.getCurrentSession();

    if (session == null || !session.getUser().getOid().equals(component.getOwnerOid()))
    {
      GenericException exception = new GenericException();
      exception.setUserMessage("Only the owner may remove access from a component");
      throw exception;
    }

    SingleActor actor = this.getActor(identifier);

    UserAccessEntity.getForUser(actor).ifPresent(entity -> {
      entity.removeUserHasAccessChild(component);
    });

  }

  private SingleActor getActor(String identifier)
  {
    SingleActor actor = null;

    {
      ExternalProfileQuery query = new ExternalProfileQuery(new QueryFactory());
      query.WHERE(query.getEmail().EQ(identifier));

      try (OIterator<? extends ExternalProfile> it = query.getIterator())
      {
        if (it.hasNext())
        {

          actor = it.next();
        }
      }
    }

    if (actor == null)
    {
      GeoprismUserQuery query = new GeoprismUserQuery(new QueryFactory());
      query.WHERE(query.getEmail().EQ(identifier));
      query.OR(query.getUsername().EQ(identifier));

      try (OIterator<? extends GeoprismUser> it = query.getIterator())
      {
        if (it.hasNext())
        {
          actor = it.next();
        }
      }
    }

    if (actor == null)
    {
      GenericException exception = new GenericException();
      exception.setUserMessage("Unable to find a user with the identifier [" + identifier + "]");
      throw exception;
    }

    return actor;
  }

  private JSONObject toObject(UasComponent component, SingleActor actor)
  {
    JSONObject object = new JSONObject();
    object.put("oid", actor.getOid());
    object.put("component", component.getOid());

    if (actor instanceof GeoprismUser)
    {
      object.put("type", "user");
      object.put("name", ( (GeoprismUser) actor ).getUsername());
    }
    else if (actor instanceof ExternalProfile)
    {
      object.put("type", "profile");
      object.put("name", ( (ExternalProfile) actor ).getEmail());
    }
    else
    {
      return null;
    }
    return object;
  }

}
