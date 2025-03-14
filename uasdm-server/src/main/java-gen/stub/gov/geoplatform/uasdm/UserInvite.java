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
package gov.geoplatform.uasdm;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.rbac.Authenticate;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import net.geoprism.GeoprismUser;
import net.geoprism.registry.Organization;
import net.geoprism.registry.service.business.AccountBusinessService;
import net.geoprism.registry.service.business.EmailBusinessServiceIF;
import net.geoprism.spring.core.ApplicationContextHolder;

public class UserInvite extends UserInviteBase
{
  private static final long serialVersionUID = 1734240010;

  private static final int  expireTime       = AppProperties.getInviteUserTokenExpireTime(); // in
                                                                                             // hours

  private static Logger     logger           = LoggerFactory.getLogger(UserInvite.class);

  public UserInvite()
  {
    super();
  }

  /**
   * This is done for permissions reasons. Runway is throwing an exception when
   * we try to create a UserDTO on the client and so we're bypassing it this
   * way.
   */
  public static net.geoprism.GeoprismUser newUserInst()
  {
    return new GeoprismUser();
  }

  /**
   * MdMethod
   * 
   * Initiates a user invite request. If the user already has one in progress,
   * it will be invalidated and a new one will be issued. If the server's email
   * settings have not been properly set up, or the user does not exist, an
   * error will be thrown.
   * 
   * @param username
   */
  @Authenticate
  public static void initiate(String invite, String roleIds, String serverExternalUrl)
  {
    initiateInTrans(invite, roleIds, serverExternalUrl);
  }

  @Transaction
  public static void initiateInTrans(String sInvite, String roleIds, String serverExternalUrl)
  {
    JSONObject joInvite = new JSONObject(sInvite);

    String email = joInvite.getString("email");

    UserInvite invite = new UserInvite();
    invite.setEmail(email);

    UserInviteQuery query = new UserInviteQuery(new QueryFactory());
    query.WHERE(query.getEmail().EQi(invite.getEmail()));

    try (OIterator<? extends UserInvite> it = query.getIterator())
    {
      while (it.hasNext())
      {
        it.next().delete();
      }
    }

    invite.setStartTime(new Date());
    invite.setToken(UserInvite.generateEncryptedToken(invite.getEmail()));
    invite.setRoleIds(roleIds);

    if (joInvite.has(UserInfo.ORGANIZATION))
    {
      JSONObject organization = joInvite.getJSONObject(UserInfo.ORGANIZATION);
      String code = organization.getString(Organization.CODE);

      if (!StringUtils.isBlank(code))
      {
        invite.setOrganization(Organization.getByCode(code));
      }
    }

    invite.apply();

    invite.sendEmail(serverExternalUrl);
  }

  /**
   * MdMethod
   * 
   * Completes the user invite request by verifying the token is valid, creating
   * the requested user, and then invalidating the request.
   * 
   * @param token
   */
  @Authenticate
  public static void complete(java.lang.String token, net.geoprism.GeoprismUser user)
  {
    completeInTrans(token, user);
  }

  @Transaction
  private static void completeInTrans(java.lang.String token, net.geoprism.GeoprismUser user)
  {
    UserInvite invite = getInviteByToken(token);

    if ( ( System.currentTimeMillis() - invite.getStartTime().getTime() ) > ( expireTime * 3600000 ))
    {
      throw new InvalidUserInviteToken();
    }

    if (invite.getRoleIds().length() > 0)
    {
      JSONArray array = new JSONArray(invite.getRoleIds());
      Set<String> roleIds = new HashSet<String>();

      for (int i = 0; i < array.length(); i++)
      {
        roleIds.add(array.getString(i));
      }

      AccountBusinessService service = ApplicationContextHolder.getBean(AccountBusinessService.class);

      service.applyUserWithRoles(user, roleIds);
    }
    else
    {
      user.apply();
    }

    UserInfo info = UserInfo.getByUser(user);

    if (info == null)
    {
      info = new UserInfo();
      info.setGeoprismUser(user);
    }
    info.apply();

    Organization organization = invite.getOrganization();

    if (organization != null)
    {
      info.addOrganization(organization).apply();
    }

    invite.delete();

    logger.info("User [" + user.getUsername() + "] has been created via a user invite.");
  }

  protected static UserInvite getInviteByToken(String token)
  {
    UserInviteQuery query = new UserInviteQuery(new QueryFactory());
    query.WHERE(query.getToken().EQ(token));

    try (OIterator<? extends UserInvite> reqIt = query.getIterator())
    {

      if (reqIt.hasNext())
      {
        UserInvite invite = reqIt.next();
        invite.appLock();

        return invite;
      }
      else
      {
        throw new InvalidUserInviteToken();
      }
    }
  }

  private void sendEmail(String serverExternalUrl)
  {
    String address = this.getEmail();
    String link = serverExternalUrl + "/project/management#/admin/invite-complete/" + this.getToken();

    String subject = "UAS Data Management Account Invitation";
    String body = "Congratulations!\n" + "\n" + "You have been invited to create an account with the UAS Data Management system. To do so, click the link below:\n" + "${link}\n" + "\n" + "The above link will stop working in ${expireTime} hours.\n" + "If you did not request this, you can safely ignore this email.";
    body = body.replace("${link}", link);
    body = body.replace("${expireTime}", String.valueOf(expireTime));

    EmailBusinessServiceIF service = ApplicationContextHolder.getBean(EmailBusinessServiceIF.class);

    service.sendEmail(subject, body, new String[] { address });
  }

  private static String generateEncryptedToken(String email)
  {
    String hashedTime = UUID.nameUUIDFromBytes(String.valueOf(System.currentTimeMillis()).getBytes()).toString();

    String hashedEmail = UUID.nameUUIDFromBytes(email.getBytes()).toString();

    return hashedTime + hashedEmail;
  }
}
