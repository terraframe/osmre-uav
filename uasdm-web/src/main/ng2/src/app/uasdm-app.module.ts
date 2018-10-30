import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpModule, XHRBackend, RequestOptions, Http } from '@angular/http';
import { ModalModule } from 'ngx-bootstrap/modal';
import { TreeModule } from 'angular-tree-component';
import { ContextMenuModule } from 'ngx-contextmenu';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { CookieService } from 'ngx-cookie-service';

import './rxjs-extensions';


import { UasdmAppComponent } from './uasdm-app.component';
import { UasdmAppRoutingModule, routedComponents } from './uasdm-app-routing.module';
import { EditModalComponent } from './management/modals/edit-modal.component';
import { CreateModalComponent } from './management/modals/create-modal.component';
import { UasdmHeaderComponent } from './header.component';
import { ConfirmModalComponent } from './management/modals/confirm-modal.component';
import { ErrorModalComponent } from './management/modals/error-modal.component';
import { LoadingBarComponent } from './loading-bar/loading-bar.component';

import { ManagementService } from './management/management.service';
import { EventService } from './event/event.service';

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
        BsDropdownModule.forRoot(),
    ],
    declarations: [
        UasdmAppComponent,
        EditModalComponent,
        CreateModalComponent,
        ConfirmModalComponent,
        ErrorModalComponent,
        LoadingBarComponent,
        
        // Routing components
        routedComponents,
        UasdmHeaderComponent,
    ],
    providers: [
        CookieService,
        ManagementService,
        EventService        
    ],
    bootstrap: [UasdmAppComponent],
    entryComponents: [EditModalComponent, CreateModalComponent, ConfirmModalComponent, ErrorModalComponent]
} )
export class UasdmAppModule { }
