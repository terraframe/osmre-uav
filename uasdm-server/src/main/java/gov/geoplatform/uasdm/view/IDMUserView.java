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
package gov.geoplatform.uasdm.view;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.lang.Nullable;

import com.runwaysdk.query.OIterator;

import gov.geoplatform.uasdm.UserInfo;
import net.geoprism.GeoprismUser;
import net.geoprism.account.ExternalProfile;
import net.geoprism.account.GeoprismUserView;
import net.geoprism.registry.Organization;

public class IDMUserView extends GeoprismUserView
{
  @Nullable
  private String organization;

  public IDMUserView(String displayName, String email, String firstName, String lastName, String phoneNumber, String oid, String organization)
  {
    super(displayName, email, firstName, lastName, phoneNumber, oid);

    this.organization = organization;
  }

  public String getOrganization()
  {
    return organization;
  }

  public void setOrganization(String organization)
  {
    this.organization = organization;
  }

  public static IDMUserView fromUser(GeoprismUser user, UserInfo info)
  {
    // The admin user doesn't have an associated UserInfo object. So info will
    // be null in that scenario.
    String organization = null;

    if (info != null)
    {
      try (OIterator<? extends Organization> it = info.getAllOrganization())
      {
        List<String> orgs = it.getAll().stream().map(o -> o.getDisplayLabel().getValue()).collect(Collectors.toList());

        organization = String.join(",", orgs);
      }
    }

    return new IDMUserView(user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getPhoneNumber(), user.getOid(), organization);
  }

  public static IDMUserView fromUser(ExternalProfile profile, UserInfo info)
  {
    String organization = null;

    if (info != null)
    {
      try (OIterator<? extends Organization> it = info.getAllOrganization())
      {
        List<String> orgs = it.getAll().stream().map(o -> o.getDisplayLabel().getValue()).collect(Collectors.toList());

        organization = String.join(",", orgs);
      }
    }

    return new IDMUserView(profile.getUsername(), profile.getEmail(), profile.getFirstName(), profile.getLastName(), profile.getPhoneNumber(), profile.getOid(), organization);
  }
}
