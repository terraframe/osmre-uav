///
///
///

import { Component, OnInit } from '@angular/core';
import { Subject } from 'rxjs';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { Account, User } from '../../model/account';

import { AccountService } from '../../service/account.service';

@Component({
	selector: 'account',
	templateUrl: './account.component.html',
	styles: ['.modal-form .check-block .chk-area { margin: 10px 0px 0 0;}']
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