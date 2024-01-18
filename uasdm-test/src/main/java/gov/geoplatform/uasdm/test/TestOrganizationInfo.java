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

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.service.business.OrganizationBusinessServiceIF;
import net.geoprism.spring.ApplicationContextHolder;

public class TestOrganizationInfo
{

  protected String code;

  protected String label;

  public TestOrganizationInfo(String code, String label)
  {
    super();
    this.code = code;
    this.label = label;
  }

  public String getCode()
  {
    return code;
  }

  public void setCode(String code)
  {
    this.code = code;
  }

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }

  public void populate(ServerOrganization organization)
  {
    organization.setCode(this.getCode());
    organization.setDisplayLabel(new LocalizedValue(this.getLabel()));
  }

  public ServerOrganization apply()
  {
    OrganizationBusinessServiceIF service = ApplicationContextHolder.getBean(OrganizationBusinessServiceIF.class);
    ServerOrganization organization = ServerOrganization.getByCode(this.getCode(), false);

    if (organization == null)
    {
      organization = new ServerOrganization();
    }

    this.populate(organization);

    service.apply(organization, null);

    return organization;
  }

  public ServerOrganization getServerObject()
  {
    return ServerOrganization.getByCode(this.getCode(), false);
  }

  public void delete()
  {
    OrganizationBusinessServiceIF service = ApplicationContextHolder.getBean(OrganizationBusinessServiceIF.class);
    ServerOrganization organization = this.getServerObject();

    if (organization != null)
    {
      service.delete(organization);
    }
  }

}
