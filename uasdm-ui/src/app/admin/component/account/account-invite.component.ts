///
///
///

import { Component } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { Account, UserInvite } from '../../model/account';

import { AccountService } from '../../service/account.service';
import { NgIf, NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LocalizeComponent } from '../../../shared/component/localize/localize.component';
import { OrganizationFieldComponent } from '../../../shared/component/organization-field/organization-field.component';
import { BooleanFieldComponent } from '../../../shared/component/boolean-field/boolean-field.component';
import { LocalizePipe } from '../../../shared/pipe/localize.pipe';

@Component({
    standalone: true,
    selector: 'account-invite',
    templateUrl: './account-invite.component.html',
    styles: [],
    imports: [NgIf, FormsModule, LocalizeComponent, OrganizationFieldComponent, NgFor, BooleanFieldComponent, LocalizePipe]
})
export class AccountInviteComponent {
	invite: UserInvite;

	constructor(private service: AccountService, public bsModalRef: BsModalRef) { }

	init(groups): void {
		this.invite = new UserInvite();
		this.invite.groups = groups;
	}

	cancel(): void {
		this.bsModalRef.hide();
	}

	onSubmit(): void {
		let roleIds: string[] = [];

		for (let i = 0; i < this.invite.groups.length; i++) {
			let group = this.invite.groups[i];

			for (let j = 0; j < group.roles.length; j++) {
				let role = group.roles[j];

				if (role.assigned) {
					roleIds.push(role.roleId);
				}
			}
		}

		this.service.inviteUser(this.invite, roleIds).then(response => {
			this.bsModalRef.hide();
		});
	}
}