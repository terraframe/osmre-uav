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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.dataaccess.ValueObject;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.BasicLeftJoinEq;
import com.runwaysdk.query.InnerJoinEq;
import com.runwaysdk.query.LeftJoinEq;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.OrderBy.SortOrder;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.Selectable;
import com.runwaysdk.query.SelectableChar;
import com.runwaysdk.query.ValueQuery;
import com.runwaysdk.system.SingleActor;

import gov.geoplatform.uasdm.bus.Bureau;
import gov.geoplatform.uasdm.bus.BureauQuery;
import gov.geoplatform.uasdm.bus.CollectionReport;
import net.geoprism.GeoprismUser;
import net.geoprism.GeoprismUserQuery;

public class UserInfo extends UserInfoBase
{
  private static final long serialVersionUID = 984575899;

  public UserInfo()
  {
    super();
  }

  @SuppressWarnings("unchecked")
  public static JSONObject page(JSONObject criteria)
  {
    ValueQuery vQuery = new ValueQuery(new QueryFactory());

    GeoprismUserQuery uQuery = new GeoprismUserQuery(vQuery);
    UserInfoQuery iQuery = new UserInfoQuery(vQuery);
    BureauQuery bQuery = new BureauQuery(vQuery);

    SelectableChar bureau = bQuery.getName(UserInfo.BUREAU);

    vQuery.SELECT(uQuery.getOid(), uQuery.getUsername(), uQuery.getFirstName(), uQuery.getLastName(), uQuery.getPhoneNumber(), uQuery.getEmail());
    vQuery.SELECT(bureau);

    vQuery.WHERE(new LeftJoinEq(uQuery.getOid(), iQuery.getGeoprismUser()));
    // vQuery.WHERE(new InnerJoinEq(iQuery.getBureau(UserInfo.BUREAU),
    // bQuery.getOid()));
    vQuery.WHERE(new BasicLeftJoinEq(iQuery.getBureau(UserInfo.BUREAU), bQuery.getOid()));

    if (criteria.has("filters"))
    {
      JSONObject filters = criteria.getJSONObject("filters");
      Iterator<String> keys = filters.keys();

      while (keys.hasNext())
      {
        String attributeName = keys.next();

        Selectable attribute = null;

        if (attributeName.equals(UserInfo.BUREAU))
        {
          attribute = bureau;
        }
        else
        {
          attribute = uQuery.get(attributeName);
        }

        if (attribute != null)
        {
          JSONObject filter = filters.getJSONObject(attributeName);

          String value = filter.get("value").toString();
          String mode = filter.get("matchMode").toString();

          if (mode.equals("contains"))
          {
            SelectableChar selectable = (SelectableChar) attribute;

            vQuery.WHERE(selectable.LIKEi("%" + value + "%"));
          }
          else if (mode.equals("equals"))
          {
            vQuery.WHERE(attribute.EQ(value));
          }
        }
      }
    }

    int pageSize = 10;
    int pageNumber = 1;

    if (criteria.has("first") && criteria.has("rows"))
    {
      int first = criteria.getInt("first");
      pageSize = criteria.getInt("rows");
      pageNumber = ( first / pageSize ) + 1;

      // vQuery.restrictRows(pageSize, pageNumber);
    }

    if (criteria.has("sortField") && criteria.has("sortOrder"))
    {
      String field = criteria.getString("sortField");
      SortOrder order = criteria.getInt("sortOrder") == 1 ? SortOrder.ASC : SortOrder.DESC;

      if (field.equals(UserInfo.BUREAU))
      {
        vQuery.ORDER_BY(bureau, order);
      }
      else
      {
        vQuery.ORDER_BY(uQuery.getS(field), order);
      }
    }

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
        result.put(UserInfo.BUREAU, vObject.getValue(UserInfo.BUREAU));

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

    CollectionReport.handleDelete(user);

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

    CollectionReport.update(user);

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
      result.put(UserInfo.INFORMATION, info.getInformation());
    }

    result.put("newInstance", user.isNew());

    return result;
  }

  public static GeoprismUser deserialize(JSONObject account)
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
