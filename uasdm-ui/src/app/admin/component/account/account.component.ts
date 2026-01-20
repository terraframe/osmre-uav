///
///
///

import { Component, OnInit } from '@angular/core';
import { Subject } from 'rxjs';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { Account, User } from '../../model/account';

import { AccountService } from '../../service/account.service';
import { NgIf, NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MustMatchDirective } from '../../../shared/directive/must-match.directive';
import { LocalizeComponent } from '../../../shared/component/localize/localize.component';
import { PhoneNumberValidatorDirective } from '../../../shared/directive/phone-number.directive';
import { OrganizationFieldComponent } from '../../../shared/component/organization-field/organization-field.component';
import { BooleanFieldComponent } from '../../../shared/component/boolean-field/boolean-field.component';
import { PasswordValidatorDirective } from '../../../shared/directive/password-validator.directive';
import { PasswordStrengthBarComponent } from '../../../shared/component/password-strength-bar/password-strength-bar.component';
import { LocalizePipe } from '../../../shared/pipe/localize.pipe';

@Component({
    standalone: true,
    selector: 'account',
    templateUrl: './account.component.html',
    styles: ['.modal-form .check-block .chk-area { margin: 10px 0px 0 0;}'],
    imports: [NgIf, FormsModule, MustMatchDirective, LocalizeComponent, PhoneNumberValidatorDirective, OrganizationFieldComponent, BooleanFieldComponent, PasswordValidatorDirective, PasswordStrengthBarComponent, NgFor, LocalizePipe]
})
export class AccountComponent implements OnInit {
	account: Account;
	certainPassword: string;
    /*
     * Observable subject for Account changes.  Called when create is successful 
     */
	public onAccountChange: Subject<User>;

	constructor(private service: AccountService, public bsModalRef: BsModalRef) { }

	ngOnInit(): void {
		this.onAccountChange = new Subject();
	}

	init(account: Account): void {
		this.account = account;
	}

	cancel(): void {
		if (this.account.user.newInstance === true) {
			this.bsModalRef.hide();
		}
		else {
          this.bsModalRef.hide();
		}
	}

	onSubmit(): void {
		let roleIds: string[] = [];

		for (let i = 0; i < this.account.groups.length; i++) {
			let group = this.account.groups[i];

			for (let j = 0; j < group.roles.length; j++) {
				let role = group.roles[j];

				if (role.assigned) {
					roleIds.push(role.roleId);
				}
			}
		}

		if (!this.account.changePassword && !this.account.user.newInstance) {
			delete this.account.user.password;
		}

		this.service.apply(this.account.user, roleIds).then(response => {
			this.onAccountChange.next(response);
			this.bsModalRef.hide();
		});
	}
}