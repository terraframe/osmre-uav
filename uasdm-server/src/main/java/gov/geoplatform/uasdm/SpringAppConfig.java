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

import java.util.List;

import javax.servlet.Filter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import net.geoprism.EncodingFilter;
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
import net.geoprism.session.LoginBruteForceGuardService;
import net.geoprism.session.SessionController;
import net.geoprism.session.SessionFilter;
import net.geoprism.spring.JsonExceptionHandler;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = { "net.geoprism.spring", "net.geoprism.graph", "gov.geoplatform.uasdm.controller", "gov.geoplatform.uasdm.service", "net.geoprism.account" })
public class SpringAppConfig extends WebMvcConfigurationSupport
{
  // @Bean(name = "multipartResolver")
  // public CommonsMultipartResolver multipartResolver()
  // {
  // CommonsMultipartResolver multipartResolver = new
  // CommonsMultipartResolver();
  // multipartResolver.setMaxUploadSize(-1);
  // return multipartResolver;
  // }

  // @Bean
  // @Override
  // public FormattingConversionService mvcConversionService()
  // {
  // DefaultFormattingConversionService conversionService = new
  // DefaultFormattingConversionService(false);
  //
  // DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("GMT");
  //// dateTimeFormatter.withZone(ZoneId.of(GeoRegistryUtil.SYSTEM_TIMEZONE.getID()));
  //
  // DateTimeFormatterRegistrar dateTimeRegistrar = new
  // DateTimeFormatterRegistrar();
  // dateTimeRegistrar.setDateFormatter(dateTimeFormatter);
  // dateTimeRegistrar.setDateTimeFormatter(dateTimeFormatter);
  // dateTimeRegistrar.registerFormatters(conversionService);
  //
  // DateFormatter dateFormatter = new DateFormatter("GMT");
  //// dateFormatter.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);
  //
  // DateFormatterRegistrar dateRegistrar = new DateFormatterRegistrar();
  // dateRegistrar.setFormatter(dateFormatter);
  // dateRegistrar.registerFormatters(conversionService);
  //
  // return conversionService;
  // }

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters)
  {
    // GsonHttpMessageConverter msgConverter = new GsonHttpMessageConverter();
    // Gson gson = new GsonBuilder().setPrettyPrinting().create();
    // msgConverter.setGson(gson);
    // converters.add(msgConverter);
  }

  @Bean
  Filter sessionFilter()
  {
    return new SessionFilter();
  }

  @Bean
  JsonExceptionHandler errorHandler()
  {
    return new JsonExceptionHandler();
  }

  @Bean
  SessionController sessionController()
  {
    return new SessionController();
  }

  @Bean
  EncodingFilter encodingFilter()
  {
    return new EncodingFilter();
  }

  @Bean
  ForgotPasswordServiceIF forgotPasswordServiceIF()
  {
    return new ForgotPasswordService();
  }

  @Bean
  ForgotPasswordBusinessServiceIF forgotPasswordBusinessServiceIF()
  {
    return new ForgotPasswordBusinessService();
  }

  @Bean
  ForgotPasswordController forgotPasswordController()
  {
    return new ForgotPasswordController();
  }

  @Bean
  RoleServiceIF roleServiceIF()
  {
    return new RoleService();
  }

  @Bean
  RoleBusinessServiceIF roleBusinessServiceIF()
  {
    return new RoleBusinessService();
  }

  @Bean
  EmailServiceIF emailService()
  {
    return new EmailService();
  }

  @Bean
  EmailBusinessServiceIF emailBusinessService()
  {
    return new EmailBusinessService();
  }

  @Bean
  EmailController emailController()
  {
    return new EmailController();
  }

  @Bean
  LoginBruteForceGuardService loginBruteForceGuard()
  {
    return new LoginBruteForceGuardService();
  }
}
