package gov.geoplatform.uasdm;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.rbac.Authenticate;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.SessionFacade;

import gov.geoplatform.uasdm.keycloak.KeycloakConstants;
import net.geoprism.RoleConstants;
import net.geoprism.account.ExternalProfile;
import net.geoprism.account.ExternalProfileQuery;
import net.geoprism.account.LocaleSerializer;

public class IDMSessionService extends IDMSessionServiceBase
{
  public static final String[] VALID_IDM_ROLES = new String[] {RoleConstants.BUILDER_ROLE, RoleConstants.ADIM_ROLE};
  
  public static final String KEYCLOAK_USERNAME_PREFIX = "keycloak-";
  
  private static final Logger logger = LoggerFactory.getLogger(IDMSessionService.class);
  
  private static final long serialVersionUID = -810320676;
  
  public IDMSessionService()
  {
    super();
  }
  
  @Authenticate
  public static java.lang.String keycloakLogin(java.lang.String userJson, java.lang.String sRoles, java.lang.String locales)
  {
    Set<String> roles = new HashSet<String>();
    
    JsonParser.parseString(sRoles).getAsJsonArray().forEach(role -> {
      if (ArrayUtils.contains(VALID_IDM_ROLES, role.getAsString())) {
        roles.add(role.getAsString());
      }
    });
    
    JsonObject joUser = JsonParser.parseString(userJson).getAsJsonObject();
    
    if (roles.size() == 0)
    {
      KeycloakNoValidRolesException ex = new KeycloakNoValidRolesException();
      ex.setUsername(joUser.get(KeycloakConstants.USERJSON_USERNAME).getAsString());
      throw ex;
    }
    
    final String username = joUser.get(KeycloakConstants.USERJSON_USERNAME).isJsonNull() ? null : joUser.get(KeycloakConstants.USERJSON_USERNAME).getAsString();
    
    SingleActorDAOIF profile = IDMSessionService.getActor(joUser, roles);

    String sessionId = SessionFacade.logIn(profile, LocaleSerializer.deserialize(locales));
    
    JsonObject json = new JsonObject();
    json.addProperty("sessionId", sessionId);
    json.addProperty("username", username);
    return json.toString();
  }
  
  @Transaction
  private static synchronized SingleActorDAOIF getActor(JsonObject joUser, Set<String> roles)
  {
    final String username = joUser.get(KeycloakConstants.USERJSON_USERNAME).isJsonNull() ? null : joUser.get(KeycloakConstants.USERJSON_USERNAME).getAsString();
    final String userid = joUser.get(KeycloakConstants.USERJSON_USERID).isJsonNull() ? null : joUser.get(KeycloakConstants.USERJSON_USERID).getAsString();
    final String firstName = joUser.get(KeycloakConstants.USERJSON_FIRSTNAME).isJsonNull() ? null : joUser.get(KeycloakConstants.USERJSON_FIRSTNAME).getAsString();
    final String lastName = joUser.get(KeycloakConstants.USERJSON_LASTNAME).isJsonNull() ? null : joUser.get(KeycloakConstants.USERJSON_LASTNAME).getAsString();
    final String phoneNumber = joUser.get(KeycloakConstants.USERJSON_PHONENUMBER).isJsonNull() ? null : joUser.get(KeycloakConstants.USERJSON_PHONENUMBER).getAsString();
    final String email = joUser.get(KeycloakConstants.USERJSON_EMAIL).isJsonNull() ? null : joUser.get(KeycloakConstants.USERJSON_EMAIL).getAsString();
    
    ExternalProfileQuery query = new ExternalProfileQuery(new QueryFactory());
    query.WHERE(query.getRemoteId().EQ(userid));
    OIterator<? extends ExternalProfile> it = query.getIterator();

    try
    {
      if (it.hasNext())
      {
        try
        {
          logger.debug("Logging in existing KeyCloak user with remote id[" + userid + "].");
          
          ExternalProfile profile = it.next();
          profile.lock();
          profile.setDisplayName(username);
          profile.setLastName(lastName);
          profile.setFirstName(firstName);
          profile.setPhoneNumber(phoneNumber);
          profile.setEmail(email);
          profile.apply();

          return (SingleActorDAOIF) BusinessFacade.getEntityDAO(profile);
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
        logger.debug("Creating new KeyCloak user with remote id[" + userid + "].");
        
        ExternalProfile profile = new ExternalProfile();
        profile.setRemoteId(userid);
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
      }
    }
    finally
    {
      it.close();
    }
  }
}
