import { ErrorHandler } from './error-handler/error-handler';
import { UasdmHeaderComponent } from './header/header.component';
import { LoadingBarComponent } from './loading-bar/loading-bar.component';
import { LocalizeComponent } from './localize/localize.component';
import { BasicConfirmModalComponent } from './modal/basic-confirm-modal.component';
import { ErrorModalComponent } from './modal/error-modal.component';
import { NotificationModalComponent } from './modal/notification-modal.component';
import { PasswordStrengthBarComponent } from './password-strength-bar/password-strength-bar.component';
import { ProfileComponent } from './profile/profile.component';

export const components: any[] = [
	ErrorHandler,
	UasdmHeaderComponent,
	LoadingBarComponent,
	LocalizeComponent,
	BasicConfirmModalComponent,
	ErrorModalComponent,
	NotificationModalComponent,
	PasswordStrengthBarComponent,
	ProfileComponent
];

export * from './error-handler/error-handler';
export * from './header/header.component';
export * from './loading-bar/loading-bar.component';
export * from './localize/localize.component';
export * from './modal/notification-modal.component';
export * from './modal/error-modal.component';
export * from './modal/basic-confirm-modal.component';
export * from './password-strength-bar/password-strength-bar.component';
export * from './profile/profile.component';
