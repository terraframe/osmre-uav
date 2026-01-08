package com.runwaysdk.build.domain;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import gov.geoplatform.uasdm.service.request.UploadService;

@ComponentScan(basePackages = { //
    "net.geoprism.spring", "gov.geoplatform.uasdm.service", "net.geoprism.registry.service.request", "net.geoprism.registry.service.business", "net.geoprism.registry.service.permission", "gov.geoplatform.uasdm.service.business" //
}, excludeFilters = { //
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = { UploadService.class }) //
})
public class PatchConfig
{

}
