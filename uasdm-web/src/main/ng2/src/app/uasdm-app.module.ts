import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpModule, XHRBackend, RequestOptions, Http } from '@angular/http';
import { TreeModule } from 'angular-tree-component';
import { ContextMenuModule } from 'ngx-contextmenu';
import { ModalModule } from 'ngx-bootstrap/modal';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { TypeaheadModule } from 'ngx-bootstrap/typeahead';
import { AccordionModule } from 'ngx-bootstrap/accordion';
import { AlertModule } from 'ngx-bootstrap/alert';
import { CookieService } from 'ngx-cookie-service';

import './rxjs-extensions';

import { UasdmAppComponent } from './uasdm-app.component';
import { UasdmAppRoutingModule, routedComponents } from './uasdm-app-routing.module';
import { EditModalComponent } from './management/modals/edit-modal.component';
import { CreateModalComponent } from './management/modals/create-modal.component';
import { MetadataModalComponent } from './management/modals/metadata-modal.component';
import { UasdmHeaderComponent } from './header.component';
import { ConfirmModalComponent } from './management/modals/confirm-modal.component';
import { ErrorModalComponent } from './management/modals/error-modal.component';
import { LoadingBarComponent } from './loading-bar/loading-bar.component';
import { ImagePreviewModalComponent } from './management/modals/image-preview-modal.component';

import { ForbiddenNameDirective } from './management/directives/forbidden-name.directive';

import { ManagementService } from './service/management.service';
import { MapService } from './service/map.service';
import { EventService } from './service/event.service';
import { AuthService } from './service/auth.service';
import { AdminGuardService } from './service/admin.guard.service';
import { CanDeactivateGuardService } from './service/can.deactivate.guard.service';

@NgModule( {
    imports: [
        BrowserModule,
        BrowserAnimationsModule,
        FormsModule,
        HttpModule,
        UasdmAppRoutingModule,
        ReactiveFormsModule,
        TreeModule.forRoot(),
        ContextMenuModule.forRoot(),
        ModalModule.forRoot(),
        AlertModule.forRoot(),        
        BsDropdownModule.forRoot(),
        TypeaheadModule.forRoot(),
        AccordionModule.forRoot()
    ],
    declarations: [
        UasdmAppComponent,
        EditModalComponent,
        CreateModalComponent,
        ConfirmModalComponent,
        MetadataModalComponent,
        ErrorModalComponent,
        LoadingBarComponent,
        ImagePreviewModalComponent,

        // Routing components
        routedComponents,
        UasdmHeaderComponent,
        ForbiddenNameDirective
    ],
    providers: [
        AdminGuardService,
        CanDeactivateGuardService,
        CookieService,
        AuthService,
        ManagementService,
        EventService,
        MapService
    ],
    bootstrap: [UasdmAppComponent],
    entryComponents: [EditModalComponent, CreateModalComponent, ImagePreviewModalComponent, ConfirmModalComponent, MetadataModalComponent, ErrorModalComponent]
} )
export class UasdmAppModule { }
