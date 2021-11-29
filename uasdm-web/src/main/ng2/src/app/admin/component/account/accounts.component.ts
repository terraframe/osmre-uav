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
import { Router } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { BasicConfirmModalComponent } from '@shared/component/modal/basic-confirm-modal.component';
import { GenericTableColumn, GenericTableConfig, TableEvent } from '@shared/model/generic-table';

import { User, Account } from '../../model/account';
import { AccountService } from '../../service/account.service';
import { AccountComponent } from './account.component';
import { AccountInviteComponent } from './account-invite.component';
import { Subject } from 'rxjs';

@Component({
	selector: 'accounts',
	templateUrl: './accounts.component.html',
	styles: ['./accounts.css']
})
export class AccountsComponent implements OnInit {

	config: GenericTableConfig;
	cols: GenericTableColumn[] = [
		{ header: 'Username', field: 'username', type: 'TEXT', sortable: true },
		{ header: 'First name', field: 'firstName', type: 'TEXT', sortable: true },
		{ header: 'Last name', field: 'lastName', type: 'TEXT', sortable: true },
		{ header: 'Phone Number', field: 'phoneNumber', type: 'TEXT', sortable: true },
		{ header: 'Email Address', field: 'email', type: 'TEXT', sortable: true },
		{ header: 'Bureau', field: 'bureau', type: 'TEXT', sortable: true, filter: true },
		{ header: '', type: 'ACTIONS', sortable: false },
	];
	refresh: Subject<void>;

	/*
	 * Reference to the modal current showing
	*/
	private bsModalRef: BsModalRef;


	constructor(private router: Router, private service: AccountService, private modalService: BsModalService) { }

	ngOnInit(): void {
		this.config = {
			service: this.service,
			remove: true,
			edit: true,
			create: true,
			label: 'User'
		}

		this.refresh = new Subject<void>();
	}

	onClick(event: TableEvent): void {
		if (event.type === 'edit') {
			this.onEdit(event.row as User);
		}
		else if (event.type === 'remove') {
			this.onRemove(event.row as User);
		}
		else if (event.type === 'create') {
			this.newInstance();
		}
	}


	onRemove(user: User): void {
		this.bsModalRef = this.modalService.show(BasicConfirmModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
		});
		this.bsModalRef.content.message = 'Are you sure you want to delete the user [' + user.username + ']?';
		this.bsModalRef.content.data = user;
		this.bsModalRef.content.type = 'DANGER';
		this.bsModalRef.content.submitText = 'Delete';

		(<BasicConfirmModalComponent>this.bsModalRef.content).onConfirm.subscribe(data => {
			this.remove(data);
		});
	}

	remove(user: User): void {
		this.service.remove(user.oid).then(response => {
			this.refresh.next();
		});
	}

	onEdit(user: User): void {
		this.service.edit(user.oid).then(account => {
			this.bsModalRef = this.modalService.show(AccountComponent, {
				animated: true,
				backdrop: true,
				ignoreBackdropClick: true,
				'class': 'upload-modal'
			});
			this.bsModalRef.content.init(account);

			this.bsModalRef.content.onAccountChange.subscribe(() => {
				this.refresh.next();
			});
		});
	}

	newInstance(): void {
		this.service.newInvite().then(account => {
			this.bsModalRef = this.modalService.show(AccountComponent, {
				animated: true,
				backdrop: true,
				ignoreBackdropClick: true,
				'class': 'upload-modal'
			});
			this.bsModalRef.content.init(account);

			this.bsModalRef.content.onAccountChange.subscribe(() => {
				this.refresh.next();
			});
		});
	}

	inviteUsers(): void {

		this.service.newInvite().then((account: Account) => {
			this.bsModalRef = this.modalService.show(AccountInviteComponent, {
				animated: true,
				backdrop: true,
				ignoreBackdropClick: true,
				'class': 'upload-modal'
			});
			this.bsModalRef.content.init(account.groups, account.bureaus);
		});
	}
}
