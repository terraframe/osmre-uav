package gov.geoplatform.uasdm;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.dataaccess.ValueObject;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.LeftJoinEq;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.ValueQuery;

import gov.geoplatform.uasdm.bus.Bureau;
import gov.geoplatform.uasdm.bus.BureauQuery;
import net.geoprism.GeoprismUser;
import net.geoprism.GeoprismUserQuery;

public class UserInfo extends UserInfoBase
{
  private static final long serialVersionUID = 984575899;

  public UserInfo()
  {
    super();
  }

  public static JSONObject page(Integer pageSize, Integer pageNumber)
  {
    ValueQuery vQuery = new ValueQuery(new QueryFactory());

    GeoprismUserQuery uQuery = new GeoprismUserQuery(vQuery);
    UserInfoQuery iQuery = new UserInfoQuery(vQuery);
    BureauQuery bQuery = new BureauQuery(vQuery);

    vQuery.WHERE(new LeftJoinEq(uQuery.getOid(), iQuery.getGeoprismUser()));
    vQuery.WHERE(new LeftJoinEq(iQuery.getBureau(), bQuery.getOid()));

    vQuery.SELECT(uQuery.getOid(), uQuery.getUsername(), uQuery.getFirstName(), uQuery.getLastName(), uQuery.getPhoneNumber(), uQuery.getEmail());
    vQuery.SELECT(bQuery.getName());
    vQuery.ORDER_BY_ASC(uQuery.getUsername());

    JSONArray results = new JSONArray();

    OIterator<ValueObject> it = vQuery.getIterator(pageSize, pageNumber);

    try
    {
      while (it.hasNext())
      {
        ValueObject vObject = it.next();

        JSONObject result = new JSONObject();
        result.put(GeoprismUser.OID, vObject.getValue(GeoprismUser.OID));
        result.put(GeoprismUser.USERNAME, vObject.getValue(GeoprismUser.USERNAME));
        result.put(GeoprismUser.FIRSTNAME, vObject.getValue(GeoprismUser.FIRSTNAME));
        result.put(GeoprismUser.LASTNAME, vObject.getValue(GeoprismUser.LASTNAME));
        result.put(GeoprismUser.PHONENUMBER, vObject.getValue(GeoprismUser.PHONENUMBER));
        result.put(GeoprismUser.EMAIL, vObject.getValue(GeoprismUser.EMAIL));
        result.put(UserInfo.BUREAU, vObject.getValue(Bureau.NAME));

        results.put(result);
      }
    }
    finally
    {
      it.close();
    }

    JSONObject page = new JSONObject();
    page.put("resultSet", results);
    page.put("count", vQuery.getCount());
    page.put("pageNumber", pageNumber);
    page.put("pageSize", pageSize);

    return page;

  }

  @Transaction
  public static JSONObject lockByUser(String userId)
  {
    GeoprismUser user = GeoprismUser.lock(userId);

    UserInfo info = UserInfo.getByUser(user);

    if (info != null)
    {
      info.lock();
    }

    return UserInfo.serialize(user, info);
  }

  @Transaction
  public static void unlockByUser(String userId)
  {
    GeoprismUser user = GeoprismUser.unlock(userId);

    UserInfo info = UserInfo.getByUser(user);

    if (info != null)
    {
      info.unlock();
    }
  }

  @Transaction
  public static void removeByUser(String userId)
  {
    GeoprismUser user = GeoprismUser.get(userId);

    UserInfo info = UserInfo.getByUser(user);

    if (info != null)
    {
      info.delete();
    }

    user.delete();
  }

  @Transaction
  public static JSONObject applyUserWithRoles(JSONObject account, String roleIds)
  {
    GeoprismUser user = deserialize(account);

    if (roleIds != null)
    {
      JSONArray array = new JSONArray(roleIds);
      List<String> list = new LinkedList<String>();

      for (int i = 0; i < array.length(); i++)
      {
        list.add(array.getString(i));
      }

      user.applyWithRoles(list.toArray(new String[list.size()]));
    }
    else
    {
      user.apply();
    }

    UserInfo info = getByUser(user);

    if (info == null)
    {
      info = new UserInfo();
      info.setGeoprismUser(user);
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

    return serialize(user, info);
  }

  private static JSONObject serialize(GeoprismUser user, UserInfo info)
  {
    JSONObject result = new JSONObject();
    result.put(GeoprismUser.OID, user.getOid());
    result.put(GeoprismUser.USERNAME, user.getUsername());
    result.put(GeoprismUser.FIRSTNAME, user.getFirstName());
    result.put(GeoprismUser.LASTNAME, user.getLastName());
    result.put(GeoprismUser.PHONENUMBER, user.getPhoneNumber());
    result.put(GeoprismUser.EMAIL, user.getEmail());
    result.put(GeoprismUser.INACTIVE, user.getInactive());

    if (info != null)
    {
      result.put(UserInfo.BUREAU, info.getBureauOid());
    }

    result.put("newInstance", user.isNew());

    return result;
  }

  private static GeoprismUser deserialize(JSONObject account)
  {
    GeoprismUser user = null;

    if (account.has(GeoprismUser.OID))
    {
      String userId = account.getString(GeoprismUser.OID);

      user = GeoprismUser.get(userId);
    }
    else
    {
      user = new GeoprismUser();
    }

    user.setUsername(account.getString(GeoprismUser.USERNAME));
    user.setFirstName(account.getString(GeoprismUser.FIRSTNAME));
    user.setLastName(account.getString(GeoprismUser.LASTNAME));
    user.setEmail(account.getString(GeoprismUser.EMAIL));

    if (account.has(GeoprismUser.PHONENUMBER))
    {
      user.setPhoneNumber(account.getString(GeoprismUser.PHONENUMBER));
    }

    if (account.has(GeoprismUser.INACTIVE))
    {
      user.setInactive(account.getBoolean(GeoprismUser.INACTIVE));
    }

    if (account.has(GeoprismUser.PASSWORD))
    {
      String password = account.getString(GeoprismUser.PASSWORD);

      if (password != null && password.length() > 0)
      {
        user.setPassword(password);
      }
    }

    return user;
  }

  public static UserInfo getByUser(GeoprismUser user)
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
