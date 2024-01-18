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
/**
 *
 */
package gov.geoplatform.uasdm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import gov.geoplatform.uasdm.mock.MockHttpServletRequest;
import gov.geoplatform.uasdm.mock.MockHttpServletResponse;
import net.geoprism.registry.service.business.RoleBusinessService;
import net.geoprism.registry.service.business.RoleBusinessServiceIF;
import net.geoprism.registry.service.request.RoleService;
import net.geoprism.registry.service.request.RoleServiceIF;

@Configuration
@ComponentScan(basePackages = { "net.geoprism.registry.service", "net.geoprism.registry.spring", "net.geoprism.spring", "net.geoprism.graph", "gov.geoplatform.uasdm.controller", "gov.geoplatform.uasdm.service", "net.geoprism.account" })
public class TestConfig
{
  @Bean
  HttpServletRequest request()
  {
    return new MockHttpServletRequest();
  }

  @Bean
  HttpServletResponse response()
  {
    return new MockHttpServletResponse();
  }
}
