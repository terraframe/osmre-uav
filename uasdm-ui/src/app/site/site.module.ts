///
///
///

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

//import { TreeModule } from 'angular-tree-component';
//import { ContextMenuModule } from 'ngx-contextmenu';
import { NgxFileDropModule } from 'ngx-file-drop';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { TypeaheadModule } from 'ngx-bootstrap/typeahead';
import { AccordionModule } from 'ngx-bootstrap/accordion';
import { TabsModule } from 'ngx-bootstrap/tabs';
import { AlertModule } from 'ngx-bootstrap/alert';
import { NgxPaginationModule } from 'ngx-pagination';
import { CollapseModule } from 'ngx-bootstrap/collapse';
import { PopoverModule } from 'ngx-bootstrap/popover';


import { ProductService } from './service/product.service';
import { ManagementService } from './service/management.service';
import { MapService } from './service/map.service';
import { SensorService } from './service/sensor.service';
import { PlatformService } from './service/platform.service';
import { MetadataService } from './service/metadata.service';
import { ClassificationService } from './service/classification.service';

import { SiteRoutingModule } from './site-routing.module';
import { SharedModule } from '../shared/shared.module';
import { ReportService } from './service/report.service';
import { UserAccessService } from './service/user-access.service';
import { KnowStacService } from './service/know-stac.service';
import { UploadService } from './service/upload.service';
import { UAVService } from './service/uav.service';

@NgModule({
    imports: [
        CommonModule,
        RouterModule,
        FormsModule,
        NgxPaginationModule,
        //        TreeModule,
        //        ContextMenuModule,
        //        ModalModule.forRoot(),
        AlertModule,
        BsDropdownModule,
        TypeaheadModule,
        AccordionModule,
        TabsModule.forRoot(),
        CollapseModule.forRoot(),
        SharedModule,
        SiteRoutingModule,
        PopoverModule.forRoot(),
        NgxFileDropModule
    ],
    providers: [
        ManagementService,
        ProductService,
        MapService,
        SensorService,
        PlatformService,
        MetadataService,
        ClassificationService,
        UAVService,
        ReportService,
        UserAccessService,
        KnowStacService,
        UploadService
    ]
})
export class SiteModule { }
