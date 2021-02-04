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
  public static java.lang.String keycloakLogin(java.lang.String username, java.lang.String sRoles, java.lang.String locales)
  {
    Set<String> roles = new HashSet<String>();
    
    JsonParser.parseString(sRoles).getAsJsonArray().forEach(role -> {
      if (ArrayUtils.contains(VALID_IDM_ROLES, role.getAsString())) {
        roles.add(role.getAsString());
      }
    });
    
    if (roles.size() == 0)
    {
      KeycloakNoValidRolesException ex = new KeycloakNoValidRolesException();
      ex.setUsername(username);
      throw ex;
    }
    
    SingleActorDAOIF profile = IDMSessionService.getActor(username, roles);

    String sessionId = SessionFacade.logIn(profile, LocaleSerializer.deserialize(locales));
    
    JsonObject json = new JsonObject();
    json.addProperty("sessionId", sessionId);
    json.addProperty("username", username);
    return json.toString();
  }
  
  @Transaction
  private static synchronized SingleActorDAOIF getActor(String remoteId, Set<String> roles)
  {
    ExternalProfileQuery query = new ExternalProfileQuery(new QueryFactory());
    query.WHERE(query.getRemoteId().EQ(remoteId));
    OIterator<? extends ExternalProfile> it = query.getIterator();

    try
    {
      if (it.hasNext())
      {
        try
        {
          logger.debug("Logging in existing KeyCloak user with remote id[" + remoteId + "].");
          
          ExternalProfile profile = it.next();
          profile.lock();
//          profile.setDisplayName(value); // TODO
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
        logger.debug("Creating new KeyCloak user with remote id[" + remoteId + "].");
        
        ExternalProfile profile = new ExternalProfile();
        profile.setRemoteId(remoteId);
//        profile.setDisplayName(value); // TODO
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
