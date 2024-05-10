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
package gov.geoplatform.uasdm.service.request;

import java.util.List;
import java.util.stream.Collectors;

import org.commongeoregistry.adapter.metadata.OrganizationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.uasdm.service.business.IDMOrganizationBusinessService;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.service.request.OrganizationService;
import net.geoprism.registry.service.request.OrganizationServiceIF;

@Service
@Primary
public class IDMOrganizationService extends OrganizationService implements OrganizationServiceIF
{
  @Autowired
  private IDMOrganizationBusinessService service;

  @Request(RequestType.SESSION)
  public List<OrganizationDTO> search(String sessionId, String text)
  {
    List<ServerOrganization> organizations = this.service.search(text);

    return organizations.stream().map(org -> org.toDTO()).collect(Collectors.toList());
  }

  @Request(RequestType.SESSION)
  public void patch(String sessionId)
  {
    this.service.patch();
  }

}
