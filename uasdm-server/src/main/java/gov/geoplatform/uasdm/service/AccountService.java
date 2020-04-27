package gov.geoplatform.uasdm.service;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.business.Mutable;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.facade.FacadeUtil;
import com.runwaysdk.mvc.conversion.BasicJSONToBusinessDTO;
import com.runwaysdk.mvc.conversion.BasicJSONToComponentDTO;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;

import gov.geoplatform.uasdm.UserInfo;
import gov.geoplatform.uasdm.UserInvite;
import gov.geoplatform.uasdm.bus.Bureau;
import gov.geoplatform.uasdm.bus.InvalidPasswordException;
import gov.geoplatform.uasdm.view.Option;
import net.geoprism.GeoprismUser;
import net.geoprism.GeoprismUserDTO;

public class AccountService
{
  @Request(RequestType.SESSION)
  public JSONObject page(String sessionId, String sortAttribute, boolean order, Integer pageSize, Integer pageNumber)
  {
    return UserInfo.page(pageSize, pageNumber);
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
  public JSONObject lock(String sessionId, String oid)
  {
    return UserInfo.lockByUser(oid);
  }

  @Request(RequestType.SESSION)
  public void unlock(String sessionId, String oid)
  {
    UserInfo.unlockByUser(oid);
  }

  @Request(RequestType.SESSION)
  public JSONObject apply(String sessionId, JSONObject account, String roleIds)
  {
    if (!isValidPassword(account.getString(GeoprismUser.PASSWORD)))
    {
      throw new InvalidPasswordException();
    }

    return UserInfo.applyUserWithRoles(account, roleIds);
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

    final GeoprismUser user = UserInfo.deserialize(object);

    UserInvite.complete(token, user);
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
