///
///
///

import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';

import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { User } from '../../model/account';

import { AccountService } from '../../service/account.service';

import { ErrorHandler, ErrorModalComponent } from '@shared/component';
import { environment } from 'src/environments/environment';
import { NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MustMatchDirective } from '../../../shared/directive/must-match.directive';
import { LocalizeComponent } from '../../../shared/component/localize/localize.component';
import { PhoneNumberValidatorDirective } from '../../../shared/directive/phone-number.directive';
import { PasswordValidatorDirective } from '../../../shared/directive/password-validator.directive';
import { PasswordStrengthBarComponent } from '../../../shared/component/password-strength-bar/password-strength-bar.component';
import { LocalizePipe } from '../../../shared/pipe/localize.pipe';

@Component({
    standalone: true,
    selector: 'account-invite-complete',
    templateUrl: './account-invite-complete.component.html',
    styles: ['.modal-form .check-block .chk-area { margin: 10px 0px 0 0;}'],
    imports: [NgIf, FormsModule, MustMatchDirective, LocalizeComponent, PhoneNumberValidatorDirective, PasswordValidatorDirective, PasswordStrengthBarComponent, LocalizePipe]
})
export class AccountInviteCompleteComponent implements OnInit {
	user: User;
	private sub: any;
	token: string;

    /*
     * Reference to the modal current showing
    */
	private bsModalRef: BsModalRef;

	constructor(
		private service: AccountService,
		private route: ActivatedRoute,
		private modalService: BsModalService) {
	}

	ngOnInit(): void {
		this.user = new User();
		this.user.phoneNumber = '';

		this.sub = this.route.params.subscribe(params => {
			this.token = params['token'];
		});
	}

	cancel(): void {
		window.location.href = environment.apiUrl;
	}

	onSubmit(): void {
		this.service.inviteComplete(this.user, this.token).then(response => {
			window.location.href = environment.apiUrl;
		});
	}

	error(err: HttpErrorResponse): void {
		// Handle error
		if (err !== null) {
			this.bsModalRef = this.modalService.show(ErrorModalComponent, { backdrop: true, class: 'modal-xl' });
			this.bsModalRef.content.message = ErrorHandler.getMessageFromError(err);
		}
	}

}