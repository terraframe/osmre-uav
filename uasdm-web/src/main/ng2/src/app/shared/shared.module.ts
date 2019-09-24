import { NgModule, ModuleWithProviders } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { CookieService } from 'ngx-cookie-service';
import { ProgressbarModule } from 'ngx-bootstrap/progressbar';
import { PasswordStrengthBarModule } from 'ng2-password-strength-bar';
import { ModalModule } from 'ngx-bootstrap/modal';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { TypeaheadModule } from 'ngx-bootstrap/typeahead';
import { AccordionModule } from 'ngx-bootstrap/accordion';
import { AlertModule } from 'ngx-bootstrap/alert';

import { UasdmHeaderComponent } from './component/header/header.component';
import { ProgressBarComponent } from './component/progress-bar/progress-bar.component';
import { LocalizeComponent } from './component/localize/localize.component';
import { LoadingBarComponent } from './component/loading-bar/loading-bar.component';
import { MessageComponent } from './component/message/message.component';
import { BooleanFieldComponent } from './component/boolean-field/boolean-field.component';
import { ProfileComponent } from './component/profile/profile.component';
import { ErrorModalComponent } from './component/modal/error-modal.component';
import { BasicConfirmModalComponent } from './component/modal/basic-confirm-modal.component';
import { NotificationModalComponent } from './component/modal/notification-modal.component';

import { LocalizePipe } from './pipe/localize.pipe';
import { KeysPipe } from './pipe/keys.pipe';
import { PhonePipe } from './pipe/phone.pipe';
import { FilterPipe } from './pipe/filter.pipe';
import { SafeHtmlPipe } from './pipe/safe-html.pipe';

import { ProgressService } from './service/progress.service';
import { EventService } from './service/event.service';
import { LocalizationService } from './service/localization.service';
import { AuthService } from './service/auth.service';
import { ProfileService } from './service/profile.service';
import { SessionService } from './service/session.service';
import { AdminGuardService, AuthGuard } from './service/guard.service';
import { HttpBackendClient } from './service/http-backend-client.service';

@NgModule( {
    imports: [
        CommonModule,
        RouterModule, 
        FormsModule,
        ProgressbarModule,
        HttpClientModule,
        PasswordStrengthBarModule,
        ModalModule.forRoot(),
        AlertModule,
        BsDropdownModule,
        TypeaheadModule,
        AccordionModule
    ],
    declarations: [
        UasdmHeaderComponent,
        ProgressBarComponent,
        LoadingBarComponent,
        ProfileComponent,
        BasicConfirmModalComponent,
        NotificationModalComponent,
        ErrorModalComponent,
        LocalizeComponent,
        MessageComponent,
        BooleanFieldComponent,
        KeysPipe,
        LocalizePipe,
        PhonePipe,
        FilterPipe,
        SafeHtmlPipe
    ],
    exports: [
        UasdmHeaderComponent,
        ProgressBarComponent,
        LoadingBarComponent,
        BasicConfirmModalComponent,
        NotificationModalComponent,
        ErrorModalComponent,
        BooleanFieldComponent,
        LocalizeComponent,
        MessageComponent,
        KeysPipe,
        LocalizePipe,
        PhonePipe,
        FilterPipe,
        SafeHtmlPipe
    ],
    entryComponents: [
        ErrorModalComponent,
        BasicConfirmModalComponent,
        NotificationModalComponent,        
        LoadingBarComponent,
        ProfileComponent
    ]
} )
export class SharedModule {
    static forRoot(): ModuleWithProviders {
        return {
            ngModule: SharedModule,
            providers: [
                CookieService,
                AuthService,
                SessionService,
                ProfileService,
                LocalizationService,
                EventService,
                ProgressService,
                AdminGuardService,
                AuthGuard,
                HttpBackendClient
            ]
        };
    }
}