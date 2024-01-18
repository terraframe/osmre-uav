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
package gov.geoplatform.uasdm.service.business;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.runwaysdk.system.Roles;

import net.geoprism.registry.service.business.RoleBusinessService;
import net.geoprism.registry.service.business.RoleBusinessServiceIF;

@Service
@Primary
public class IDMRoleBusinessService extends RoleBusinessService implements RoleBusinessServiceIF
{
  @Override
  public List<Roles> getAllAssignableRoles()
  {
    // Exclude all roles created for imported organizations
    List<Roles> roles = super.getAllAssignableRoles();
    roles = roles.stream().filter(role -> !role.getRoleName().startsWith("cgr")).collect(Collectors.toList());

    return roles;
  }
}
