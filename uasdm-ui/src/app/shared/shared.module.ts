///
///
///

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

import { TableModule } from 'primeng/table';
import { SliderModule } from 'primeng/slider';
import { DropdownModule } from 'primeng/dropdown';
import { MultiSelectModule } from 'primeng/multiselect';
import { ButtonModule } from 'primeng/button';

import { UasdmHeaderComponent } from './component/header/header.component';
import { LocalizeComponent } from './component/localize/localize.component';
import { LoadingBarComponent } from './component/loading-bar/loading-bar.component';
import { BooleanFieldComponent } from './component/boolean-field/boolean-field.component';
import { ProfileComponent } from './component/profile/profile.component';
import { ErrorModalComponent } from './component/modal/error-modal.component';
import { BasicConfirmModalComponent } from './component/modal/basic-confirm-modal.component';
import { NotificationModalComponent } from './component/modal/notification-modal.component';
import { ModalStepIndicatorComponent } from './modal/step-indicator/modal-step-indicator.component';
import { PasswordStrengthBarComponent } from './component/password-strength-bar/password-strength-bar.component';
import { GenericTableComponent } from './component/generic-table/generic-table.component';

import { LocalizePipe } from './pipe/localize.pipe';
import { KeysPipe } from './pipe/keys.pipe';
import { PhonePipe } from './pipe/phone.pipe';
import { FilterPipe } from './pipe/filter.pipe';
import { SafeHtmlPipe } from './pipe/safe-html.pipe';
import { IdmDatePipe } from './pipe/idmdate.pipe';

import { MustMatchDirective } from './directive/must-match.directive';
import { PhoneNumberValidatorDirective } from './directive/phone-number.directive'; 
import { PasswordValidatorDirective } from './directive/password-validator.directive';

import { DateService } from './service/date.service';
import { ProgressService } from './service/progress.service';
import { EventService } from './service/event.service';
import { LocalizationService } from './service/localization.service';
import { AuthService } from './service/auth.service';
import { ProfileService } from './service/profile.service';
import { SessionService } from './service/session.service';
import { AdminGuardService, AuthGuard } from './service/guard.service';
import { HttpBackendClient } from './service/http-backend-client.service';

import { LPGSyncService } from './service/lpg-sync.service';
import { OrganizationSyncService } from './service/organization-sync.service';


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
		CollapseModule.forRoot(),

        TableModule,
		SliderModule,
		DropdownModule,
		MultiSelectModule,
		ButtonModule
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
		ModalStepIndicatorComponent,
		PasswordStrengthBarComponent,
		KeysPipe,
		LocalizePipe,
		PhonePipe,
		FilterPipe,
		SafeHtmlPipe,
		IdmDatePipe,
		MustMatchDirective,
		PhoneNumberValidatorDirective,
		PasswordValidatorDirective,
		GenericTableComponent
	],
	exports: [
		UasdmHeaderComponent,
		LoadingBarComponent,
		BasicConfirmModalComponent,
		NotificationModalComponent,
		ErrorModalComponent,
		BooleanFieldComponent,
		LocalizeComponent,
		ModalStepIndicatorComponent,
		PasswordStrengthBarComponent,
		KeysPipe,
		LocalizePipe,
		PhonePipe,
		FilterPipe,
		SafeHtmlPipe,
		IdmDatePipe,
		MustMatchDirective,
		PhoneNumberValidatorDirective,
		PasswordValidatorDirective,
		GenericTableComponent
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
	static forRoot(): ModuleWithProviders<SharedModule> {
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
				DateService,
				AdminGuardService,
				AuthGuard,
				HttpBackendClient,
				LPGSyncService,
				OrganizationSyncService
			]
		};
	}
}