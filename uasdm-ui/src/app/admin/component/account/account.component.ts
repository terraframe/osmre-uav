///
/// Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Runway SDK(tm).
///
/// Runway SDK(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Runway SDK(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
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