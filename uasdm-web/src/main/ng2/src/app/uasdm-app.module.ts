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
import { ImagePreviewModalComponent } from './management/modals/image-preview-modal.component';
import { NotificationModalComponent } from './management/modals/notification-modal.component';
import { UploadModalComponent } from './management/modals/upload-modal.component';

import { ForbiddenNameDirective } from './management/directives/forbidden-name.directive';
import { SafeHtmlPipe } from './pipes/safe-html.pipe'
import { OnlyNumber } from './management/directives/number-only.directive';


import { ManagementService } from './service/management.service';
import { MapService } from './service/map.service';
import { EventService } from './service/event.service';
import { AuthService } from './service/auth.service';
import { AdminGuardService } from './service/admin.guard.service';
import { CanDeactivateGuardService } from './service/can.deactivate.guard.service';


import { PasswordStrengthBarModule } from 'ng2-password-strength-bar';

import { CoreModule } from './core/core.module';

import { SessionService } from './authentication/session.service';
import { ProfileService } from './profile/profile.service';
import { ProfileComponent } from './profile/profile.component';

import { HubService } from './hub/hub.service';
import { ForgotPasswordService } from './forgotpassword/forgotpassword.service';
import { ForgotPasswordCompleteService } from './forgotpassword-complete/forgotpassword-complete.service';
import { AuthGuard } from './authentication/auth.guard';
import { AdminGuard } from './authentication/admin.guard';

import { HubHeaderComponent } from './hub/hub-header.component';
import { LoginHeaderComponent } from './authentication/login-header.component';

import { AdminModule } from './admin/admin.module';

@NgModule( {
    imports: [
        BrowserModule,
        BrowserAnimationsModule,
        FormsModule,
        HttpModule,
        CoreModule,
        UasdmAppRoutingModule,
        ReactiveFormsModule,
        TreeModule.forRoot(),
        ContextMenuModule.forRoot(),
        ModalModule.forRoot(),
        AlertModule.forRoot(),        
        BsDropdownModule.forRoot(),
        TypeaheadModule.forRoot(),
        AccordionModule.forRoot(),
        PasswordStrengthBarModule,
        AdminModule
    ],
    declarations: [
        UasdmAppComponent,
        EditModalComponent,
        CreateModalComponent,
        ConfirmModalComponent,
        NotificationModalComponent,
        MetadataModalComponent,
        ErrorModalComponent,
        ImagePreviewModalComponent,
        HubHeaderComponent,
    	LoginHeaderComponent,
    	ProfileComponent,
        UploadModalComponent,

        // Routing components
        routedComponents,
        UasdmHeaderComponent,
        ForbiddenNameDirective,
        SafeHtmlPipe,
        OnlyNumber
    ],
    providers: [
        AdminGuardService,
        CanDeactivateGuardService,
        CookieService,
        AuthService,
        ManagementService,
        EventService,
        MapService,
        SessionService,
        ProfileService,
        ForgotPasswordService,
	    ForgotPasswordCompleteService,
	    HubService,
	    AuthGuard,
	    AdminGuard
    ],
    bootstrap: [UasdmAppComponent],
    entryComponents: [
      UploadModalComponent,
      EditModalComponent,
      CreateModalComponent,
      ImagePreviewModalComponent,
      ConfirmModalComponent,
      NotificationModalComponent,
      MetadataModalComponent,
      ErrorModalComponent,
      HubHeaderComponent,
      LoginHeaderComponent,
      ProfileComponent
    ]
} )
export class UasdmAppModule { }
