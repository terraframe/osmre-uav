import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpModule, XHRBackend, RequestOptions, Http } from '@angular/http';
import { ModalModule } from 'ngx-bootstrap/modal';
import { TreeModule } from 'angular-tree-component';
import { ContextMenuModule } from 'ngx-contextmenu';

import './rxjs-extensions';


import { UasdmAppComponent } from './uasdm-app.component';
import { UasdmAppRoutingModule, routedComponents } from './uasdm-app-routing.module';
import { EditModalComponent } from './management/modals/edit-modal.component';
import { CreateModalComponent } from './management/modals/create-modal.component';
import { UasdmHeaderComponent } from './header.component';


import { ManagementService } from './management/management.service';

@NgModule( {
    imports: [
        BrowserModule,
        FormsModule,
        HttpModule,
        UasdmAppRoutingModule,
        ReactiveFormsModule,
        TreeModule.forRoot(),
        ContextMenuModule.forRoot(),
        ModalModule.forRoot()
    ],
    declarations: [
        UasdmAppComponent,
        EditModalComponent,
        CreateModalComponent,
        // Routing components
        routedComponents,
        UasdmHeaderComponent,
    ],
    providers: [
        ManagementService
    ],
    bootstrap: [UasdmAppComponent],
    entryComponents: [EditModalComponent, CreateModalComponent]
} )
export class UasdmAppModule { }
