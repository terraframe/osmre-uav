import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TreeModule } from 'angular-tree-component';
import { ContextMenuModule } from 'ngx-contextmenu';
import { ModalModule } from 'ngx-bootstrap/modal';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { TypeaheadModule } from 'ngx-bootstrap/typeahead';
import { AccordionModule } from 'ngx-bootstrap/accordion';
import { TabsModule } from 'ngx-bootstrap/tabs';
import { AlertModule } from 'ngx-bootstrap/alert';
import { NgxPaginationModule } from 'ngx-pagination';

import { MapAttributeComponent } from './component/map-attribute/map-attribute.component';
import { EntityModalComponent } from './component/modal/entity-modal.component';
import { MetadataModalComponent } from './component/modal/metadata-modal.component';
import { ImagePreviewModalComponent } from './component/modal/image-preview-modal.component';
import { UploadModalComponent } from './component/modal/upload-modal.component';
import { CollectionModalComponent } from './component/modal/collection-modal.component';
import { ProjectsComponent } from './component/projects.component';
import { UploadComponent } from './component/upload.component';
import { UserProfileComponent } from './component/user-profile.component';
import { SensorsComponent } from './component/sensor/sensors.component';
import { SensorComponent } from './component/sensor/sensor.component';
import { PlatformsComponent } from './component/platform/platforms.component';
import { PlatformComponent } from './component/platform/platform.component';

import { ForbiddenNameDirective } from './directive/forbidden-name.directive';
import { OnlyNumber } from './directive/number-only.directive';

import { ManagementService } from './service/management.service';
import { MapService } from './service/map.service';
import { CanDeactivateGuardService } from './service/can.deactivate.guard.service';
import { SensorService } from './service/sensor.service';
import { PlatformService } from './service/platform.service';

import { SiteRoutingModule } from './site-routing.module';
import { SharedModule } from '../shared/shared.module';

import '../rxjs-extensions';

@NgModule( {
    imports: [
        CommonModule,
        RouterModule,
        FormsModule,
        ReactiveFormsModule,
        NgxPaginationModule,
        TreeModule,
        ContextMenuModule,
        //        ModalModule.forRoot(),
        AlertModule,
        BsDropdownModule,
        TypeaheadModule,
        AccordionModule,
        TabsModule,
        SharedModule,
        SiteRoutingModule
    ],
    declarations: [
        MapAttributeComponent,
        EntityModalComponent,
        MetadataModalComponent,
        ImagePreviewModalComponent,
        UploadModalComponent,
        CollectionModalComponent,
        ProjectsComponent,
        UploadComponent,
        UserProfileComponent,
        ForbiddenNameDirective,
        OnlyNumber,
        SensorComponent,
        SensorsComponent,
        PlatformComponent,
        PlatformsComponent,        
    ],
    providers: [
        CanDeactivateGuardService,
        ManagementService,
        MapService,
        SensorService,
        PlatformService        
    ],
    entryComponents: [
        UploadModalComponent,
        EntityModalComponent,
        ImagePreviewModalComponent,
        MetadataModalComponent,
        SensorComponent,
        CollectionModalComponent,
        PlatformComponent        
    ]
} )
export class SiteModule { }
