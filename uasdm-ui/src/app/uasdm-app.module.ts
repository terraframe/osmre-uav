///
///
///

import { APP_INITIALIZER, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
//import { TreeModule } from 'angular-tree-component';
//import { ContextMenuModule } from 'ngx-contextmenu';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { TypeaheadModule } from 'ngx-bootstrap/typeahead';
import { AccordionModule } from 'ngx-bootstrap/accordion';
import { AlertModule } from 'ngx-bootstrap/alert';
import { CookieService } from 'ngx-cookie-service';
import { CollapseModule } from 'ngx-bootstrap/collapse';
import { TreeModule } from "@circlon/angular-tree-component";

import './rxjs-extensions';

import { UasdmAppComponent } from './uasdm-app.component';
import { UasdmAppRoutingModule, routedComponents } from './uasdm-app-routing.module';

import { HubService } from './core/service/hub.service';
import { ForgotPasswordService } from './core/service/forgotpassword.service';

import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { HttpErrorInterceptor } from './core/service/http-error.interceptor';

import { HubHeaderComponent } from './core/component/hub/hub-header.component';
import { LoginHeaderComponent } from './core/component/login/login-header.component';

import { NgxPaginationModule } from 'ngx-pagination';
import { SharedModule } from './shared/shared.module';
import { APP_BASE_HREF, DatePipe, PlatformLocation } from '@angular/common';
import { ConfigurationService } from '@core/service/configuration.service';

@NgModule( {
    imports: [
        BrowserModule,
        BrowserAnimationsModule,
        FormsModule,
        ReactiveFormsModule,
        HttpClientModule,
        UasdmAppRoutingModule,
        AlertModule.forRoot(),
        BsDropdownModule.forRoot(),
        TypeaheadModule.forRoot(),
        AccordionModule.forRoot(),
        NgxPaginationModule,
        SharedModule.forRoot(),
        CollapseModule.forRoot(),
        TreeModule,
    ],
    declarations: [
        UasdmAppComponent,
        HubHeaderComponent,
        LoginHeaderComponent,

        // Routing components
        routedComponents
    ],
    providers: [
        {
            provide: APP_BASE_HREF,
            useFactory: (s: PlatformLocation) => s.getBaseHrefFromDOM(),
            deps: [PlatformLocation]
        },
        {
            provide: HTTP_INTERCEPTORS,
            useClass: HttpErrorInterceptor,
            multi: true
        },
        CookieService,
        ForgotPasswordService,
        HubService,
        DatePipe,
        ConfigurationService,
        {
            'provide': APP_INITIALIZER,
            'useFactory': (service: ConfigurationService) => {
                // Do initing of services that is required before app loads
                // NOTE: this factory needs to return a function (that then returns a promise)
                return () => service.load()  // + any other services...
            },
            'deps': [ConfigurationService, HttpClientModule],
            'multi': true,
        }
    ],
    bootstrap: [UasdmAppComponent],
    entryComponents: []
} )
export class UasdmAppModule { }
