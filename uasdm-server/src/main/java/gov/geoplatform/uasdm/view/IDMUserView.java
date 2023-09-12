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
package gov.geoplatform.uasdm.view;

import org.springframework.lang.Nullable;

import gov.geoplatform.uasdm.UserInfo;
import net.geoprism.GeoprismUser;
import net.geoprism.account.GeoprismUserView;

public class IDMUserView extends GeoprismUserView
{
  @Nullable
  private String bureau;

  public IDMUserView(String displayName, String email, String firstName, String lastName, String phoneNumber, String oid, String bureau)
  {
    super(displayName, email, firstName, lastName, phoneNumber, oid);
    this.bureau = bureau;
  }
  
  public static IDMUserView fromUser(GeoprismUser user, UserInfo info)
  {
    // The admin user doesn't have an associated UserInfo object. So info will be null in that scenario.
    String bureau = null;
    if (info != null && info.getBureau() != null)
    {
      bureau = info.getBureau().getDisplayLabel();
    }
    
    IDMUserView view = new IDMUserView(user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getPhoneNumber(), user.getOid(), bureau);
    return view;
  }

  public String getBureau()
  {
    return bureau;
  }

  public void setBureau(String bureau)
  {
    this.bureau = bureau;
  }
}
