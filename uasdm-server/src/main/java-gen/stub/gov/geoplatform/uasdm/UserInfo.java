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
package gov.geoplatform.uasdm;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.commongeoregistry.adapter.metadata.OrganizationDTO;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.SingleActor;

import gov.geoplatform.uasdm.bus.Bureau;
import gov.geoplatform.uasdm.processing.report.CollectionReportFacade;
import net.geoprism.GeoprismUser;
import net.geoprism.account.ExternalProfile;
import net.geoprism.account.GeoprismActorIF;
import net.geoprism.registry.Organization;
import net.geoprism.registry.service.business.AccountBusinessService;
import net.geoprism.spring.ApplicationContextHolder;

public class UserInfo extends UserInfoBase
{
  private static final long  serialVersionUID = 984575899;

  public static final String ORGANIZATION     = "organization";

  public UserInfo()
  {
    super();
  }

  public List<Organization> getOrganizations()
  {
    try (OIterator<? extends Organization> it = this.getAllOrganization())
    {
      return it.getAll().stream().map(o -> (Organization) o).collect(Collectors.toList());
    }
  }

  public static JSONObject page(JSONObject criteria)
  {
    return new UserInfoPageQuery(criteria).getPage();
  }

  public static UserInfo getUserInfo(String userId)
  {
    SingleActor user = SingleActor.get(userId);

    return UserInfo.getByUser(user);
  }

  public static JSONObject getByUserId(String userId)
  {
    SingleActor user = SingleActor.get(userId);

    UserInfo info = UserInfo.getByUser(user);

    return UserInfo.serialize((GeoprismActorIF) user, info);
  }

  @Transaction
  public static void removeByUser(String userId)
  {
    SingleActor user = SingleActor.get(userId);

    UserInfo info = UserInfo.getByUser(user);

    if (info != null)
    {
      info.delete();
    }

    CollectionReportFacade.handleDelete((GeoprismActorIF) user).doIt();

    user.delete();
  }

  @Transaction
  public static JSONObject applyUserWithRoles(JSONObject account, String roleIds)
  {
    GeoprismActorIF user = deserialize(account);

    if (user instanceof ExternalProfile)
    {
      ( (ExternalProfile) user ).setRemoteId(user.getUsername());
    }

    if (roleIds != null)
    {
      JSONArray array = new JSONArray(roleIds);
      Set<String> setRoleIds = new HashSet<String>();

      for (int i = 0; i < array.length(); i++)
      {
        setRoleIds.add(array.getString(i));
      }

      AccountBusinessService service = ApplicationContextHolder.getBean(AccountBusinessService.class);

      service.applyUserWithRoles(user, setRoleIds);
    }
    else
    {
      user.apply();
    }

    UserInfo info = getByUser((SingleActor) user);

    if (info == null)
    {
      info = new UserInfo();
      info.setGeoprismUser((SingleActor) user);
    }
    else
    {
      info.appLock();
    }

    if (account.has(UserInfo.INFORMATION))
    {
      info.setInformation(account.get(UserInfo.INFORMATION).toString());
    }
    else
    {
      info.setInformation("");
    }

    if (account.has(UserInfo.BUREAU))
    {
      String bureauId = account.getString(UserInfo.BUREAU);

      if (bureauId != null && bureauId.length() > 0)
      {
        info.setBureau(Bureau.get(bureauId));
      }
      else
      {
        info.setBureau(null);
      }
    }

    info.apply();

    // Remove the user from any organization
    try (OIterator<? extends Organization> it = info.getAllOrganization())
    {
      while (it.hasNext())
      {
        info.removeOrganization(it.next());
      }
    }

    // Assign the user to the new organization
    try (OIterator<? extends OrganizationHasUser> it = info.getAllOrganizationRel())
    {
      it.forEach(rel -> rel.delete());
    }

    if (account.has(UserInfo.ORGANIZATION) && !account.isNull(UserInfo.ORGANIZATION))
    {
      JSONObject org = account.getJSONObject(UserInfo.ORGANIZATION);
      String code = org.getString(Organization.CODE);

      if (!StringUtils.isBlank(code))
      {
        Organization organization = Organization.getByCode(code);

        info.addOrganization(organization).apply();
      }
    }

    CollectionReportFacade.update(user).doIt();

    return serialize(user, info);
  }

  private static JSONObject serialize(GeoprismActorIF user, UserInfo info)
  {
    JSONObject result = new JSONObject();
    result.put(GeoprismUser.OID, user.getOid());
    result.put(GeoprismUser.USERNAME, user.getUsername());
    result.put(GeoprismUser.FIRSTNAME, user.getFirstName());
    result.put(GeoprismUser.LASTNAME, user.getLastName());
    result.put(GeoprismUser.PHONENUMBER, user.getPhoneNumber());
    result.put(GeoprismUser.EMAIL, user.getEmail());
    result.put(GeoprismUser.INACTIVE, user.getInactive());
    result.put("externalProfile", ( user instanceof ExternalProfile ));

    if (info != null)
    {
      List<Organization> organizations = info.getOrganizations();

      if (organizations.size() > 0)
      {
        Organization organization = organizations.get(0);
        OrganizationDTO dto = organization.toDTO();

        JsonObject object = dto.toJSON();
        object.remove(OrganizationDTO.JSON_LOCALIZED_CONTACT_INFO);
        object.remove(OrganizationDTO.JSON_ENABLED);
        object.remove(OrganizationDTO.JSON_PARENT_CODE);
        object.remove(OrganizationDTO.JSON_PARENT_LABEL);

        result.put(UserInfo.ORGANIZATION, new JSONObject(object.toString()));
      }
      // result.put(UserInfo.BUREAU, info.getBureauOid());
      result.put(UserInfo.INFORMATION, info.getInformation());
    }

    result.put("newInstance", user.isNew());

    return result;
  }

  public static GeoprismActorIF deserialize(JSONObject account)
  {
    GeoprismActorIF user = null;

    if (account.has(GeoprismUser.OID))
    {
      String userId = account.getString(GeoprismUser.OID);

      user = (GeoprismActorIF) SingleActor.lock(userId);
    }
    else
    {
      if (AppProperties.requireKeycloakLogin())
      {
        user = new ExternalProfile();
      }
      else
      {
        user = new GeoprismUser();
      }
    }

    if (user instanceof ExternalProfile)
    {
      if (user.isNew())
      {
        user.setEmail(account.getString(GeoprismUser.EMAIL));
        user.setUsername(account.getString(GeoprismUser.EMAIL));
      }
    }
    else
    {
      user.setUsername(account.getString(GeoprismUser.USERNAME));
      user.setFirstName(account.getString(GeoprismUser.FIRSTNAME));
      user.setLastName(account.getString(GeoprismUser.LASTNAME));
      user.setEmail(account.getString(GeoprismUser.EMAIL));

      if (account.has(GeoprismUser.PHONENUMBER))
      {
        user.setPhoneNumber(account.getString(GeoprismUser.PHONENUMBER));
      }

      if (account.has(GeoprismUser.PASSWORD) && user instanceof GeoprismUser)
      {
        String password = account.getString(GeoprismUser.PASSWORD);

        if (password != null && password.length() > 0)
        {
          ( (GeoprismUser) user ).setPassword(password);
        }
      }
    }

    if (account.has(GeoprismUser.INACTIVE))
    {
      user.setInactive(account.getBoolean(GeoprismUser.INACTIVE));
    }

    return user;
  }

  public static UserInfo getByUser(SingleActor user)
  {
    if (user.isAppliedToDB())
    {
      UserInfoQuery query = new UserInfoQuery(new QueryFactory());
      query.WHERE(query.getGeoprismUser().EQ(user));

      try (OIterator<? extends UserInfo> it = query.getIterator())
      {

        if (it.hasNext())
        {
          return it.next();
        }
      }
    }

    return null;
  }
}
