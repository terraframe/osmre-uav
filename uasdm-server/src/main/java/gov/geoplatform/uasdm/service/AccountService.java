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

import java.io.InputStream;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.runwaysdk.resource.StreamResource;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionIF;
import com.runwaysdk.system.SingleActor;

import gov.geoplatform.uasdm.UserInfo;
import gov.geoplatform.uasdm.UserInvite;
import gov.geoplatform.uasdm.account.CSVUserImporter;
import gov.geoplatform.uasdm.bus.Bureau;
import gov.geoplatform.uasdm.bus.InvalidPasswordException;
import gov.geoplatform.uasdm.view.IDMUserView;
import gov.geoplatform.uasdm.view.Option;
import net.geoprism.GeoprismUser;
import net.geoprism.account.GeoprismActorIF;

@Service
public class AccountService
{
  @Request(RequestType.SESSION)
  public JSONObject page(String sessionId, JSONObject criteria)
  {
    return UserInfo.page(criteria);
  }

  @Request(RequestType.SESSION)
  public JSONArray getBureaus(String sessionId)
  {
    JSONArray response = new JSONArray();

    List<Option> options = Bureau.getOptions();

    for (Option option : options)
    {
      response.put(option.toJSON());
    }

    return response;
  }

  @Request(RequestType.SESSION)
  public JSONObject get(String sessionId, String oid)
  {
    return UserInfo.getByUserId(oid);
  }

  @Request(RequestType.SESSION)
  public JSONObject apply(String sessionId, JSONObject account, String roleIds)
  {
    if (account.has(GeoprismUser.PASSWORD) && !isValidPassword(account.getString(GeoprismUser.PASSWORD)))
    {
      throw new InvalidPasswordException();
    }

    return UserInfo.applyUserWithRoles(account, roleIds);
  }
  
  @Request(RequestType.SESSION)
  public void uploadUsers(String sessionId, InputStream is, String fileName)
  {
    new CSVUserImporter(new StreamResource(is, fileName)).doImport();
  }

  @Request(RequestType.SESSION)
  public void remove(String sessionId, String oid)
  {
    UserInfo.removeByUser(oid);
  }

  @Request(RequestType.SESSION)
  public void inviteComplete(String sessionId, String token, String json)
  {
    final JSONObject object = new JSONObject(json);

    if (!isValidPassword(object.getString(GeoprismUser.PASSWORD)))
    {
      throw new InvalidPasswordException();
    }

    final GeoprismActorIF user = UserInfo.deserialize(object);

    UserInvite.complete(token, (GeoprismUser) user);
  }

  @Request(RequestType.SESSION)
  public IDMUserView getCurrentUser(String sessionId)
  {
    SessionIF session = Session.getCurrentSession();

    if (session != null)
    {
      SingleActor user = SingleActor.get(session.getUser().getOid());

      return IDMUserView.fromUser((GeoprismActorIF) user, UserInfo.getByUser(user));
    }

    return null;
  }

  public static boolean isValidPassword(String password)
  {
    // Must be at least 14 characters in length.
    // Two uppercase letters [A-Z]
    // Two lowercase letters [a-z]
    // Two digits [0-9]
    // Two special characters [e.g.: !@#$&*]

    if (password.length() < 14)
    {
      return false;
    }

    if (!password.matches("(?=.*[0-9].*[0-9]).*"))
    {
      return false;
    }

    if (!password.matches("(?=.*[a-z].*[a-z]).*"))
    {
      return false;
    }

    if (!password.matches("(?=.*[A-Z].*[A-Z]).*"))
    {
      return false;
    }

    if (!password.matches("(?=.*[~!@#$%^&*()_-].*[~!@#$%^&*()_-]).*"))
    {
      return false;
    }

    return true;
  }
}
