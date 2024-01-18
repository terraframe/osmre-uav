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

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import net.geoprism.registry.Organization;
import net.geoprism.registry.OrganizationQuery;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.service.business.OrganizationBusinessService;
import net.geoprism.registry.service.business.OrganizationBusinessServiceIF;

@Service
@Primary
public class IDMOrganizationBusinessService extends OrganizationBusinessService implements OrganizationBusinessServiceIF
{
  public List<ServerOrganization> search(String text)
  {
    OrganizationQuery query = new OrganizationQuery(new QueryFactory());
    query.WHERE(query.getDisplayLabel().localize().LIKEi("%" + text + "%"));
    query.OR(query.getCode().LIKEi("%" + text + "%"));

    try (OIterator<? extends Organization> iterator = query.getIterator())
    {
      return iterator.getAll().stream().map(org -> ServerOrganization.get(org)).collect(Collectors.toList());
    }
  }

  @Transaction
  public void patch()
  {
    new OrganizationMigrator().run();
  }

}
