///
///
///

import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { ForgotPasswordService } from '../../service/forgotpassword.service';
import { NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MustMatchDirective } from '@shared/directive/must-match.directive';
import { LocalizeComponent } from '@shared/component/localize/localize.component';
import { PasswordValidatorDirective } from '@shared/directive/password-validator.directive';
import { PasswordStrengthBarComponent } from '@shared/component/password-strength-bar/password-strength-bar.component';
import { LocalizePipe } from '@shared/pipe/localize.pipe';

@Component({
    standalone: true,
    templateUrl: './forgotpassword-complete.component.html',
    styleUrls: ['./forgotpassword-complete.component.css'],
    imports: [NgIf, FormsModule, MustMatchDirective, LocalizeComponent, PasswordValidatorDirective, PasswordStrengthBarComponent, LocalizePipe]
})
export class ForgotPasswordCompleteComponent implements OnInit {
	newPassword: string;
	token: string;
	passwordIsReset: boolean = false;
	private sub: any;

	constructor(
		private service: ForgotPasswordService,
		private router: Router,
		private route: ActivatedRoute) {
	}

	ngOnInit() {
		this.sub = this.route.params.subscribe(params => {
			this.token = params['token'];
		});
	}

	ngOnDestroy() {
		this.sub.unsubscribe();
	}


	cancel(): void {
		this.router.navigate(['login']);
	}

	onSubmit(): void {
		this.service.complete(this.newPassword, this.token).then(() => {
			this.passwordIsReset = true;
		});
	}
}
