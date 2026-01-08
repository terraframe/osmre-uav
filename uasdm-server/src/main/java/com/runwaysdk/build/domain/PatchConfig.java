package com.runwaysdk.build.domain;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import gov.geoplatform.uasdm.LogErrorFilter;
import gov.geoplatform.uasdm.service.UASDMLoginGuard;
import gov.geoplatform.uasdm.service.request.IDMSessionService;
import gov.geoplatform.uasdm.service.request.UploadService;
import net.geoprism.spring.web.JsonExceptionHandler;

@ComponentScan(basePackages = { //
    "net.geoprism.spring", //
    "gov.geoplatform.uasdm.service", //
    "net.geoprism.registry.service.request", //
    "net.geoprism.registry.service.business", //
    "net.geoprism.registry.service.permission", //
    "gov.geoplatform.uasdm.service.business" //
}, excludeFilters = { //
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {  //
        UploadService.class, //
        JsonExceptionHandler.class, //
        IDMSessionService.class, //
        UASDMLoginGuard.class, //
        LogErrorFilter.class //
    })
})
public class PatchConfig
{

}
