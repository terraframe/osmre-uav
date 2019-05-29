package gov.geoplatform.uasdm.service;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.uasdm.UserInfo;
import gov.geoplatform.uasdm.bus.Bureau;
import gov.geoplatform.uasdm.view.Option;

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
    return UserInfo.applyUserWithRoles(account, roleIds);
  }

  @Request(RequestType.SESSION)
  public void remove(String sessionId, String oid)
  {
    UserInfo.removeByUser(oid);
  }
}
