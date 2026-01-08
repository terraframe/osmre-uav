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
