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
package gov.geoplatform.uasdm.test;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

import net.geoprism.GeoprismUser;
import net.geoprism.GeoprismUserQuery;

public class TestUserInfo
{
  
  private String username;
  
  private String password;
  
  private String email;
  
  private String[] roleNameArray;
  
  public TestUserInfo(String username, String password, String email, String[] roleNameArray)
  {
    this.username = username;
    this.password = password;
    this.email = email;
    this.roleNameArray = roleNameArray == null ? new String[] {} : roleNameArray;
  }
  
  public void delete()
  {
    TestDataSet.deleteUser(username);
  }
  
  @Request
  public GeoprismUser apply()
  {
    GeoprismUser user = this.getGeoprismUser();
    
    if (user != null)
    {
      return user;
    }
    else
    {
      return TestDataSet.createUser(username, password, email, roleNameArray);
    }
  }
  
  public GeoprismUser getGeoprismUser()
  {
    GeoprismUserQuery query = new GeoprismUserQuery(new QueryFactory());
    query.WHERE(query.getUsername().EQ(this.username));
    OIterator<? extends GeoprismUser> it = query.getIterator();
    
    try
    {
      if (it.hasNext())
      {
        return it.next();
      }
      else
      {
        return null;
      }
    }
    finally
    {
      it.close();
    }
  }

  public String getUsername()
  {
    return username;
  }

  public void setUsername(String username)
  {
    this.username = username;
  }

  public String getPassword()
  {
    return password;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }

  public String[] getRoleNameArray()
  {
    return roleNameArray;
  }

  public void setRoleNameArray(String[] roleNameArray)
  {
    this.roleNameArray = roleNameArray;
  }
  
}