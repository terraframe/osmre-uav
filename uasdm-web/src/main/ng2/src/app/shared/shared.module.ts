import { NgModule, ModuleWithProviders } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CookieService } from 'ngx-cookie-service';
import { ModalModule } from 'ngx-bootstrap/modal';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { TypeaheadModule } from 'ngx-bootstrap/typeahead';
import { AccordionModule } from 'ngx-bootstrap/accordion';
import { AlertModule } from 'ngx-bootstrap/alert';
import { CollapseModule } from 'ngx-bootstrap/collapse';

import { UasdmHeaderComponent } from './component/header/header.component';
import { LocalizeComponent } from './component/localize/localize.component';
import { LoadingBarComponent } from './component/loading-bar/loading-bar.component';
import { BooleanFieldComponent } from './component/boolean-field/boolean-field.component';
import { VideoPlayerComponent } from './component/video-player/video-player.component';
import { ProfileComponent } from './component/profile/profile.component';
import { ErrorModalComponent } from './component/modal/error-modal.component';
import { BasicConfirmModalComponent } from './component/modal/basic-confirm-modal.component';
import { NotificationModalComponent } from './component/modal/notification-modal.component';
import { ModalStepIndicatorComponent } from './modal/step-indicator/modal-step-indicator.component';
import { PasswordStrengthBarComponent } from './component/password-strength-bar/password-strength-bar.component';

import { LocalizePipe } from './pipe/localize.pipe';
import { KeysPipe } from './pipe/keys.pipe';
import { PhonePipe } from './pipe/phone.pipe';
import { FilterPipe } from './pipe/filter.pipe';
import { SafeHtmlPipe } from './pipe/safe-html.pipe';

import { MustMatchDirective } from './directive/must-match.directive';
import { PhoneNumberValidatorDirective } from './directive/phone-number.directive';

import { ProgressService } from './service/progress.service';
import { EventService } from './service/event.service';
import { LocalizationService } from './service/localization.service';
import { AuthService } from './service/auth.service';
import { ProfileService } from './service/profile.service';
import { SessionService } from './service/session.service';
import { AdminGuardService, AuthGuard } from './service/guard.service';
import { HttpBackendClient } from './service/http-backend-client.service';


@NgModule({
	imports: [
		CommonModule,
		RouterModule,
		FormsModule,
		ModalModule.forRoot(),
		AlertModule,
		BsDropdownModule,
		TypeaheadModule,
		AccordionModule,
		CollapseModule.forRoot()
	],
	declarations: [
		UasdmHeaderComponent,
		LoadingBarComponent,
		ProfileComponent,
		BasicConfirmModalComponent,
		NotificationModalComponent,
		ErrorModalComponent,
		LocalizeComponent,
		BooleanFieldComponent,
		VideoPlayerComponent,
		ModalStepIndicatorComponent,
		PasswordStrengthBarComponent,
		KeysPipe,
		LocalizePipe,
		PhonePipe,
		FilterPipe,
		SafeHtmlPipe,
		MustMatchDirective,
		PhoneNumberValidatorDirective
	],
	exports: [
		UasdmHeaderComponent,
		LoadingBarComponent,
		BasicConfirmModalComponent,
		NotificationModalComponent,
		ErrorModalComponent,
		BooleanFieldComponent,
		VideoPlayerComponent,
		LocalizeComponent,
		ModalStepIndicatorComponent,
		PasswordStrengthBarComponent,
		KeysPipe,
		LocalizePipe,
		PhonePipe,
		FilterPipe,
		SafeHtmlPipe,
		MustMatchDirective,
		PhoneNumberValidatorDirective
	],
	entryComponents: [
		ErrorModalComponent,
		BasicConfirmModalComponent,
		NotificationModalComponent,
		LoadingBarComponent,
		ProfileComponent
	]
})
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