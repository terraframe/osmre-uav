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
import net.geoprism.email.business.EmailBusinessService;
import net.geoprism.email.business.EmailBusinessServiceIF;
import net.geoprism.email.controller.EmailController;
import net.geoprism.email.service.EmailService;
import net.geoprism.email.service.EmailServiceIF;
import net.geoprism.forgotpassword.business.ForgotPasswordBusinessService;
import net.geoprism.forgotpassword.business.ForgotPasswordBusinessServiceIF;
import net.geoprism.forgotpassword.controller.ForgotPasswordController;
import net.geoprism.forgotpassword.service.ForgotPasswordService;
import net.geoprism.forgotpassword.service.ForgotPasswordServiceIF;
import net.geoprism.rbac.RoleBusinessService;
import net.geoprism.rbac.RoleBusinessServiceIF;
import net.geoprism.rbac.RoleService;
import net.geoprism.rbac.RoleServiceIF;

@Configuration
@ComponentScan(basePackages = { "net.geoprism.spring", "net.geoprism.graph", "gov.geoplatform.uasdm.controller", "gov.geoplatform.uasdm.service", "net.geoprism.account"  })
public class TestConfig
{
  @Bean
  HttpServletRequest request(){
    return new MockHttpServletRequest();
  }
  
  @Bean
  HttpServletResponse response(){
    return new MockHttpServletResponse();
  }

  @Bean
  ForgotPasswordServiceIF forgotPasswordServiceIF() {
    return new ForgotPasswordService();
  }
  
  @Bean
  ForgotPasswordBusinessServiceIF forgotPasswordBusinessServiceIF() {
    return new ForgotPasswordBusinessService();
  }
  
  @Bean
  ForgotPasswordController forgotPasswordController() {
    return new ForgotPasswordController();
  }
  
  @Bean
  RoleServiceIF roleServiceIF() {
    return new RoleService();
  }
  
  @Bean
  RoleBusinessServiceIF roleBusinessServiceIF() {
    return new RoleBusinessService();
  }
  
  @Bean
  EmailServiceIF emailService() {
    return new EmailService();
  }
  
  @Bean
  EmailBusinessServiceIF emailBusinessService() {
    return new EmailBusinessService();
  }
  
  @Bean
  EmailController emailController() {
    return new EmailController();
  }
}
