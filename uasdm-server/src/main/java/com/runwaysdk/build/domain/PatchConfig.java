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
package com.runwaysdk.build.domain;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@ComponentScan(basePackages = { //
    "net.geoprism.spring", //
    "gov.geoplatform.uasdm.service", //
    "net.geoprism.registry.service.request", //
    "net.geoprism.registry.service.business", //
    "net.geoprism.registry.service.permission", //
    "gov.geoplatform.uasdm.service.business" //
}, excludeFilters = { //
    @ComponentScan.Filter(type = FilterType.REGEX, pattern = {  //
        ".*UploadService", //
        ".*JsonExceptionHandler", //
        ".*IDMSessionService", //
        ".*SessionService", //
        ".*UASDMLoginGuard", //
        ".*LogErrorFilter" //
    })
})
public class PatchConfig
{

}
