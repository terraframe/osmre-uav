import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { TreeModule } from 'angular-tree-component';
import { ContextMenuModule } from 'ngx-contextmenu';
import { ModalModule } from 'ngx-bootstrap/modal';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { TypeaheadModule } from 'ngx-bootstrap/typeahead';
import { AccordionModule } from 'ngx-bootstrap/accordion';
import { AlertModule } from 'ngx-bootstrap/alert';
import { CookieService } from 'ngx-cookie-service';
import { PasswordStrengthBarModule } from 'ng2-password-strength-bar';

import './rxjs-extensions';

import { UasdmAppComponent } from './uasdm-app.component';
import { UasdmAppRoutingModule, routedComponents } from './uasdm-app-routing.module';

import { HubService } from './core/service/hub.service';
import { ForgotPasswordService } from './core/service/forgotpassword.service';
import { ForgotPasswordCompleteService } from './core/service/forgotpassword-complete.service';

import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { HttpErrorInterceptor } from './core/service/http-error.interceptor';

import { HubHeaderComponent } from './core/component/hub/hub-header.component';
import { LoginHeaderComponent } from './core/component/login/login-header.component';

import { NgxPaginationModule } from 'ngx-pagination';
import { SharedModule } from './shared/shared.module';

@NgModule( {
    imports: [
        BrowserModule,
        BrowserAnimationsModule,
        FormsModule,
        ReactiveFormsModule,
        HttpClientModule,
        UasdmAppRoutingModule,
        TreeModule.forRoot(),
        ContextMenuModule.forRoot(),
        //        ModalModule.forRoot(),
        AlertModule.forRoot(),
        BsDropdownModule.forRoot(),
        TypeaheadModule.forRoot(),
        AccordionModule.forRoot(),
        NgxPaginationModule,
        PasswordStrengthBarModule,
        SharedModule.forRoot()
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
            provide: HTTP_INTERCEPTORS,
            useClass: HttpErrorInterceptor,
            multi: true
        },
        CookieService,
        ForgotPasswordService,
        ForgotPasswordCompleteService,
        HubService
    ],
    bootstrap: [UasdmAppComponent],
    entryComponents: []
} )
export class UasdmAppModule { }
