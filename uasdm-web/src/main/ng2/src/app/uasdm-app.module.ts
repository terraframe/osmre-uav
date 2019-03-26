import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpModule, XHRBackend, RequestOptions, Http } from '@angular/http';
import { ModalModule } from 'ngx-bootstrap/modal';
import { TreeModule } from 'angular-tree-component';
import { ContextMenuModule } from 'ngx-contextmenu';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
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

import { ManagementService } from './management/management.service';
import { MapService } from './service/map.service';
import { SearchService } from './management/search.service';
import { EventService } from './event/event.service';
import { AuthService } from './auth/auth.service';
import { AdminGuard } from './auth/admin.guard';

@NgModule( {
    imports: [
        BrowserModule,
        FormsModule,
        HttpModule,
        UasdmAppRoutingModule,
        ReactiveFormsModule,
        TreeModule.forRoot(),
        ContextMenuModule.forRoot(),
        ModalModule.forRoot(),
        AlertModule.forRoot(),        
        BsDropdownModule.forRoot(),
    ],
    declarations: [
        UasdmAppComponent,
        EditModalComponent,
        CreateModalComponent,
        ConfirmModalComponent,
        MetadataModalComponent,
        ErrorModalComponent,
        LoadingBarComponent,

        // Routing components
        routedComponents,
        UasdmHeaderComponent,
    ],
    providers: [
        AdminGuard,
        CookieService,
        AuthService,
        ManagementService,
        EventService,
        SearchService,
        MapService
    ],
    bootstrap: [UasdmAppComponent],
    entryComponents: [EditModalComponent, CreateModalComponent, ConfirmModalComponent, MetadataModalComponent, ErrorModalComponent]
} )
export class UasdmAppModule { }
