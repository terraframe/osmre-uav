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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.rbac.Authenticate;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.SessionFacade;

import gov.geoplatform.uasdm.keycloak.KeycloakConstants;
import gov.geoplatform.uasdm.keycloak.UnassignedKeycloakUserException;
import net.geoprism.account.ExternalProfile;
import net.geoprism.account.ExternalProfileQuery;
import net.geoprism.account.GeoprismActorIF;
import net.geoprism.account.LocaleSerializer;
import net.geoprism.rbac.RoleConstants;

public class IDMSessionService extends IDMSessionServiceBase
{
  public static final String ADMIN_RENAME_ROLE = "gov.geoplatform.aim.Administrator";
  
  public static final String FIELD_WORKER_RENAME_ROLE = "gov.geoplatform.aim.FieldWorker";
  
  public static final String[] VALID_IDM_ROLES = new String[] {RoleConstants.DASHBOARD_BUILDER, RoleConstants.ADMIN, ADMIN_RENAME_ROLE, FIELD_WORKER_RENAME_ROLE};
  
  public static final String KEYCLOAK_USERNAME_PREFIX = "keycloak-";
  
  private static final Logger logger = LoggerFactory.getLogger(IDMSessionService.class);
  
  private static final long serialVersionUID = -810320676;
  
  public IDMSessionService()
  {
    super();
  }
  
  /*
  private static Set<String> deserializeRoles(String sRoles)
  {
    JsonArray preRoles = JsonParser.parseString(sRoles).getAsJsonArray();
    
    Set<String> postRoles = new HashSet<String>();
    
    for (int i = 0; i < preRoles.size(); ++i)
    {
      String preRole = preRoles.get(i).getAsString();
      String postRole = preRole;
      
      if (preRole.equals(ADMIN_RENAME_ROLE))
      {
        postRole = RoleConstants.ADMIN;
      }
      else if (preRole.equals(FIELD_WORKER_RENAME_ROLE))
      {
        postRole = RoleConstants.DASHBOARD_BUILDER;
      } 
      
      if (postRole != null && ArrayUtils.contains(VALID_IDM_ROLES, postRole))
      {
        postRoles.add(postRole);
      }
    }
    
    return postRoles;
  }
  */
  
  @Authenticate
  public static java.lang.String keycloakLogin(java.lang.String userJson, java.lang.String sRoles, java.lang.String locales)
  {
    // Set<String> roles = deserializeRoles(sRoles);
    
    JsonObject joUser = JsonParser.parseString(userJson).getAsJsonObject();
    
    /*
    if (roles.size() == 0)
    {
      KeycloakNoValidRolesException ex = new KeycloakNoValidRolesException();
      ex.setUsername(joUser.get(KeycloakConstants.USERJSON_USERNAME).getAsString());
      throw ex;
    }
    */
    
    final String username = joUser.get(KeycloakConstants.USERJSON_USERNAME).isJsonNull() ? null : joUser.get(KeycloakConstants.USERJSON_USERNAME).getAsString();
    
    ExternalProfile profile = IDMSessionService.getProfile(joUser);
    
    if (Boolean.TRUE.equals(((GeoprismActorIF)profile).getInactive()))
    {
      net.geoprism.account.InactiveUserException ex = new net.geoprism.account.InactiveUserException("User [" + ((GeoprismActorIF)profile).getUsername() + "] is inactive");
      throw ex;
    }

    String sessionId = SessionFacade.logIn(((SingleActorDAOIF)BusinessFacade.getEntityDAO(profile)), LocaleSerializer.deserialize(locales));
    
    JsonObject json = new JsonObject();
    json.addProperty("sessionId", sessionId);
    json.addProperty("username", username);
    return json.toString();
  }
  
  @Transaction
  private static synchronized ExternalProfile getProfile(JsonObject joUser)
  {
    final String username = joUser.get(KeycloakConstants.USERJSON_USERNAME).isJsonNull() ? null : joUser.get(KeycloakConstants.USERJSON_USERNAME).getAsString();
    final String userid = joUser.get(KeycloakConstants.USERJSON_USERID).isJsonNull() ? null : joUser.get(KeycloakConstants.USERJSON_USERID).getAsString();
    final String firstName = joUser.get(KeycloakConstants.USERJSON_FIRSTNAME).isJsonNull() ? null : joUser.get(KeycloakConstants.USERJSON_FIRSTNAME).getAsString();
    final String lastName = joUser.get(KeycloakConstants.USERJSON_LASTNAME).isJsonNull() ? null : joUser.get(KeycloakConstants.USERJSON_LASTNAME).getAsString();
    final String phoneNumber = joUser.get(KeycloakConstants.USERJSON_PHONENUMBER).isJsonNull() ? null : joUser.get(KeycloakConstants.USERJSON_PHONENUMBER).getAsString();
    final String email = joUser.get(KeycloakConstants.USERJSON_EMAIL).isJsonNull() ? null : joUser.get(KeycloakConstants.USERJSON_EMAIL).getAsString();
    
    ExternalProfileQuery query = new ExternalProfileQuery(new QueryFactory());
    query.WHERE(query.getEmail().EQi(email));
    OIterator<? extends ExternalProfile> it = query.getIterator();

    try
    {
      if (it.hasNext())
      {
        try
        {
          logger.debug("Logging in existing KeyCloak user with email [" + email + "].");
          
          ExternalProfile profile = it.next();
          profile.lock();
          profile.setRemoteId(userid);
          profile.setDisplayName(username);
          profile.setLastName(lastName);
          profile.setFirstName(firstName);
          profile.setPhoneNumber(phoneNumber);
          profile.apply();
          
          /*
          SingleActorDAOIF dao = (SingleActorDAOIF) BusinessFacade.getEntityDAO(profile);
          
          RoleBusinessService service = ApplicationContextHolder.getBean(RoleBusinessService.class);
          
          List<Roles> allRoles = service.getAllAssignableRoles();
          for (Roles role : allRoles)
          {
            RoleDAO roleDAO = RoleDAO.get(role.getOid()).getBusinessDAO();

            if (roles.contains(role.getRoleName()))
            {
              roleDAO.assignMember(dao);
            }
            else
            {
              roleDAO.deassignMember(dao);
            }
          }
          */

          return profile;
        }
        catch (Throwable t)
        {
          String msg = "Encountered an unexpected error while logging user in.";
          
          logger.error(msg, t);
          
          throw new ProgrammingErrorException(msg, t);
        }
      }
      else
      {
        UnassignedKeycloakUserException ex = new UnassignedKeycloakUserException();
        ex.setEmail(email);
        throw ex;
        
        /*
        logger.debug("Creating new KeyCloak user with remote id[" + username + "].");
        
        ExternalProfile profile = new ExternalProfile();
        profile.setRemoteId(username);
        profile.setDisplayName(username);
        profile.setLastName(lastName);
        profile.setFirstName(firstName);
        profile.setPhoneNumber(phoneNumber);
        profile.setEmail(email);
        profile.apply();

        SingleActorDAOIF actor = (SingleActorDAOIF) BusinessFacade.getEntityDAO(profile);

        for (String roleName : roles)
        {
          RoleDAO role = RoleDAO.findRole(roleName).getBusinessDAO();
          role.assignMember(actor);
        }

        return actor;
        */
      }
    }
    finally
    {
      it.close();
    }
  }
}
