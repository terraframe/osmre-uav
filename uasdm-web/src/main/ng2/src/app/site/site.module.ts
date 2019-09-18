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
import { AlertModule } from 'ngx-bootstrap/alert';
import { NgxPaginationModule } from 'ngx-pagination';

import { EditModalComponent } from './component/modal/edit-modal.component';
import { CreateModalComponent } from './component/modal/create-modal.component';
import { MetadataModalComponent } from './component/modal/metadata-modal.component';
import { ImagePreviewModalComponent } from './component/modal/image-preview-modal.component';
import { UploadModalComponent } from './component/modal/upload-modal.component';
import { ProjectsComponent } from './component/projects.component';
import { UploadComponent } from './component/upload.component';
import { UserProfileComponent } from './component/user-profile.component';

import { ForbiddenNameDirective } from './directive/forbidden-name.directive';
import { OnlyNumber } from './directive/number-only.directive';

import { ManagementService } from './service/management.service';
import { MapService } from './service/map.service';
import { CanDeactivateGuardService } from './service/can.deactivate.guard.service';

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
        SharedModule,
        SiteRoutingModule
    ],
    declarations: [
        EditModalComponent,
        CreateModalComponent,
        MetadataModalComponent,
        ImagePreviewModalComponent,
        UploadModalComponent,
        ProjectsComponent,
        UploadComponent,
        UserProfileComponent,
        ForbiddenNameDirective,
        OnlyNumber
    ],
    providers: [
        CanDeactivateGuardService,
        ManagementService,
        MapService,
    ],
    entryComponents: [
        UploadModalComponent,
        EditModalComponent,
        CreateModalComponent,
        ImagePreviewModalComponent,
        MetadataModalComponent
    ]
} )
export class SiteModule { }
